package de.fraunhofer.fit.ips.model.simple;

import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Value
public class Assertion {
    @Nonnull final String test;
    @Nullable final String xpathDefaultNamespace;
}
