package de.fraunhofer.fit.omp.testmonitor.routing.mqtt.requestreply;

import de.fraunhofer.fit.omp.testmonitor.routing.InnerBodyExtractor;
import de.fraunhofer.fit.omp.testmonitor.routing.RequestReplyExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;

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
