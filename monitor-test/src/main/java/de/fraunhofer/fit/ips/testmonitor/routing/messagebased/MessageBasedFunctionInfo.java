package de.fraunhofer.fit.ips.testmonitor.routing.messagebased;

import de.fraunhofer.fit.ips.testmonitor.routing.MEP;
import de.fraunhofer.fit.ips.testmonitor.routing.FunctionInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
@RequiredArgsConstructor
@Builder
public class MessageBasedFunctionInfo implements FunctionInfo {
    @Nonnull final QName functionElementName;
    @Nonnull final String functionName;
    @Nonnull final String serviceName;
    @Nonnull final QName requestElementName;
    @Nullable final QName responseElementName;

    @Override
    public MEP getMep() {
        return null == responseElementName ? MEP.Message : MEP.RequestReply;
    }
}
