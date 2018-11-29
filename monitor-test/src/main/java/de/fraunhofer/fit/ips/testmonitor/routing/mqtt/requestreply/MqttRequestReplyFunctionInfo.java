package de.fraunhofer.fit.ips.testmonitor.routing.mqtt.requestreply;

import de.fraunhofer.fit.ips.testmonitor.routing.MEP;
import de.fraunhofer.fit.ips.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.ips.testmonitor.routing.FunctionInfo;
import de.fraunhofer.fit.ips.testmonitor.topic.TopicMatcher;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
@RequiredArgsConstructor
@Builder
public class MqttRequestReplyFunctionInfo implements FunctionInfo {
    @Nonnull final QName functionElementName;
    @Nonnull final String functionName;
    @Nonnull final String serviceName;
    @Nonnull final FunctionValidator functionValidator;
    @Nonnull final TopicMatcher requestTopicValidator;
    @Nonnull final TopicMatcher replyTopicValidator;
    @Nonnull final QName requestElementName;
    @Nonnull final QName replyElementName;

    @Override
    public MEP getMep() {
        return MEP.RequestReply;
    }
}
