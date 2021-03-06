package de.fraunhofer.fit.ips.model.xsd;

import de.fraunhofer.fit.ips.Visitable;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface NamedConceptWithOrigin extends Visitable<NamedConceptWithOriginVisitor> {
    QName getName();

    Origin getOrigin();
}
