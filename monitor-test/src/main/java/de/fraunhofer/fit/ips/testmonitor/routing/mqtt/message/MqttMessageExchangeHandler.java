package de.fraunhofer.fit.ips.testmonitor.routing.mqtt.message;

import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;
import de.fraunhofer.fit.ips.testmonitor.routing.InnerBodyExtractor;
import de.fraunhofer.fit.ips.testmonitor.routing.MessageExchangeHandler;

import javax.annotation.Nonnull;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class MqttMessageExchangeHandler extends MessageExchangeHandler<MqttMessageFunctionInfo> {
    public MqttMessageExchangeHandler(
            @Nonnull final InstanceValidator dataTypeValidator,
            @Nonnull final MqttMessageFunctionInfo functionInfo,
            @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, functionInfo, innerBodyExtractor);
    }
}
