package de.fraunhofer.fit.omp.testmonitor.exception;

import lombok.Getter;
import lombok.ToString;
import org.apache.camel.Message;

import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@ToString
@Getter
public class IllegalTopicException extends MonitorException {
    final String illegalTopic;
    final List<String> legalTopics;

    @Override
    public void accept(final MonitorExceptionVisitor visitor) {
        visitor.visit(this);
    }

    public IllegalTopicException(Message affectedMessage, String illegalTopic,
                                 List<String> legalTopics) {
        super(affectedMessage);
        this.illegalTopic = illegalTopic;
        this.legalTopics = legalTopics;
    }

    public IllegalTopicException(String message, Message affectedMessage, String illegalTopic,
                                 List<String> legalTopics) {
        super(message, affectedMessage);
        this.illegalTopic = illegalTopic;
        this.legalTopics = legalTopics;
    }

    public IllegalTopicException(String message, Throwable cause, Message affectedMessage,
                                 String illegalTopic, List<String> legalTopics) {
        super(message, cause, affectedMessage);
        this.illegalTopic = illegalTopic;
        this.legalTopics = legalTopics;
    }

    public IllegalTopicException(Throwable cause, Message affectedMessage, String illegalTopic,
                                 List<String> legalTopics) {
        super(cause, affectedMessage);
        this.illegalTopic = illegalTopic;
        this.legalTopics = legalTopics;
    }

    public IllegalTopicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                                 Message affectedMessage, String illegalTopic,
                                 List<String> legalTopics) {
        super(message, cause, enableSuppression, writableStackTrace, affectedMessage);
        this.illegalTopic = illegalTopic;
        this.legalTopics = legalTopics;
    }
}