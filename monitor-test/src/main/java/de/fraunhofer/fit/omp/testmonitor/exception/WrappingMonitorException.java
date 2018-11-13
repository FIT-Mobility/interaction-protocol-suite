package de.fraunhofer.fit.omp.testmonitor.exception;

import lombok.Getter;
import lombok.ToString;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@ToString
@Getter
public class WrappingMonitorException extends MonitorException {
    final Exception wrapped;

    @Override
    public void accept(final MonitorExceptionVisitor visitor) {
        visitor.visit(this);
    }

    public WrappingMonitorException(Message affectedMessage, Exception wrapped) {
        super(affectedMessage);
        this.wrapped = wrapped;
    }

    public WrappingMonitorException(String message, Message affectedMessage, Exception wrapped) {
        super(message, affectedMessage);
        this.wrapped = wrapped;
    }

    public WrappingMonitorException(String message, Throwable cause, Message affectedMessage,
                                    Exception wrapped) {
        super(message, cause, affectedMessage);
        this.wrapped = wrapped;
    }

    public WrappingMonitorException(Throwable cause, Message affectedMessage, Exception wrapped) {
        super(cause, affectedMessage);
        this.wrapped = wrapped;
    }

    public WrappingMonitorException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace,
                                    Message affectedMessage, Exception wrapped) {
        super(message, cause, enableSuppression, writableStackTrace, affectedMessage);
        this.wrapped = wrapped;
    }
}
