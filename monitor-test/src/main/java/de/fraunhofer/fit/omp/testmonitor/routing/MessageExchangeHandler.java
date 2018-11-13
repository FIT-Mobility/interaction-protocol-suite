package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import org.apache.camel.Message;

import javax.annotation.Nonnull;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class MessageExchangeHandler<FI extends FunctionInfo> extends ExchangeHandler<FI> {
    public MessageExchangeHandler(
            @Nonnull final InstanceValidator dataTypeValidator,
            @Nonnull final FI functionInfo,
            @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, functionInfo, innerBodyExtractor);
    }

    @Override
    public void addAdditionalMessage(final Message message) throws WrappingMonitorException {
        if (messages.size() != 0) {
            throw new WrappingMonitorException(message,
                    new IllegalArgumentException("You seem to have used a " + getClass().getName() + " for request-reply interaction!"));
        }
        super.addAdditionalMessage(message);
    }
}
