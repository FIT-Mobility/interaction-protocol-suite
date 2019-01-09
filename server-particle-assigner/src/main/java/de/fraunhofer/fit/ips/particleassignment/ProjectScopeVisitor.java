package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
class ProjectScopeVisitor extends ScopedStructureVisitor<ProjectScope> {
    ProjectScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                        final WrappedScope<ProjectScope> projectScope) {
        super(particleToScope, projectScope);
    }

    @Override
    public void visit(final Service service) {
        final ServiceScopeVisitor serviceScopeVisitor = new ServiceScopeVisitor(particleToScope, new WrappedScope<>(wrappedScope.scope.new ServiceScope(service), true));
        for (final StructureBase child : service.getChildren()) {
            child.accept(serviceScopeVisitor);
        }
    }
}
