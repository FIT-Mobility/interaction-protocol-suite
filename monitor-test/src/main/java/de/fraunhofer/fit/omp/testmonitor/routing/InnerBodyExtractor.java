package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface InnerBodyExtractor {
    default String extractInnerBody(final Message message) throws WrappingMonitorException {
        return message.getBody(String.class);
    }
}
