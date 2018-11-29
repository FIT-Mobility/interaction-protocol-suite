package de.fraunhofer.fit.ips.testmonitor.routing.mqtt.requestreply;

import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;
import de.fraunhofer.fit.ips.testmonitor.routing.InnerBodyExtractor;
import de.fraunhofer.fit.ips.testmonitor.routing.RequestReplyExchangeHandler;

import javax.annotation.Nonnull;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class MqttRequestReplyExchangeHandler extends RequestReplyExchangeHandler<MqttRequestReplyFunctionInfo> {
    public MqttRequestReplyExchangeHandler(
            @Nonnull final InstanceValidator dataTypeValidator,
            @Nonnull final MqttRequestReplyFunctionInfo functionInfo,
            @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, MqttRequestReplyFunctionInfo::getFunctionValidator, functionInfo, innerBodyExtractor);
    }
}
