package de.fraunhofer.fit.ips.testmonitor.routing.mqtt.requestreply;

import com.google.common.collect.Iterables;
import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;
import de.fraunhofer.fit.ips.testmonitor.Constants;
import de.fraunhofer.fit.ips.testmonitor.exception.IllegalTopicException;
import de.fraunhofer.fit.ips.testmonitor.exception.NoCorrelationIdException;
import de.fraunhofer.fit.ips.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.ips.testmonitor.routing.ExchangeHandler;
import de.fraunhofer.fit.ips.testmonitor.routing.ExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.FunctionlessExchangeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Message;
import org.apache.camel.component.mqtt.MQTTConfiguration;
import org.apache.camel.support.DefaultTimeoutMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.namespace.QName;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
@NotThreadSafe
public class MqttRequestReplyExchangeHandlerFactory implements ExchangeHandlerFactory, AutoCloseable {
    private static final int PURGE_POLL_INTERVAL_MILLIS = 20_000;
    private static final int TIMEOUT_MILLIS = 3600_000;

    @Nonnull protected final InstanceValidator dataTypeValidator;
    @Nonnull protected final Function<QName, MqttRequestReplyFunctionInfo> functionIdentifier;

    private final DefaultTimeoutMap<String, MqttRequestReplyExchangeHandler> pendingRequests;

    @java.beans.ConstructorProperties({"dataTypeValidator", "functionIdentifier"})
    public MqttRequestReplyExchangeHandlerFactory(@Nonnull final Reporter reporter,
                                                  @Nonnull final InstanceValidator dataTypeValidator,
                                                  @Nonnull final Function<QName, MqttRequestReplyFunctionInfo> functionIdentifier) {
        this.dataTypeValidator = dataTypeValidator;
        this.functionIdentifier = functionIdentifier;
        this.pendingRequests = new DefaultTimeoutMap<String, MqttRequestReplyExchangeHandler>(new ScheduledThreadPoolExecutor(1), PURGE_POLL_INTERVAL_MILLIS) {
            @Override
            public boolean onEviction(final String correlationId,
                                      final MqttRequestReplyExchangeHandler exchangeHandler) {
                log.warn("Exchange Handler waiting for further messages timed out after {}s. CorrelationID: {}; Function: {}",
                        TIMEOUT_MILLIS / 1000, correlationId, exchangeHandler.getFunctionInfo().getFunctionElementName());
                reporter.processMissingFollowUpMessage(
                        Iterables.getLast(exchangeHandler.getMessages(), null),
                        exchangeHandler.getFunctionInfo().getReplyElementName().toString(),
                        TIMEOUT_MILLIS / 1000
                );
                return true;
            }
        };
        try {
            this.pendingRequests.start();
        } catch (final Exception e) {
            log.error(Objects.toString(e));
        }
    }

    @Override
    public ExchangeHandler identifyIntent(final Reporter reporter, final Message message)
            throws WrappingMonitorException {
        // topic -> correlation id
        final String topic = message.getHeader(MQTTConfiguration.MQTT_SUBSCRIBE_TOPIC, String.class);
        final String correlationId;
        {
            String correlationId1;
            try {
                correlationId1 = determineCorrelationId(message, topic);
                message.setHeader(Constants.CORRELATION_ID, correlationId1);
            } catch (final NoCorrelationIdException e) {
                reporter.processWarning(e);
                correlationId1 = null;
            }
            correlationId = correlationId1;
        }

        if (correlationId != null) {
            final MqttRequestReplyExchangeHandler cachedExchangeHandler = pendingRequests.remove(correlationId);

            if (null != cachedExchangeHandler) {
                try {
                    cachedExchangeHandler.getFunctionInfo().getReplyTopicValidator().checkValidTopic(message, topic);
                } catch (final IllegalTopicException e) {
                    reporter.processWarning(e);
                }
                return cachedExchangeHandler;
            }
        }

        // data-type -> functionInfo
        final QName requestName = ExchangeHandler.determineRootNodeQName(message, this);

        final MqttRequestReplyFunctionInfo functionInfo = functionIdentifier.apply(requestName);

        if (null == functionInfo) {
            reporter.processMissingFunctionInfo(message, requestName.toString());
            return new FunctionlessExchangeHandler(dataTypeValidator, this);
        }
        reporter.processFunctionIdentified(message, functionInfo);
        try {
            functionInfo.getRequestTopicValidator().checkValidTopic(message, topic);
        } catch (final IllegalTopicException e) {
            reporter.processWarning(e);
        }

        final MqttRequestReplyExchangeHandler exchangeHandler = new MqttRequestReplyExchangeHandler(dataTypeValidator, functionInfo, this);
        if (correlationId != null) {
            pendingRequests.put(correlationId, exchangeHandler, TIMEOUT_MILLIS);
        }
        return exchangeHandler;
    }

    public static String determineCorrelationId(final Message exchange, final String topic)
            throws NoCorrelationIdException {
        final String[] topicParts = topic.split("/", -1);
        for (int i = 0, topicPartsLength = topicParts.length; i < topicPartsLength; i++) {
            String topicPart = topicParts[i];
            if ("CorrelationId".equals(topicPart)) {
                return topicParts[i + 1];
            }
        }
        throw new NoCorrelationIdException(exchange, topic);
    }

    @Override
    public void close() throws Exception {
        pendingRequests.stop();
    }
}
