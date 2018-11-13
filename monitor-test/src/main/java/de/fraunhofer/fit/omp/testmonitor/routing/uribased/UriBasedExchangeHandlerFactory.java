package de.fraunhofer.fit.omp.testmonitor.routing.uribased;

import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.routing.ExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.routing.ExchangeHandlerFactory;
import de.fraunhofer.fit.omp.testmonitor.routing.FunctionlessExchangeHandler;
import de.fraunhofer.fit.omp.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class UriBasedExchangeHandlerFactory implements ExchangeHandlerFactory {
    @Nonnull protected final InstanceValidator dataTypeValidator;
    @Nonnull protected final FunctionValidator functionValidator;
    @Nonnull protected final HashMap<String, UriBasedFunctionInfo> lookup;

    @Override
    public ExchangeHandler identifyIntent(final Reporter reporter, final Message message) {
        final String httpPath = message.getHeader(Exchange.HTTP_PATH, String.class);

        // FIXME impl: determine function info from http path
        final UriBasedFunctionInfo functionInfo = lookup.get(httpPath);
        if (null == functionInfo) {
            reporter.processMissingFunctionInfo(message, httpPath);
            return new FunctionlessExchangeHandler(dataTypeValidator, this);
        }
        reporter.processFunctionIdentified(message, functionInfo);
        return new UriBasedExchangeHandler(dataTypeValidator, functionValidator, functionInfo, this);
    }
}
