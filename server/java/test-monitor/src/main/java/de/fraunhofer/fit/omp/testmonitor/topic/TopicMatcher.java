package de.fraunhofer.fit.omp.testmonitor.topic;

import de.fraunhofer.fit.omp.testmonitor.exception.IllegalTopicException;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface TopicMatcher {
    void checkValidTopic(final Message message, final String publishTopic) throws IllegalTopicException;
}
