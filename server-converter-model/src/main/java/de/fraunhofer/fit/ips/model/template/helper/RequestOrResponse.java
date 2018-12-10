package de.fraunhofer.fit.ips.model.template.helper;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface RequestOrResponse extends StructureBase {
    QName getParticle();
}
