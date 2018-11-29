package de.fraunhofer.fit.ips.testmonitor.routing.uribased;

import de.fraunhofer.fit.ips.testmonitor.routing.MEP;
import de.fraunhofer.fit.ips.testmonitor.routing.FunctionInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
@RequiredArgsConstructor
@Builder
public class UriBasedFunctionInfo implements FunctionInfo {
    @Nonnull final QName functionElementName;
    @Nonnull final String functionName;
    @Nonnull final String serviceName;
    @Nonnull final MEP mep;
}
