package de.fraunhofer.fit.ips.model.converter;

import com.google.common.collect.Maps;
import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import de.fraunhofer.fit.ips.model.template.Assertion;
import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.Level;
import de.fraunhofer.fit.ips.model.template.MultilingualPlaintext;
import de.fraunhofer.fit.ips.model.template.MultilingualRichtext;
import de.fraunhofer.fit.ips.model.template.Particle;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.Request;
import de.fraunhofer.fit.ips.model.template.Response;
import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.Text;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.xsd.Element;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.model.xsd.Type;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class Converter {
    final Schema schema;
    final CachingOption cache;

    public interface CachingOption {
        default <T extends StructureBase> T cacheAndReturn(final String identifier, final T instance) {
            return instance;
        }
    }

    public static Project convert(final Schema schema, final de.fraunhofer.fit.ips.proto.structure.Project from,
                                  final CachingOption cache)
            throws IllegalDocumentStructureException {
        return new Converter(schema, cache).convert(from);
    }

    public static Project convert(final Schema schema, final de.fraunhofer.fit.ips.proto.structure.Project from)
            throws IllegalDocumentStructureException {
        return convert(schema, from, new CachingOption() {
        });
    }

    public Project convert(final de.fraunhofer.fit.ips.proto.structure.Project from)
            throws IllegalDocumentStructureException {
        final ProjectChildConverterResult result = convertProjectChildren(from.getChildrenList());
        return cache.cacheAndReturn(from.getIdentifier(),
                Project.builder()
                       .title(from.getTitle())
                       .services(result.getServices())
                       .children(result.getChildren())
                       .build());
    }

    @Value(staticConstructor = "of")
    private static class ProjectChildConverterResult {
        final List<StructureBase> children;
        final List<Service> services;
    }

    private ProjectChildConverterResult convertProjectChildren(
            final List<de.fraunhofer.fit.ips.proto.structure.Project.ProjectChild> from)
            throws IllegalDocumentStructureException {
        final List<Service> services = new ArrayList<>();
        final List<StructureBase> children = new ArrayList<>();
        for (final de.fraunhofer.fit.ips.proto.structure.Project.ProjectChild child : from) {
            switch (child.getChildCase()) {
                case LEVEL:
                    children.add(convert(child.getLevel(), SpecialCaseHandler.of(
                            de.fraunhofer.fit.ips.proto.structure.Level.LevelChild.ChildCase.SERVICE,
                            levelChild -> {
                                final Service converted = convert(levelChild.getService());
                                services.add(converted);
                                return converted;
                            }
                    )));
                    break;
                case SERVICE: {
                    final Service service = convert(child.getService());
                    services.add(service);
                    children.add(service);
                    break;
                }
            }
        }
        return ProjectChildConverterResult.of(children, services);
    }

    private QName convert(de.fraunhofer.fit.ips.proto.structure.QName qname) {
        return new QName(qname.getNamespaceUri(), qname.getNcName());
    }

    private Particle convert(final de.fraunhofer.fit.ips.proto.structure.Particle particle) {
        if (!particle.hasQName()) {
            return null;
        }
        final QName qName = convert(particle.getQName());
        return new Particle(qName);
    }

    private Function convert(final de.fraunhofer.fit.ips.proto.structure.Function from)
            throws IllegalDocumentStructureException {
        final FunctionChildConverterResult result = convertFunctionChildren(from.getChildrenList());
        return cache.cacheAndReturn(from.getIdentifier(),
                Function.builder()
                        .name(from.getName())
                        .headingTitle(convert(from.getHeadingTitle()))
                        .children(result.children)
                        .request(result.request)
                        .response(result.response)
                        .assertions(result.assertions)
                        .build());
    }

    private Assertion convert(final de.fraunhofer.fit.ips.proto.structure.Function.Assertion assertion) {
        final Assertion.AssertionBuilder assertionBuilder = Assertion.builder();
        if (!assertion.getXpathDefaultNamespace().isEmpty()) {
            assertionBuilder.xpathDefaultNamespace(assertion.getXpathDefaultNamespace());
        }
        return assertionBuilder
                .test(assertion.getTest())
                .headingTitle(convert(assertion.getHeadingTitle()))
                .description(convert(assertion.getDescription()))
                .build();
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    private static class FunctionChildConverterResult {
        final List<StructureBase> children;
        @Nullable Request request;
        @Nullable Response response;
        @Nonnull final List<Assertion> assertions = new ArrayList<>();

        void setRequest(final Request request) {
            if (this.request != null) {
                throw new IllegalArgumentException();
            }
            this.request = request;
        }

        void setResponse(final Response response) {
            if (this.response != null) {
                throw new IllegalArgumentException();
            }
            this.response = response;
        }
    }

    private FunctionChildConverterResult convertFunctionChildren(
            final List<de.fraunhofer.fit.ips.proto.structure.Function.FunctionChild> from)
            throws IllegalDocumentStructureException {
        @Nonnull final List<StructureBase> children = new ArrayList<>();
        final FunctionChildConverterResult result = FunctionChildConverterResult.of(children);
        for (final de.fraunhofer.fit.ips.proto.structure.Function.FunctionChild child : from) {
            switch (child.getChildCase()) {
                case LEVEL:
                    children.add(convert(child.getLevel(), SpecialCaseHandler.of(
                            de.fraunhofer.fit.ips.proto.structure.Level.LevelChild.ChildCase.REQUEST,
                            levelChild -> {
                                final Request converted = convert(levelChild.getRequest());
                                result.setRequest(converted);
                                return converted;
                            }
                    ), SpecialCaseHandler.of(
                            de.fraunhofer.fit.ips.proto.structure.Level.LevelChild.ChildCase.RESPONSE,
                            levelChild -> {
                                final Response converted = convert(levelChild.getResponse());
                                result.setResponse(converted);
                                return converted;
                            }
                    )));
                    break;
                case REQUEST: {
                    final Request request = convert(child.getRequest());
                    result.setRequest(request);
                    children.add(request);
                    break;
                }
                case RESPONSE: {
                    final Response response = convert(child.getResponse());
                    result.setResponse(response);
                    children.add(response);
                    break;
                }
                case TEXT:
                    children.add(convert(child.getText()));
                    break;
                case PARTICLE:
                    children.add(convert(child.getParticle()));
                    break;
                case ASSERTION: {
                    final Assertion assertion = convert(child.getAssertion());
                    result.assertions.add(assertion);
                    children.add(assertion);
                    break;
                }
            }
        }
        return result;
    }

    @Value(staticConstructor = "of")
    private static class SpecialCaseHandler {
        de.fraunhofer.fit.ips.proto.structure.Level.LevelChild.ChildCase specialCase;
        ChildHandler childHandler;
    }

    interface ChildHandler {
        StructureBase handle(de.fraunhofer.fit.ips.proto.structure.Level.LevelChild child)
                throws IllegalDocumentStructureException;
    }

    private Level convert(final de.fraunhofer.fit.ips.proto.structure.Level from,
                          final SpecialCaseHandler... specialCaseHandlers) throws IllegalDocumentStructureException {
        final List<StructureBase> children = new ArrayList<>();
        child_loop:
        for (final de.fraunhofer.fit.ips.proto.structure.Level.LevelChild levelChild : from.getChildrenList()) {
            final de.fraunhofer.fit.ips.proto.structure.Level.LevelChild.ChildCase childCase = levelChild.getChildCase();
            for (final SpecialCaseHandler specialCaseHandler : specialCaseHandlers) {
                if (childCase == specialCaseHandler.specialCase) {
                    children.add(specialCaseHandler.childHandler.handle(levelChild));
                    continue child_loop;
                }
            }
            switch (childCase) {
                case TEXT:
                    children.add(convert(levelChild.getText()));
                    break;
                case LEVEL:
                    children.add(convert(levelChild.getLevel(), specialCaseHandlers));
                    break;
                case PARTICLE:
                    children.add(convert(levelChild.getParticle()));
                    break;
                case REQUEST:
                    children.add(convert(levelChild.getRequest()));
                    break;
                case RESPONSE:
                    children.add(convert(levelChild.getResponse()));
                    break;
                case SERVICE:
                    children.add(convert(levelChild.getService()));
                    break;
                case FUNCTION:
                    children.add(convert(levelChild.getFunction()));
                    break;
            }
        }
        if (!from.hasHeadingTitle()) {
            throw new IllegalArgumentException("oh oh, no title");
        }
        final MultilingualPlaintext headingTitle = convert(from.getHeadingTitle());
        return cache.cacheAndReturn(from.getIdentifier(),
                Level.builder().headingTitle(headingTitle)
                     .children(children)
                     .suppressNumbering(from.getSuppressNumbering())
                     .build());
    }

    private Text convert(final de.fraunhofer.fit.ips.proto.structure.Text text) {
        final MultilingualRichtext rtContent = convert(text.getRtContent());
        return new Text(rtContent);
    }

    private Service convert(final de.fraunhofer.fit.ips.proto.structure.Service from)
            throws IllegalDocumentStructureException {
        final ServiceChildConverterResult result = convertServiceChildren(from.getChildrenList());
        return cache.cacheAndReturn(from.getIdentifier(),
                Service.builder()
                       .name(from.getName())
                       .headingTitle(convert(from.getHeadingTitle()))
                       .functions(result.functions)
                       .children(result.children)
                       .build());
    }

    @Value(staticConstructor = "of")
    private static class ServiceChildConverterResult {
        final List<StructureBase> children;
        final List<Function> functions;
    }

    private ServiceChildConverterResult convertServiceChildren(
            List<de.fraunhofer.fit.ips.proto.structure.Service.ServiceChild> from)
            throws IllegalDocumentStructureException {
        final List<Function> functions = new ArrayList<>();
        final List<StructureBase> children = new ArrayList<>();
        for (final de.fraunhofer.fit.ips.proto.structure.Service.ServiceChild child : from) {
            switch (child.getChildCase()) {
                case LEVEL:
                    children.add(convert(child.getLevel(), SpecialCaseHandler.of(
                            de.fraunhofer.fit.ips.proto.structure.Level.LevelChild.ChildCase.FUNCTION,
                            levelChild -> {
                                final Function converted = convert(levelChild.getFunction());
                                functions.add(converted);
                                return converted;
                            }
                    )));
                    break;
                case FUNCTION: {
                    final Function function = convert(child.getFunction());
                    functions.add(function);
                    children.add(function);
                    break;
                }
                case TEXT:
                    children.add(convert(child.getText()));
                    break;
                case PARTICLE:
                    children.add(convert(child.getParticle()));
                    break;
            }
        }
        return ServiceChildConverterResult.of(children, functions);
    }

    private Request convert(final de.fraunhofer.fit.ips.proto.structure.Request request)
            throws IllegalDocumentStructureException {
        final Request.RequestBuilder builder = Request.builder();
        if (request.hasHeadingTitle()) {
            builder.headingTitle(convert(request.getHeadingTitle()));
        }
        if (request.hasQName()) {
            builder.datatype(determineDataTypeName(convert(request.getQName())));
        }
        builder.children(convertRequestResponseChildren(request.getChildrenList()));
        return cache.cacheAndReturn(request.getIdentifier(), builder.build());
    }

    private List<StructureBase> convertRequestResponseChildren(
            final List<de.fraunhofer.fit.ips.proto.structure.Request.RequestResponseChild> from)
            throws IllegalDocumentStructureException {
        final List<StructureBase> children = new ArrayList<>();
        for (final de.fraunhofer.fit.ips.proto.structure.Request.RequestResponseChild child : from) {
            switch (child.getChildCase()) {
                case LEVEL:
                    children.add(convert(child.getLevel()));
                    break;
                case TEXT:
                    children.add(convert(child.getText()));
                    break;
                case PARTICLE:
                    children.add(convert(child.getParticle()));
                    break;
            }
        }
        return children;
    }

    private MultilingualRichtext convert(
            final de.fraunhofer.fit.ips.proto.structure.MultilingualRichtext multilingualRichtext) {
        return new MultilingualRichtext(Maps.transformValues(multilingualRichtext.getLanguageToRichtextMap(), NestedListFixer::fixLists));
    }

    private MultilingualPlaintext convert(
            final de.fraunhofer.fit.ips.proto.structure.MultilingualPlaintext multilingualPlaintext) {
        return new MultilingualPlaintext(multilingualPlaintext.getLanguageToPlaintextMap());
    }

    private Response convert(final de.fraunhofer.fit.ips.proto.structure.Response response)
            throws IllegalDocumentStructureException {
        final Response.ResponseBuilder builder = Response.builder();
        if (response.hasHeadingTitle()) {
            builder.headingTitle(convert(response.getHeadingTitle()));
        }
        if (response.hasQName()) {
            builder.datatype(determineDataTypeName(convert(response.getQName())));
        }
        builder.children(convertRequestResponseChildren(response.getChildrenList()));
        return cache.cacheAndReturn(response.getIdentifier(), builder.build());
    }

    private QName determineDataTypeName(final QName rrQname) throws IllegalDocumentStructureException {
        @Nullable final NamedConceptWithOrigin namedConceptWithOrigin = schema.getConcepts().get(rrQname);
        if (namedConceptWithOrigin instanceof Element) {
            final QName dataTypeName = ((Element) namedConceptWithOrigin).getDataType();
            final NamedConceptWithOrigin dataType = schema.getConcepts().get(dataTypeName);
            if (dataType instanceof Type) {
                return dataTypeName;
            }
        } else if (namedConceptWithOrigin instanceof Type) {
            return rrQname;
        }
        throw new IllegalDocumentStructureException("qname could not be used to identify type of concept: " + rrQname.toString());
    }
}
