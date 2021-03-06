package de.fraunhofer.fit.ips.server;

import com.google.protobuf.ByteString;
import de.fraunhofer.fit.ips.Utils;
import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import de.fraunhofer.fit.ips.model.converter.Converter;
import de.fraunhofer.fit.ips.model.parser.XSDParser;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.particleassignment.ParticleScopeAnalyzer;
import de.fraunhofer.fit.ips.particleassignment.ParticleType;
import de.fraunhofer.fit.ips.proto.javabackend.AssignParticlesRequest;
import de.fraunhofer.fit.ips.proto.javabackend.AssignParticlesResponse;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportResponse;
import de.fraunhofer.fit.ips.proto.javabackend.JavaBackendGrpc;
import de.fraunhofer.fit.ips.proto.javabackend.ReportType;
import de.fraunhofer.fit.ips.proto.javabackend.ValidationRequest;
import de.fraunhofer.fit.ips.proto.javabackend.ValidationResponse;
import de.fraunhofer.fit.ips.reportgenerator.reporter.PdfReporter;
import de.fraunhofer.fit.ips.reportgenerator.reporter.ReportConfiguration;
import de.fraunhofer.fit.ips.reportgenerator.reporter.ReportMetadata;
import de.fraunhofer.fit.ips.reportgenerator.reporter.Reporter;
import de.fraunhofer.fit.ips.xsd.Validator;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class MyJavaBackendImpl extends JavaBackendGrpc.JavaBackendImplBase {
    private byte[] template = Utils.readResourceIntoByteArray("/DocumentationTemplate.docx");

    public MyJavaBackendImpl() throws IOException {
    }

    abstract class ResultsCache {
        @Getter(lazy = true) private final byte[] docxReport = generateDocxReport();
        @Getter(lazy = true) private final byte[] pdfReport = generatePdfReport();

        abstract byte[] generateDocxReport();

        byte[] generatePdfReport() {
            try {
                return new PdfReporter().report(generateDocxReport());
            } catch (IOException e) {
                log.error(Objects.toString(e.getMessage()), e);
                return new byte[0];
            }
        }
    }

    @RequiredArgsConstructor
    class SimpleResultsCache extends ResultsCache {
        final Supplier<byte[]> docxReportGenerator;

        @Override
        byte[] generateDocxReport() {
            return docxReportGenerator.get();
        }
    }

    @Override
    public void createReport(final CreateReportRequest request,
                             final StreamObserver<CreateReportResponse> responseObserver) {
        final ReportConfiguration reportConfiguration = getReportConfiguration(request);
        final Schema schema = XSDParser.createFromData(request.getSchemaAndProjectStructure().getSchema().getXsd(), reportConfiguration.getXsdPrefix())
                                       .process(reportConfiguration.getLocalPrefixIfMissing());
        final Project project;
        try {
            project = Converter.convert(schema, request.getSchemaAndProjectStructure().getProject());
        } catch (final IllegalDocumentStructureException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException());
            return;
        }

        final ReportMetadata reportMetadata = getReportMetadata(request);

        final ResultsCache results = new SimpleResultsCache(() -> Reporter.createReport(schema, project, reportConfiguration, reportMetadata, template));

        final CreateReportResponse.Builder responseBuilder = CreateReportResponse.newBuilder();
        final List<ReportType> reportTypesList = request.getReportTypesList();
        for (final ReportType reportType : reportTypesList) {
            switch (reportType) {
                case REPORT_TYPE_PDF:
                    responseBuilder.addReports(
                            CreateReportResponse.Report
                                    .newBuilder()
                                    .setReportType(reportType)
                                    .setReport(ByteString.copyFrom(results.getPdfReport()))
                                    .build()
                    );
                    break;
                case REPORT_TYPE_DOCX:
                    responseBuilder.addReports(
                            CreateReportResponse.Report
                                    .newBuilder()
                                    .setReportType(reportType)
                                    .setReport(ByteString.copyFrom(results.getDocxReport()))
                                    .build()
                    );
                    break;
            }
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private ReportMetadata getReportMetadata(final CreateReportRequest request) {
        final ReportMetadata.ReportMetadataBuilder reportMetadataBuilder = ReportMetadata.builder();
        // TODO fill metadata with life
        final CreateReportRequest.Metadata metadata = request.getMetadata();
        return reportMetadataBuilder.build();
    }

    private ReportConfiguration getReportConfiguration(final CreateReportRequest request) {
        final ReportConfiguration.ReportConfigurationBuilder configurationBuilder = ReportConfiguration.builder();
        if (request.hasConfiguration()) {
            final CreateReportRequest.Configuration configuration = request.getConfiguration();
            checkAndSet(configuration.getLanguagesList(), List::isEmpty, configurationBuilder::languages);
            checkAndSet(configuration.getXsdDocumentationLanguage(), String::isEmpty, configurationBuilder::xsdDocumentationLanguage);
            checkAndSet(configuration.getXsdPrefix(), String::isEmpty, configurationBuilder::xsdPrefix);
            checkAndSet(configuration.getLocalPrefixIfMissing(), String::isEmpty, configurationBuilder::localPrefixIfMissing);
            configurationBuilder.expandAttributeGroups(!configuration.getPreventExpandingAttributeGroups());
            configurationBuilder.expandElementGroups(!configuration.getPreventExpandingElementGroups());
            configurationBuilder.hideInheritanceInExtensions(!configuration.getPrintInheritanceInExtensions());
            configurationBuilder.inlineEnums(!configuration.getPreventInliningEnums());
        }
        return configurationBuilder.build();
    }

    private <T> void checkAndSet(final T value, final Predicate<T> invalidator, final Consumer<T> setter) {
        if (!invalidator.test(value)) {
            setter.accept(value);
        }
    }

    @Override
    public void assignParticles(final AssignParticlesRequest request,
                                final StreamObserver<AssignParticlesResponse> responseObserver) {
        final ReportConfiguration defaultReportConfiguration = ReportConfiguration.builder().build();
        final Schema schema = XSDParser.createFromData(request.getSchemaAndProjectStructure().getSchema().getXsd(), defaultReportConfiguration.getXsdPrefix())
                                       .process(defaultReportConfiguration.getLocalPrefixIfMissing());
        final Project project;
        final Map<StructureBase, String> instanceToIdentifier = new IdentityHashMap<>();
        try {
            project = Converter.convert(schema, request.getSchemaAndProjectStructure().getProject(), new Converter.CachingOption() {
                @Override
                public <T extends StructureBase> T cacheAndReturn(final String identifier, final T instance) {
                    instanceToIdentifier.put(instance, identifier);
                    return instance;
                }
            });
        } catch (final IllegalDocumentStructureException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException());
            return;
        }
        final EnumSet<ParticleType> consideredParticleTypes = EnumSet.allOf(ParticleType.class);
        for (final de.fraunhofer.fit.ips.proto.javabackend.ParticleType particleType : request.getIgnoredParticleTypesList()) {
            switch (particleType) {
                case PARTICLE_TYPE_ELEMENT:
                    consideredParticleTypes.remove(ParticleType.ELEMENT);
                    break;
                case PARTICLE_TYPE_COMPLEX_TYPE:
                    consideredParticleTypes.remove(ParticleType.COMPLEX_TYPE);
                    break;
                case PARTICLE_TYPE_GROUP:
                    consideredParticleTypes.remove(ParticleType.GROUP);
                    break;
                case PARTICLE_TYPE_SIMPLE_TYPE_RESTRICTION:
                    consideredParticleTypes.remove(ParticleType.SIMPLE_TYPE_RESTRICTION);
                    break;
                case PARTICLE_TYPE_SIMPLE_TYPE_ENUMERATION:
                    consideredParticleTypes.remove(ParticleType.SIMPLE_TYPE_ENUMERATION);
                    break;
                case PARTICLE_TYPE_SIMPLE_TYPE_UNION:
                    consideredParticleTypes.remove(ParticleType.SIMPLE_TYPE_UNION);
                    break;
                case PARTICLE_TYPE_SIMPLE_TYPE_LIST:
                    consideredParticleTypes.remove(ParticleType.SIMPLE_TYPE_LIST);
                    break;
                case PARTICLE_TYPE_GLOBAL_ATTRIBUTE:
                    consideredParticleTypes.remove(ParticleType.GLOBAL_ATTRIBUTE);
                    break;
                case PARTICLE_TYPE_GLOBAL_ATTRIBUTE_GROUP:
                    consideredParticleTypes.remove(ParticleType.GLOBAL_ATTRIBUTE_GROUP);
                    break;
                case PARTICLE_TYPE_UNSET:
                    break;
                case UNRECOGNIZED:
                    responseObserver.onError(Status.INVALID_ARGUMENT.augmentDescription("unrecognized particle type " + particleType).asException());
                    return;
            }
        }
        final Map<StructureBase, List<QName>> categorizedDanglingParticles = ParticleScopeAnalyzer.categorizeDanglingParticles(schema, project, consideredParticleTypes);
        final AssignParticlesResponse.Builder responseBuilder = AssignParticlesResponse.newBuilder();
        for (final Map.Entry<StructureBase, List<QName>> entry : categorizedDanglingParticles.entrySet()) {
            final AssignParticlesResponse.TargetIdentifierAndParticles.Builder tiapBuilder
                    = AssignParticlesResponse.TargetIdentifierAndParticles.newBuilder().setIdentifier(instanceToIdentifier.get(entry.getKey()));
            for (final QName qName : entry.getValue()) {
                tiapBuilder.addQName(de.fraunhofer.fit.ips.proto.structure.QName
                        .newBuilder()
                        .setNamespaceUri(qName.getNamespaceURI())
                        .setNcName(qName.getLocalPart())
                        .build());
            }
            responseBuilder.addTargetIdentifierAndParticles(tiapBuilder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void validate(final ValidationRequest request,
                         final StreamObserver<ValidationResponse> responseObserver) {
        final Collection<Validator.ValidationError> validationErrors = Validator.validate(request.getSchema().getXsd());
        final ValidationResponse.Builder responseBuilder = ValidationResponse.newBuilder();
        for (final Validator.ValidationError validationError : validationErrors) {
            final Validator.ValidationError.ErrorLocation location = validationError.getLocation();
            responseBuilder.addErrors(ValidationResponse.Error
                    .newBuilder()
                    .setLine(location.getLine())
                    .setColumn(location.getColumn())
                    .setMessage(validationError.getMessage())
                    .build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
