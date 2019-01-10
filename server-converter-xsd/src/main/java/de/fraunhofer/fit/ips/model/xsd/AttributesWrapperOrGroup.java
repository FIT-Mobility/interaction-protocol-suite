package de.fraunhofer.fit.ips.model.xsd;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.LinkedHashMap;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface AttributesWrapperOrGroup {
    @Nonnull
    LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> getAttributes();

    @Nullable
    Attributes.AnyAttribute getAnyAttribute();
}
