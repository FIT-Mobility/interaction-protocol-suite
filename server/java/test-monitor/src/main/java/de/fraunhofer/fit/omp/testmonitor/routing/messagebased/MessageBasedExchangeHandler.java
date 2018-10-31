package de.fraunhofer.fit.omp.testmonitor.routing.messagebased;

import de.fraunhofer.fit.omp.testmonitor.routing.InnerBodyExtractor;
import de.fraunhofer.fit.omp.testmonitor.routing.RequestReplyExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
public class MessageBasedExchangeHandler extends RequestReplyExchangeHandler<MessageBasedFunctionInfo> {
    public MessageBasedExchangeHandler(
            @Nonnull final InstanceValidator dataTypeValidator,
            @Nonnull final FunctionValidator functionValidator,
            @Nonnull final MessageBasedFunctionInfo functionInfo,
            @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, ignored -> functionValidator, functionInfo, innerBodyExtractor);
    }
}
