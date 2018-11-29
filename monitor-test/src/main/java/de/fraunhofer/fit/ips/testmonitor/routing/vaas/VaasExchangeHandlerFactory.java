package de.fraunhofer.fit.ips.testmonitor.routing.vaas;

import com.google.common.collect.Iterables;
import de.fraunhofer.fit.ips.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.ips.testmonitor.routing.FunctionlessExchangeHandler;
import de.fraunhofer.fit.ips.testmonitor.routing.messagebased.MessageBasedFunctionInfo;
import de.fraunhofer.fit.ips.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;
import de.fraunhofer.fit.ips.testmonitor.routing.ExchangeHandler;
import de.fraunhofer.fit.ips.testmonitor.routing.ExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.RequestReplyExchangeHandler;
import de.fraunhofer.fit.ips.vaas.VaasConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Message;
import org.apache.camel.support.DefaultTimeoutMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
@NotThreadSafe
public class VaasExchangeHandlerFactory implements ExchangeHandlerFactory {
    @Nonnull protected final InstanceValidator dataTypeValidator;
    @Nonnull protected final FunctionValidator functionValidator;
    @Nonnull protected final HashMap<QName, MessageBasedFunctionInfo> lookup;

    private static final int PURGE_POLL_INTERVAL_MILLIS = 20_000;
    private static final int TIMEOUT_MILLIS = 60_000;

    private final DefaultTimeoutMap<String, RequestReplyExchangeHandler<MessageBasedFunctionInfo>> pendingRequests;

    public VaasExchangeHandlerFactory(
            @Nonnull final Reporter reporter,
            @Nonnull final InstanceValidator dataTypeValidator,
            @Nonnull final FunctionValidator functionValidator,
            @Nonnull final HashMap<QName, MessageBasedFunctionInfo> lookup) {
        this.dataTypeValidator = dataTypeValidator;
        this.functionValidator = functionValidator;
        this.lookup = lookup;
        this.pendingRequests = new DefaultTimeoutMap<String, RequestReplyExchangeHandler<MessageBasedFunctionInfo>>(
                new ScheduledThreadPoolExecutor(1), PURGE_POLL_INTERVAL_MILLIS) {
            @Override
            public boolean onEviction(final String exchangeId,
                                      final RequestReplyExchangeHandler<MessageBasedFunctionInfo> exchangeHandler) {
                log.warn("Exchange Handler waiting for further messages timed out after {}s. ExchangeID: {}; Function: {}",
                        TIMEOUT_MILLIS / 1000, exchangeId, exchangeHandler.getFunctionInfo().getFunctionElementName());
                reporter.processMissingFollowUpMessage(
                        Iterables.getLast(exchangeHandler.getMessages(), null),
                        // even though the response element name should not be null in case we are
                        // waiting for a response, make the toString null-safe
                        Objects.toString(exchangeHandler.getFunctionInfo().getResponseElementName()),
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

    private static final Set<String> VALID_MESSAGE_TYPES = new HashSet<>(Arrays.asList(
            VaasConstants.HTTP_HEADER_MESSAGE_TYPE_REQUEST,
            VaasConstants.HTTP_HEADER_MESSAGE_TYPE_RESPONSE
    ));

    @Override
    public ExchangeHandler identifyIntent(final Reporter reporter, final Message message)
            throws WrappingMonitorException {
        final @Nullable String exchangeId = message.getHeader(VaasConstants.HTTP_HEADER_EXCHANGE_ID, String.class);
        final @Nullable String messageType = message.getHeader(VaasConstants.HTTP_HEADER_MESSAGE_TYPE, String.class);

        if (null == exchangeId) {
            throw new WrappingMonitorException(message, new IllegalArgumentException("exchange id header not set!"));
        }
        if (!VALID_MESSAGE_TYPES.contains(messageType)) {
            throw new WrappingMonitorException(message, new IllegalArgumentException("message type header not recognized!"));
        }

        if (VaasConstants.HTTP_HEADER_MESSAGE_TYPE_RESPONSE.equals(messageType)) {
            final RequestReplyExchangeHandler<MessageBasedFunctionInfo> exchangeHandler = pendingRequests.remove(exchangeId);
            if (null == exchangeHandler) {
                final QName elementName = ExchangeHandler.determineRootNodeQName(message, this);
                reporter.processMissingFunctionInfo(message, elementName.toString());
                return new FunctionlessExchangeHandler(dataTypeValidator, this);
            }
            return exchangeHandler;
        }

        final QName elementName = ExchangeHandler.determineRootNodeQName(message, this);
        final MessageBasedFunctionInfo functionInfo = lookup.get(elementName);
        if (null == functionInfo) {
            reporter.processMissingFunctionInfo(message, elementName.toString());
            return new FunctionlessExchangeHandler(dataTypeValidator, this);
        }
        reporter.processFunctionIdentified(message, functionInfo);
        final RequestReplyExchangeHandler<MessageBasedFunctionInfo> exchangeHandler
                = new RequestReplyExchangeHandler<>(dataTypeValidator, ignored -> functionValidator, functionInfo, this);
        pendingRequests.put(exchangeId, exchangeHandler, TIMEOUT_MILLIS);
        return exchangeHandler;
    }
}
