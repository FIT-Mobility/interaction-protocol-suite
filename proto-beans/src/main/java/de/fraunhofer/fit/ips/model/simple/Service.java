package de.fraunhofer.fit.ips.model.simple;

import lombok.Value;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Value
public class Service {
    @Nonnull final String name;
    @Nonnull final List<Function> functions;
}
