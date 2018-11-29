package de.fraunhofer.fit.ips.testmonitor.routing.mqtt.message;

import de.fraunhofer.fit.ips.testmonitor.routing.FunctionInfo;
import de.fraunhofer.fit.ips.testmonitor.routing.MEP;
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
public class MqttMessageFunctionInfo implements FunctionInfo {
    @Nonnull final QName functionElementName;
    @Nonnull final String functionName;
    @Nonnull final String serviceName;
    @Nonnull final TopicMatcher topicValidator;

    @Override
    public MEP getMep() {
        return MEP.Message;
    }
}
