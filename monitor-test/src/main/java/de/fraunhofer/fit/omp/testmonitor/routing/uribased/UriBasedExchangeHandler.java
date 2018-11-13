package de.fraunhofer.fit.omp.testmonitor.routing.uribased;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.routing.InnerBodyExtractor;
import de.fraunhofer.fit.omp.testmonitor.routing.MEP;
import de.fraunhofer.fit.omp.testmonitor.routing.RequestReplyExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.MessageSupport;

import javax.annotation.Nonnull;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class UriBasedExchangeHandler extends RequestReplyExchangeHandler<UriBasedFunctionInfo> {
    public UriBasedExchangeHandler(
            @Nonnull final InstanceValidator dataTypeValidator,
            @Nonnull final FunctionValidator functionValidator,
            @Nonnull final UriBasedFunctionInfo functionInfo,
            @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, ignored -> functionValidator, functionInfo, innerBodyExtractor);
    }

    @Override
    public void addAdditionalMessage(Message message) throws WrappingMonitorException {
        if (messages.size() != 0) {
            if (MEP.Message == functionInfo.getMep()) {
                throw new WrappingMonitorException(message, new IllegalArgumentException("Function " + functionInfo.functionElementName + " can only handle one message!"));
            }
            // request-reply
            // TODO impl: convert inMessage to xml in case it is some different format right now
            super.addAdditionalMessage(message);
        }
        final String httpMethod = message.getHeader(Exchange.HTTP_METHOD, String.class);
        if ("GET".equals(httpMethod)) {
            final String httpQuery = message.getHeader(Exchange.HTTP_QUERY, String.class);
            // TODO impl: create inputMessage from query parameters
            final MessageSupport inputMessage = null;
            inputMessage.setExchange(message.getExchange());
            dataTypeValidator.validateSource(inputMessage, innerBodyExtractor, Reporter.ValidationTarget.REQUEST);
            messages.add(inputMessage);
        } else if ("POST".equals(httpMethod)) {
            super.addAdditionalMessage(message);
        } else {
            throw new WrappingMonitorException(message, new IllegalArgumentException("Unsupported HTTP Method: " + httpMethod));
        }
    }
}
