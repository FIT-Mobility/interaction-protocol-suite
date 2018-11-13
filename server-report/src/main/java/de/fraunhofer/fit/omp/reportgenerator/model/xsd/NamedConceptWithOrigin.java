package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.Visitable;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface NamedConceptWithOrigin extends Visitable<NamedConceptWithOriginVisitor> {
    QName getName();

    Origin getOrigin();
}
