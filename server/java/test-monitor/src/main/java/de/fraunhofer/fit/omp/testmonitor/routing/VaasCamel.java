package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.configuration.VaasConfiguration;
import de.fraunhofer.fit.omp.testmonitor.reporting.VaasReporter;
import de.fraunhofer.fit.omp.testmonitor.routing.vaas.VaasExchangeHandlerFactory;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class VaasCamel extends Camel<VaasReporter> {
    protected final VaasExchangeHandlerFactory exchangeHandlerFactory;
    protected final String fromEndpoint;

    public VaasCamel(final VaasConfiguration configuration,
                     final VaasReporter reporter,
                     final VaasExchangeHandlerFactory exchangeHandlerFactory) {
        super(configuration.getName(), reporter);
        this.exchangeHandlerFactory = exchangeHandlerFactory;
        this.fromEndpoint = String.format("jetty:http://%s:%d?matchOnUriPrefix=true", configuration.getLocalHost(), configuration.getLocalPort());
    }

    @Override
    protected void configureSpecificRoutes() {
        from(fromEndpoint)
                .routeId("vaas-" + configurationName)
                .process(reporter::messageReceived)
                .process(e -> exchangeHandlerFactory.identifyIntent(reporter, e.getMessage()).addAdditionalMessage(e.getMessage()));
    }
}
