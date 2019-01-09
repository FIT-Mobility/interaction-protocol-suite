package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
class ServiceScopeVisitor extends ScopedStructureVisitor<ProjectScope.ServiceScope> {
    ServiceScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                        final WrappedScope<ProjectScope.ServiceScope> wrappedScope) {
        super(particleToScope, wrappedScope);
    }

    @Override
    public void visit(final Function function) {
        final FunctionScopeVisitor functionScopeVisitor = new FunctionScopeVisitor(particleToScope, new WrappedScope<>(wrappedScope.scope.new FunctionScope(function), true));
        for (final StructureBase child : function.getChildren()) {
            child.accept(functionScopeVisitor);
        }
    }
}
