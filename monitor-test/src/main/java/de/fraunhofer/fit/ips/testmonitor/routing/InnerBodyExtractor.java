package de.fraunhofer.fit.ips.testmonitor.routing;

import de.fraunhofer.fit.ips.testmonitor.exception.WrappingMonitorException;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface InnerBodyExtractor {
    default String extractInnerBody(final Message message) throws WrappingMonitorException {
        return message.getBody(String.class);
    }
}
