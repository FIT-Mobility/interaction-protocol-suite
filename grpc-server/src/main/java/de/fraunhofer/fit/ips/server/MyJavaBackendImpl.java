package de.fraunhofer.fit.ips.server;

import com.google.protobuf.ByteString;
import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import de.fraunhofer.fit.ips.proto.javabackend.AssignTypesRequest;
import de.fraunhofer.fit.ips.proto.javabackend.AssignTypesResponse;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportResponse;
import de.fraunhofer.fit.ips.proto.javabackend.JavaBackendGrpc;
import de.fraunhofer.fit.ips.proto.javabackend.ReportType;
import de.fraunhofer.fit.ips.proto.javabackend.ValidationRequest;
import de.fraunhofer.fit.ips.proto.javabackend.ValidationResponse;
import de.fraunhofer.fit.ips.reportgenerator.converter2.Converter;
import de.fraunhofer.fit.ips.reportgenerator.model.template.Project;
import de.fraunhofer.fit.ips.reportgenerator.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.parser.XSDParser;
import de.fraunhofer.fit.ips.reportgenerator.reporter2.ReportConfiguration;
import de.fraunhofer.fit.ips.reportgenerator.reporter2.ReportMetadata;
import de.fraunhofer.fit.ips.reportgenerator.reporter2.Reporter;
import de.fraunhofer.fit.ips.reportgenerator.typeassignment.TypeScopeAnalyzer;
import de.fraunhofer.fit.ips.xsd.Validator;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class MyJavaBackendImpl extends JavaBackendGrpc.JavaBackendImplBase {
    private byte[] template = read("/DocumentationTemplate.docx");

    public MyJavaBackendImpl() throws IOException {
    }

    private static byte[] read(@Nonnull final String resourceName) throws IOException {
        try (final InputStream inputStream = Server.class.getResourceAsStream(resourceName);
             final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toByteArray();
        }
    }


    @Override
    public void createReport(final CreateReportRequest request,
                             final StreamObserver<CreateReportResponse> responseObserver) {
        final Schema schema = XSDParser.createFromData(request.getSchemaAndProjectStructure().getSchema().getXsd()).process();
        final Project project;
        try {
            project = Converter.convert(schema, request.getSchemaAndProjectStructure().getProject());
        } catch (final IllegalDocumentStructureException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException());
            return;
        }
        final ReportConfiguration reportConfiguration = getReportConfiguration(request);

        // TODO fill metadata with life
        final ReportMetadata.ReportMetadataBuilder reportMetadataBuilder = ReportMetadata.builder();
        final CreateReportRequest.Metadata metadata = request.getMetadata();
        final ReportMetadata reportMetadata = reportMetadataBuilder.build();

        final byte[] report = Reporter.createReport(schema, project, reportConfiguration, reportMetadata, template);
        final byte[] pdfBytes = report; // FIXME convert to pdf

        final CreateReportResponse.Builder responseBuilder = CreateReportResponse.newBuilder();
        final List<ReportType> reportTypesList = request.getReportTypesList();
        for (final ReportType reportType : reportTypesList) {
            switch (reportType) {
                case REPORT_TYPE_PDF:
                    responseBuilder.addReports(
                            CreateReportResponse.Report
                                    .newBuilder()
                                    .setReportType(reportType)
                                    .setReport(ByteString.copyFrom(pdfBytes))
                                    .build()
                    );
                    break;
                case REPORT_TYPE_DOCX:
                    responseBuilder.addReports(
                            CreateReportResponse.Report
                                    .newBuilder()
                                    .setReportType(reportType)
                                    .setReport(ByteString.copyFrom(report))
                                    .build()
                    );
                    break;
            }
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private ReportConfiguration getReportConfiguration(CreateReportRequest request) {
        final ReportConfiguration.ReportConfigurationBuilder configurationBuilder = ReportConfiguration.builder();
        if (request.hasConfiguration()) {
            final CreateReportRequest.Configuration configuration = request.getConfiguration();
            checkAndSet(configuration.getLanguagesList(), List::isEmpty, configurationBuilder::languages);
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
    public void assignTypes(final AssignTypesRequest request,
                            final StreamObserver<AssignTypesResponse> responseObserver) {
        final Schema schema = XSDParser.createFromData(request.getSchemaAndProjectStructure().getSchema().getXsd()).process();
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
        final Map<StructureBase, List<QName>> categorizedDanglingTypes = TypeScopeAnalyzer.categorizeDanglingTypes(schema, project);
        final AssignTypesResponse.Builder responseBuilder = AssignTypesResponse.newBuilder();
        for (final Map.Entry<StructureBase, List<QName>> entry : categorizedDanglingTypes.entrySet()) {
            final AssignTypesResponse.TargetIdentifierAndTypes.Builder tiatBuilder
                    = AssignTypesResponse.TargetIdentifierAndTypes.newBuilder().setIdentifier(instanceToIdentifier.get(entry.getKey()));
            for (final QName qName : entry.getValue()) {
                tiatBuilder.addQName(de.fraunhofer.fit.ips.proto.structure.QName
                        .newBuilder()
                        .setNamespaceUri(qName.getNamespaceURI())
                        .setNcName(qName.getLocalPart())
                        .build());
            }
            responseBuilder.addTargetIdentifierAndTypes(tiatBuilder.build());
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
