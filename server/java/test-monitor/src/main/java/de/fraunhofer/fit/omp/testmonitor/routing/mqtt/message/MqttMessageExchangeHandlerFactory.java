package de.fraunhofer.fit.omp.testmonitor.routing.mqtt.message;

import de.fraunhofer.fit.omp.testmonitor.exception.IllegalTopicException;
import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.routing.ExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.routing.ExchangeHandlerFactory;
import de.fraunhofer.fit.omp.testmonitor.routing.FunctionlessExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Message;
import org.apache.camel.component.mqtt.MQTTConfiguration;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class MqttMessageExchangeHandlerFactory implements ExchangeHandlerFactory {
    @Nonnull protected final InstanceValidator dataTypeValidator;
    @Nonnull protected final Function<QName, MqttMessageFunctionInfo> functionIdentifier;

    @Override
    public ExchangeHandler identifyIntent(final Reporter reporter, final Message message)
            throws WrappingMonitorException {
        final QName requestName = ExchangeHandler.determineRootNodeQName(message, this);
        final MqttMessageFunctionInfo functionInfo = functionIdentifier.apply(requestName);
        if (null == functionInfo) {
            reporter.processMissingFunctionInfo(message, requestName.toString());
            return new FunctionlessExchangeHandler(dataTypeValidator, this);
        }
        reporter.processFunctionIdentified(message, functionInfo);
        final String topic = message.getHeader(MQTTConfiguration.MQTT_SUBSCRIBE_TOPIC, String.class);
        try {
            functionInfo.getTopicValidator().checkValidTopic(message, topic);
        } catch (final IllegalTopicException e) {
            reporter.processWarning(e);
        }
        return new MqttMessageExchangeHandler(dataTypeValidator, functionInfo, this);
    }
}
