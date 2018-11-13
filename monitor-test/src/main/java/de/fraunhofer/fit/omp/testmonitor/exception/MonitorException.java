package de.fraunhofer.fit.omp.testmonitor.exception;

import de.fraunhofer.fit.omp.testmonitor.Visitable;
import lombok.Getter;
import org.apache.camel.Message;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
public abstract class MonitorException extends Exception implements Visitable<MonitorExceptionVisitor> {
    final Message affectedMessage;

    public MonitorException(Message affectedMessage) {
        this.affectedMessage = affectedMessage;
    }

    public MonitorException(String message, Message affectedMessage) {
        super(message);
        this.affectedMessage = affectedMessage;
    }

    public MonitorException(String message, Throwable cause, Message affectedMessage) {
        super(message, cause);
        this.affectedMessage = affectedMessage;
    }

    public MonitorException(Throwable cause, Message affectedMessage) {
        super(cause);
        this.affectedMessage = affectedMessage;
    }

    public MonitorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                            Message affectedMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.affectedMessage = affectedMessage;
    }
}
