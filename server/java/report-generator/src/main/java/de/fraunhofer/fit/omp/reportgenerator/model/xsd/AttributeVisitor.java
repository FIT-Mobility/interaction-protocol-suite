package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.Visitor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface AttributeVisitor extends Visitor {
    void visit(final Attributes.LocalAttribute localAttribute);

    void visit(final Attributes.GlobalAttribute globalAttribute);

    void visit(final Attributes.AttributeGroup attributeGroup);
}
