package de.fraunhofer.fit.ips.testmonitor.routing;

import de.fraunhofer.fit.ips.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface ExchangeHandlerFactory extends InnerBodyExtractor {
    ExchangeHandler identifyIntent(final Reporter reporter, final Message message) throws WrappingMonitorException;
}
