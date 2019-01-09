package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.helper.RequestOrResponse;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
class ProjectScope implements Scoped {
    final Project project;

    @Override
    public Project getStructuralElement() {
        return project;
    }

    @Override
    public Scope getScope() {
        return Scope.PROJECT;
    }

    @Override
    public Scoped merge(final Scoped other) {
        return this;
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    @EqualsAndHashCode
    class ServiceScope implements Scoped {
        final Service service;

        @Override
        public Service getStructuralElement() {
            return service;
        }

        @Override
        public Scope getScope() {
            return Scope.SERVICE;
        }

        @Override
        public Scoped merge(final Scoped other) {
            switch (other.getScope()) {
                case PROJECT:
                    return other;
                case SERVICE:
                    return ((ServiceScope) other).service == service ? this : new ProjectScope(project);
                case FUNCTION:
                    return ((ServiceScope.FunctionScope) other).getService() == service ? this : new ProjectScope(project);
                case REQUEST_OR_RESPONSE:
                    return ((ServiceScope.FunctionScope.RequestOrResponseScope) other).getService() == service ? this : new ProjectScope(project);
            }
            throw new IllegalStateException("unknown scope enum value");
        }

        @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
        @EqualsAndHashCode
        class FunctionScope implements Scoped {
            final Function function;

            @Override
            public Function getStructuralElement() {
                return function;
            }

            @Override
            public Scope getScope() {
                return Scope.FUNCTION;
            }

            Service getService() {
                return service;
            }

            @Override
            public Scoped merge(final Scoped other) {
                switch (other.getScope()) {
                    case FUNCTION:
                        return ((ServiceScope.FunctionScope) other).function == function ? this : new ServiceScope(service).merge(other);
                    case REQUEST_OR_RESPONSE:
                        return ((ServiceScope.FunctionScope.RequestOrResponseScope) other).getFunction() == function ? this : new ServiceScope(service).merge(other);
                }
                return other.merge(this);
            }

            @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
            @EqualsAndHashCode
            class RequestOrResponseScope implements Scoped {
                final RequestOrResponse requestOrResponse;

                @Override
                public RequestOrResponse getStructuralElement() {
                    return requestOrResponse;
                }

                @Override
                public Scope getScope() {
                    return Scope.REQUEST_OR_RESPONSE;
                }

                Service getService() {
                    return service;
                }

                Function getFunction() {
                    return function;
                }

                @Override
                public Scoped merge(final Scoped other) {
                    if (Scope.REQUEST_OR_RESPONSE == other.getScope()) {
                        return ((ServiceScope.FunctionScope.RequestOrResponseScope) other).requestOrResponse == requestOrResponse ? this : new ServiceScope.FunctionScope(function).merge(other);
                    }
                    return other.merge(this);
                }
            }
        }
    }
}
