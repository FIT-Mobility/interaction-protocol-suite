package de.fraunhofer.fit.ips.testmonitor.routing;


import de.fraunhofer.fit.ips.testmonitor.configuration.HttpConfiguration;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class HttpCamel extends NativeRequestResponseCamel<Reporter> {
    public HttpCamel(final HttpConfiguration httpConfiguration,
                     final Reporter reporter,
                     final ExchangeHandlerFactory exchangeHandlerFactory) {
        super(httpConfiguration.getName(), reporter, exchangeHandlerFactory,
                String.format("jetty:http://%s:%d?matchOnUriPrefix=true", httpConfiguration.getLocalHost(), httpConfiguration.getLocalPort()),
                String.format("ahc:%s?bridgeEndpoint=true", httpConfiguration.getWsURI())
        );
    }
}
