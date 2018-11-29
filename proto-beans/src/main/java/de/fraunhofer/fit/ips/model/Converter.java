package de.fraunhofer.fit.ips.model;

import de.fraunhofer.fit.ips.model.simple.Assertion;
import de.fraunhofer.fit.ips.model.simple.Function;
import de.fraunhofer.fit.ips.model.simple.Project;
import de.fraunhofer.fit.ips.model.simple.Service;
import de.fraunhofer.fit.ips.proto.structure.Level;
import de.fraunhofer.fit.ips.proto.structure.Request;
import de.fraunhofer.fit.ips.proto.structure.Response;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@UtilityClass
public class Converter {
    public static Project convert(@Nonnull final de.fraunhofer.fit.ips.proto.structure.Project project)
            throws IllegalDocumentStructureException {
        final ServiceFinder serviceFinder = ServiceFinder.find(project);
        return new Project(project.getTitle(), serviceFinder.services);
    }

    @RequiredArgsConstructor
    private static class ServiceFinder {
        @Nonnull final de.fraunhofer.fit.ips.proto.structure.Project project;
        @Nonnull final List<Service> services = new ArrayList<>();

        private static ServiceFinder find(@Nonnull final de.fraunhofer.fit.ips.proto.structure.Project project)
                throws IllegalDocumentStructureException {
            return new ServiceFinder(project).find();
        }

        private ServiceFinder find() throws IllegalDocumentStructureException {
            for (final de.fraunhofer.fit.ips.proto.structure.Project.ProjectChild projectChild : project.getChildrenList()) {
                switch (projectChild.getChildCase()) {
                    case SERVICE:
                        services.add(convert(projectChild.getService()));
                        break;
                    case LEVEL:
                        find(projectChild.getLevel());
                        break;
                }
            }
            return this;
        }

        private void find(@Nonnull final Level level) throws IllegalDocumentStructureException {
            for (final Level.LevelChild levelChild : level.getChildrenList()) {
                switch (levelChild.getChildCase()) {
                    case SERVICE:
                        services.add(convert(levelChild.getService()));
                        break;
                    case LEVEL:
                        find(levelChild.getLevel());
                        break;
                    case TEXT:
                    case PARTICLE:
                        break;
                    default:
                        throw new IllegalDocumentStructureException("Illegal level-child-type in project: " + levelChild.getChildCase().name());
                }
            }
        }
    }

    private static Service convert(@Nonnull final de.fraunhofer.fit.ips.proto.structure.Service service)
            throws IllegalDocumentStructureException {
        @Nonnull final String serviceName = service.getName();
        if (serviceName.isEmpty()) {
            throw new IllegalDocumentStructureException("service without name found!");
        }
        final FunctionFinder functionFinder = FunctionFinder.find(service);
        return new Service(serviceName, functionFinder.functions);
    }

    @RequiredArgsConstructor
    private static class FunctionFinder {
        @Nonnull final de.fraunhofer.fit.ips.proto.structure.Service service;
        @Nonnull final List<Function> functions = new ArrayList<>();

        private static FunctionFinder find(@Nonnull final de.fraunhofer.fit.ips.proto.structure.Service service)
                throws IllegalDocumentStructureException {
            return new FunctionFinder(service).find();
        }

        private FunctionFinder find() throws IllegalDocumentStructureException {
            for (final de.fraunhofer.fit.ips.proto.structure.Service.ServiceChild serviceChild : service.getChildrenList()) {
                switch (serviceChild.getChildCase()) {
                    case FUNCTION:
                        functions.add(convert(serviceChild.getFunction()));
                        break;
                    case LEVEL:
                        find(serviceChild.getLevel());
                        break;
                }
            }
            return this;
        }

        private void find(@Nonnull final Level level) throws IllegalDocumentStructureException {
            for (final Level.LevelChild levelChild : level.getChildrenList()) {
                switch (levelChild.getChildCase()) {
                    case FUNCTION:
                        functions.add(convert(levelChild.getFunction()));
                        break;
                    case LEVEL:
                        find(levelChild.getLevel());
                        break;
                    case TEXT:
                    case PARTICLE:
                        break;
                    default:
                        throw new IllegalDocumentStructureException("Illegal level-child in service: " + levelChild.getChildCase().name());
                }
            }
        }
    }

