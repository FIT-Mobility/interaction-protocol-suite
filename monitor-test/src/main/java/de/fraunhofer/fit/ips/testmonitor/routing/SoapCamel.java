package de.fraunhofer.fit.ips.testmonitor.routing;

import de.fraunhofer.fit.ips.testmonitor.configuration.SoapConfiguration;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class SoapCamel extends NativeRequestResponseCamel<Reporter> {
    public SoapCamel(final SoapConfiguration configuration,
                     final Reporter reporter,
                     final ExchangeHandlerFactory exchangeHandlerFactory) {
        super(configuration.getName(), reporter, exchangeHandlerFactory,
                String.format("jetty:http://%s:%d?matchOnUriPrefix=true", configuration.getLocalHost(), configuration.getLocalPort()),
                String.format("ahc:%s?bridgeEndpoint=true", configuration.getWsURI())
        );
    }
}
