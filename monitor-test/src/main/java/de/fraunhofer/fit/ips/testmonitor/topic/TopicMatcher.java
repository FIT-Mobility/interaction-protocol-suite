package de.fraunhofer.fit.ips.testmonitor.topic;

import de.fraunhofer.fit.ips.testmonitor.exception.IllegalTopicException;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface TopicMatcher {
    void checkValidTopic(final Message message, final String publishTopic) throws IllegalTopicException;
}
