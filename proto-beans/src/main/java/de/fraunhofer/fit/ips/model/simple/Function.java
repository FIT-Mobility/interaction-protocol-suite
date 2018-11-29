package de.fraunhofer.fit.ips.model.simple;

import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Value
public class Function {
    @Nonnull final String name;
    @Nullable final QName requestType;
    @Nullable final QName responseType;
    @Nonnull final List<Assertion> assertions;
}
