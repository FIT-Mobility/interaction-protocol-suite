package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.cxf.common.message.CxfMessageHelper;
import org.apache.camel.component.cxf.converter.CxfConverter;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class NativeRequestResponseCamel<R extends Reporter> extends Camel<R> {
    protected final ExchangeHandlerFactory exchangeHandlerFactory;
    protected final String fromEndpoint;
    protected final String toEndpoint;

    public NativeRequestResponseCamel(final String configurationName,
                                      final R reporter,
                                      final ExchangeHandlerFactory exchangeHandlerFactory,
                                      final String fromEndpoint,
                                      final String toEndpoint) {
        super(configurationName, reporter);
        this.exchangeHandlerFactory = exchangeHandlerFactory;
        this.fromEndpoint = fromEndpoint;
        this.toEndpoint = toEndpoint;
    }

    @Override
    protected void configureSpecificRoutes() {
        from(fromEndpoint)
                .routeId("NativeRequestResponseCamel-identification-" + configurationName)
                .process(reporter::messageReceived)
                .process(exchange -> {
                    final ExchangeHandler exchangeHandler = exchangeHandlerFactory.identifyIntent(reporter, exchange.getMessage());
                    setExchangeHandler(exchange, exchangeHandler);
                })
                // start of wiretapping block
                .wireTap("mock:validator-part1-" + configurationName)
                .process(exchange -> getExchangeHandler(exchange).addAdditionalMessage(exchange.getMessage()))
                .end()
                // end of wiretapping block
                .to(toEndpoint)
                .process(reporter::messageReceived)
                // start of wiretapping block
                .wireTap("mock:validator-part2-" + configurationName)
                .process(exchange -> getExchangeHandler(exchange).addAdditionalMessage(exchange.getMessage()))
                .end()
        // end of wiretapping block
        ;
    }
}
