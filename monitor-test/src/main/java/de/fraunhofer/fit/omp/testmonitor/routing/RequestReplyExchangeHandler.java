package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import org.apache.camel.Message;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class RequestReplyExchangeHandler<FI extends FunctionInfo> extends ExchangeHandler<FI> {
    @Nonnull protected final Function<FI, FunctionValidator> functionValidatorGetter;

    public RequestReplyExchangeHandler(@Nonnull final InstanceValidator dataTypeValidator,
                                       @Nonnull final Function<FI, FunctionValidator> functionValidatorGetter,
                                       @Nonnull final FI functionInfo,
                                       @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, functionInfo, innerBodyExtractor);
        this.functionValidatorGetter = functionValidatorGetter;
    }

    @Override
    public Reporter.ValidationTarget determineMessageType(Message message) {
        return messages.isEmpty() ? Reporter.ValidationTarget.REQUEST : Reporter.ValidationTarget.RESPONSE;
    }

    @Override
    public void addAdditionalMessage(final Message message) throws WrappingMonitorException {
        super.addAdditionalMessage(message);
        final int numMessages = messages.size();
        if (2 == numMessages) {
            functionValidatorGetter.apply(functionInfo)
                                   .validateFunction(functionInfo.getFunctionElementName(),
                                           messages.get(numMessages - 2),
                                           messages.get(numMessages - 1),
                                           innerBodyExtractor);
        } else if (2 < numMessages) {
            throw new WrappingMonitorException(message, new IllegalArgumentException(getClass().getName() + " cannot cope with more than two messages!"));
        }
    }
}
