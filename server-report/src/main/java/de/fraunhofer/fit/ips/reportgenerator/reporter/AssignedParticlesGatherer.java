package de.fraunhofer.fit.ips.reportgenerator.reporter;

import de.fraunhofer.fit.ips.model.template.Assertion;
import de.fraunhofer.fit.ips.model.template.Level;
import de.fraunhofer.fit.ips.model.template.Particle;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.Request;
import de.fraunhofer.fit.ips.model.template.Response;
import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.Text;
import de.fraunhofer.fit.ips.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class AssignedParticlesGatherer implements StructureVisitor {
    private final Set<QName> assignedParticles = new HashSet<>();

    public static Set<QName> gather(final Project project) {
        final AssignedParticlesGatherer gatherer = new AssignedParticlesGatherer();
        project.accept(gatherer);
        return gatherer.assignedParticles;
    }

    @Override
    public void visit(Assertion assertion) {
        // no-op
    }

    @Override
    public void visit(Particle datatype) {
        assignedParticles.add(datatype.getName());
    }

    private void handleInnerNode(final InnerNode innerNode) {
        for (final StructureBase child : innerNode.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(de.fraunhofer.fit.ips.model.template.Function function) {
        handleInnerNode(function);
    }

    @Override
    public void visit(Level level) {
        handleInnerNode(level);
    }

    @Override
    public void visit(Project project) {
        for (final StructureBase child : project.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Request request) {
        handleInnerNode(request);
    }

    @Override
    public void visit(Response response) {
        handleInnerNode(response);
    }

    @Override
    public void visit(Service service) {
        handleInnerNode(service);
    }

    @Override
    public void visit(Text text) {
        // no-op
    }
}
