package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.model.ModelCamelContext;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public abstract class Camel<R extends Reporter> extends RouteBuilder {
    protected final String configurationName;
    protected final R reporter;

    protected static void setExchangeHandler(final Exchange e, final ExchangeHandler exchangeHandler) {
        e.setProperty("exchangeHandler", exchangeHandler);
    }

    protected static ExchangeHandler getExchangeHandler(final Exchange e) {
        return e.getProperty("exchangeHandler", ExchangeHandler.class);
    }

    public void registerTo(final Main camelMain) {
        camelMain.addRouteBuilder(this);
    }

    @Override
    public void configure() {
        final ModelCamelContext context = getContext();
        context.setStreamCaching(Boolean.TRUE);

        onException(Throwable.class).process(reporter::onException).handled(true);

        configureSpecificRoutes();
    }

    protected abstract void configureSpecificRoutes();
}
