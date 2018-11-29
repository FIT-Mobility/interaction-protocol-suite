package de.fraunhofer.fit.ips.testmonitor.exception;

import lombok.Getter;
import lombok.ToString;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
@ToString
public class NoCorrelationIdException extends MonitorException {
    final String topic;

    @Override
    public void accept(final MonitorExceptionVisitor visitor) {
        visitor.visit(this);
    }

    public NoCorrelationIdException(final Message affectedMessage, final String topic) {
        super(affectedMessage);
        this.topic = topic;
    }

    public NoCorrelationIdException(final String message, final Message affectedMessage, final String topic) {
        super(message, affectedMessage);
        this.topic = topic;
    }

    public NoCorrelationIdException(final String message, Throwable cause, final Message affectedMessage,
                                    final String topic) {
        super(message, cause, affectedMessage);
        this.topic = topic;
    }

    public NoCorrelationIdException(final Throwable cause, final Message affectedMessage, final String topic) {
        super(cause, affectedMessage);
        this.topic = topic;
    }

    public NoCorrelationIdException(final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace, final Message affectedMessage,
                                    final String topic) {
        super(message, cause, enableSuppression, writableStackTrace, affectedMessage);
        this.topic = topic;
    }
}
