package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.Visitor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface SequenceOrChoiceOrGroupRefOrElementListVisitor extends Visitor {
    void visit(final Sequence sequence);

    void visit(final Choice choice);

    void visit(final GroupRef groupRef);

    void visit(final ElementList elementList);
}
