package de.fraunhofer.fit.omp.testmonitor.exception;

import lombok.Getter;
import lombok.ToString;
import org.apache.camel.Message;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@ToString
@Getter
public class MissingFunctionInfoException extends MonitorException {
    final QName elementName;

    @Override
    public void accept(final MonitorExceptionVisitor visitor) {
        visitor.visit(this);
    }

    public MissingFunctionInfoException(Message affectedMessage, QName elementName) {
        super(affectedMessage);
        this.elementName = elementName;
    }

    public MissingFunctionInfoException(String message, Message affectedMessage,
                                        QName elementName) {
        super(message, affectedMessage);
        this.elementName = elementName;
    }

    public MissingFunctionInfoException(String message, Throwable cause, Message affectedMessage,
                                        QName elementName) {
        super(message, cause, affectedMessage);
        this.elementName = elementName;
    }

    public MissingFunctionInfoException(Throwable cause, Message affectedMessage,
                                        QName elementName) {
        super(cause, affectedMessage);
        this.elementName = elementName;
    }

    public MissingFunctionInfoException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace, Message affectedMessage,
                                        QName elementName) {
        super(message, cause, enableSuppression, writableStackTrace, affectedMessage);
        this.elementName = elementName;
    }
}
