package de.fraunhofer.fit.omp.testmonitor.routing.mqtt.message;

import de.fraunhofer.fit.omp.testmonitor.routing.InnerBodyExtractor;
import de.fraunhofer.fit.omp.testmonitor.routing.MessageExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;

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
