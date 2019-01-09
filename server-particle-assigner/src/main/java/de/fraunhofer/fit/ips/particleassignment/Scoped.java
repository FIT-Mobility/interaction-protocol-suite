package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.template.helper.StructureBase;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
interface Scoped {
    StructureBase getStructuralElement();

    Scope getScope();

    Scoped merge(final Scoped other);
}
