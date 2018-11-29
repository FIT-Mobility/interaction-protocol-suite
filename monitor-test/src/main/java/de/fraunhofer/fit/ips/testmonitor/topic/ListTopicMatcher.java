package de.fraunhofer.fit.ips.testmonitor.topic;

import de.fraunhofer.fit.ips.testmonitor.exception.IllegalTopicException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Message;

import java.util.List;
import java.util.Objects;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class ListTopicMatcher implements TopicMatcher {
    public static boolean match(final String publishTopic, final String subscriptionString) {
        final String[] publishTopicSegments = publishTopic.split("/", -1);
        final String[] subscriptionStringSegments = subscriptionString.split("/", -1);
        int i = 0;
        for (; i < subscriptionStringSegments.length; i++) {
            final String subscriptionStringSegment = subscriptionStringSegments[i];
            if ("#".equals(subscriptionStringSegment)) {
                return true;
            }
            if ("+".equals(subscriptionStringSegment)) {
                continue;
            }
            if (!Objects.equals(publishTopicSegments[i], subscriptionStringSegment)) {
                return false;
            }
        }
        // if there is more to match left in the public topic, no match
        return !(i < publishTopicSegments.length);
    }

    public static void checkValidTopic(final Message message, final String publishTopic, final List<String> validTopics)
            throws IllegalTopicException {
        assert !validTopics.isEmpty();
        if (validTopics.stream().noneMatch(vt -> match(publishTopic, vt))) {
            throw new IllegalTopicException(message, publishTopic, validTopics);
        }
    }

    final List<String> validTopics;

    public void checkValidTopic(final Message message, final String publishTopic) throws IllegalTopicException {
        checkValidTopic(message, publishTopic, validTopics);
    }
}
