package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface ExchangeHandlerFactory extends InnerBodyExtractor {
    ExchangeHandler identifyIntent(final Reporter reporter, final Message message) throws WrappingMonitorException;
}