    private static @Nullable
    String toNullIfEmpty(final @Nonnull String string) {
        return string.isEmpty() ? null : string;
    }

    private static Function convert(@Nonnull final de.fraunhofer.fit.ips.proto.structure.Function function)
            throws IllegalDocumentStructureException {
        @Nonnull final String functionName = function.getName();
        if (functionName.isEmpty()) {
            throw new IllegalDocumentStructureException("function without name found!");
        }
        final RequestResponseFinder requestResponseFinder = RequestResponseFinder.find(function);
        return new Function(functionName, requestResponseFinder.request, requestResponseFinder.response, requestResponseFinder.assertions);
    }

    @RequiredArgsConstructor
    private static class RequestResponseFinder {
        @Nonnull final de.fraunhofer.fit.ips.proto.structure.Function function;
        @Nullable QName request;
        @Nullable QName response;
        @Nonnull final List<Assertion> assertions = new ArrayList<>();

        private static RequestResponseFinder find(
                @Nonnull final de.fraunhofer.fit.ips.proto.structure.Function function)
                throws IllegalDocumentStructureException {
            return new RequestResponseFinder(function).find();
        }

        private RequestResponseFinder find() throws IllegalDocumentStructureException {
            for (final de.fraunhofer.fit.ips.proto.structure.Function.FunctionChild functionChild : function.getChildrenList()) {
                switch (functionChild.getChildCase()) {
                    case REQUEST:
                        setRequest(functionChild.getRequest());
                        break;
                    case RESPONSE:
                        setResponse(functionChild.getResponse());
                        break;
                    case LEVEL:
                        find(functionChild.getLevel());
                        break;
                    case ASSERTION: {
                        @Nullable final Assertion assertion = convert(functionChild.getAssertion());
                        if (null != assertion) {
                            assertions.add(assertion);
                        }
                        break;
                    }
                }
            }
            return this;
        }

        private void find(@Nonnull final Level level) throws IllegalDocumentStructureException {
            for (final Level.LevelChild levelChild : level.getChildrenList()) {
                switch (levelChild.getChildCase()) {
                    case REQUEST:
                        setRequest(levelChild.getRequest());
                        break;
                    case RESPONSE:
                        setResponse(levelChild.getResponse());
                        break;
                    case LEVEL:
                        find(levelChild.getLevel());
                        break;
                    case TEXT:
                    case PARTICLE:
                        break;
                    default:
                        throw new IllegalDocumentStructureException("Illegal level-child in function: " + levelChild.getChildCase().name());
                }
            }
        }

        private static @Nullable
        Assertion convert(
                @Nonnull final de.fraunhofer.fit.ips.proto.structure.Function.Assertion assertion) {
            final String test = assertion.getTest();
            if (test.isEmpty()) {
                return null;
            }
            return new Assertion(test, toNullIfEmpty(assertion.getXpathDefaultNamespace()));
        }

        private static QName convert(@Nonnull final de.fraunhofer.fit.ips.proto.structure.QName qName)
                throws IllegalDocumentStructureException {
            @Nonnull final String ncName = qName.getNcName();
            if (ncName.isEmpty()) {
                throw new IllegalDocumentStructureException("QName without local part: " + qName);
            }
            final String namespaceUri = qName.getNamespaceUri();
            return new QName(namespaceUri, ncName);
        }

        private void setRequest(@Nonnull final Request request)
                throws IllegalDocumentStructureException {
            if (null != this.request) {
                throw new IllegalDocumentStructureException("found two request children in function " + function.getName());
            }
            if (!request.hasQName()) {
                throw new IllegalDocumentStructureException("found request without data type qname in function " + function.getName());
            }
            this.request = convert(request.getQName());
        }

        private void setResponse(@Nonnull final Response response)
                throws IllegalDocumentStructureException {
            if (null != this.response) {
                throw new IllegalDocumentStructureException("found two request children in function " + function.getName());
            }
            if (!response.hasQName()) {
                throw new IllegalDocumentStructureException("found response without data type qname in function " + function.getName());
            }
            this.response = convert(response.getQName());
        }
    }
}
