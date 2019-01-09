package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.template.Assertion;
import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.Level;
import de.fraunhofer.fit.ips.model.template.Particle;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.Request;
import de.fraunhofer.fit.ips.model.template.Response;
import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.Text;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class ScopedStructureVisitor<S extends Scoped> implements StructureVisitor {
    final Map<QName, WrappedScope<? extends Scoped>> particleToScope;
    final WrappedScope<S> wrappedScope;

    void registerExplicit(final QName particleName) {
        particleToScope.put(particleName, wrappedScope);
    }

    void registerImplicit(final QName particleName) {
        particleToScope.merge(particleName, new WrappedScope<>(wrappedScope.scope, false), WrappedScope::merge);
    }

    @Override
    public void visit(final Level level) {
        for (final StructureBase child : level.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(final Particle particle) {
        registerExplicit(particle.getName());
    }

    @Override
    public void visit(final Assertion assertion) {
    }

    @Override
    public void visit(final Function function) {
    }

    @Override
    public void visit(final Project project) {
    }

    @Override
    public void visit(final Request request) {
    }

    @Override
    public void visit(final Response response) {
    }

    @Override
    public void visit(final Service service) {
    }

    @Override
    public void visit(final Text text) {
    }
}
