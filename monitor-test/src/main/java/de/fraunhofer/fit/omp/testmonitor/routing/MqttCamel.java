package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.configuration.MqttConfiguration;
import de.fraunhofer.fit.omp.testmonitor.reporting.MqttReporter;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class MqttCamel extends Camel<MqttReporter> {
    protected final ExchangeHandlerFactory messageExchangeHandlerFactory;
    protected final ExchangeHandlerFactory requestReplyExchangeHandlerFactory;

    protected final String host; // = "tcp://127.0.0.1:1883"; // "ssl://localhost:1885";

    public MqttCamel(
            final MqttConfiguration config,
            final MqttReporter reporter,
            final ExchangeHandlerFactory messageExchangeHandlerFactory,
            final ExchangeHandlerFactory requestReplyExchangeHandlerFactory) {
        super(config.getName(), reporter);
        this.messageExchangeHandlerFactory = messageExchangeHandlerFactory;
        this.requestReplyExchangeHandlerFactory = requestReplyExchangeHandlerFactory;
        this.host = config.getMqttBrokerHostString();
    }

    @Override
    protected void configureSpecificRoutes() {
        fromF("mqtt:message-endpoint-%s?host=%s&subscribeTopicNames=Version/+/Country/#", configurationName, host)
                .routeId("mqtt-message-validation-" + configurationName)
                .process(reporter::messageReceived)
                .process(e -> messageExchangeHandlerFactory.identifyIntent(reporter, e.getMessage()).addAdditionalMessage(e.getMessage()));

        fromF("mqtt:request-reply-endpoint-%s?host=%s&subscribeTopicNames=Version/+/Inbox/#", configurationName, host)
                .routeId("mqtt-request-reply-validation-" + configurationName)
                .process(reporter::messageReceived)
                .process(e -> requestReplyExchangeHandlerFactory.identifyIntent(reporter, e.getMessage()).addAdditionalMessage(e.getMessage()));
    }
}
