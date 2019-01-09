package de.fraunhofer.fit.ips.particleassignment;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
class RequestOrResponseScopeVisitor extends ScopedStructureVisitor<ProjectScope.ServiceScope.FunctionScope.RequestOrResponseScope> {
    RequestOrResponseScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                                  final WrappedScope<ProjectScope.ServiceScope.FunctionScope.RequestOrResponseScope> wrappedScope) {
        super(particleToScope, wrappedScope);
        registerImplicit(wrappedScope.getScope().getStructuralElement().getParticle());
    }
}
