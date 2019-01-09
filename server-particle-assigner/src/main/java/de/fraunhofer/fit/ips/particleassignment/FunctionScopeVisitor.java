package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.template.Request;
import de.fraunhofer.fit.ips.model.template.Response;
import de.fraunhofer.fit.ips.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.model.template.helper.RequestOrResponse;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
class FunctionScopeVisitor extends ScopedStructureVisitor<ProjectScope.ServiceScope.FunctionScope> {
    FunctionScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                         final WrappedScope<ProjectScope.ServiceScope.FunctionScope> wrappedScope) {
        super(particleToScope, wrappedScope);
    }

    private <RR extends RequestOrResponse & InnerNode> void handle(final RR requestOrResponse) {
        final RequestOrResponseScopeVisitor requestOrResponseScopeVisitor = new RequestOrResponseScopeVisitor(particleToScope, new WrappedScope<>(wrappedScope.scope.new RequestOrResponseScope(requestOrResponse), true));
        for (final StructureBase child : requestOrResponse.getChildren()) {
            child.accept(requestOrResponseScopeVisitor);
        }
    }

    @Override
    public void visit(final Request request) {
        handle(request);
    }

    @Override
    public void visit(final Response response) {
        handle(response);
    }
}
