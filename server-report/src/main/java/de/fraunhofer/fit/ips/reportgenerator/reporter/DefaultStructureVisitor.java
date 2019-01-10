package de.fraunhofer.fit.ips.reportgenerator.reporter;

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

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
interface DefaultStructureVisitor extends StructureVisitor {
    void fallback(final StructureBase structureBase);

    default void visit(final Assertion assertion) {
        fallback(assertion);
    }

    default void visit(final Particle datatype) {
        fallback(datatype);
    }

    default void visit(final Function function) {
        fallback(function);
    }

    default void visit(final Level level) {
        fallback(level);
    }

    default void visit(final Project project) {
        fallback(project);
    }

    default void visit(final Request request) {
        fallback(request);
    }

    default void visit(final Response response) {
        fallback(response);
    }

    default void visit(final Service service) {
        fallback(service);
    }

    default void visit(final Text text) {
        fallback(text);
    }
}
