package de.fraunhofer.fit.omp.testmonitor.reporting;

import de.fraunhofer.fit.omp.testmonitor.exception.MonitorException;
import de.fraunhofer.fit.omp.testmonitor.routing.FunctionInfo;
import jooq.testmonitor.enums.ValidationTargetEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.jooq.Field;
import org.xml.sax.SAXParseException;

import javax.annotation.concurrent.ThreadSafe;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@ThreadSafe
public interface Reporter {
    @RequiredArgsConstructor
    class Aspect<T> {
        @Getter final Field<T> key;
        final Function<Message, T> toValue;

        public T getValue(final Message message) {
            return toValue.apply(message);
        }
    }

    @RequiredArgsConstructor
    enum ValidationTarget {
        REQUEST(ValidationTargetEnum.request), RESPONSE(ValidationTargetEnum.response);
        @Getter final ValidationTargetEnum jooq;

        static {
            // to make sure that the jooq enum and the java enum are equivalent, we use a static initializer check
            for (final ValidationTargetEnum value : ValidationTargetEnum.values()) {
                switch (value) {
                    case request:
                    case response:
                        continue;
                    default:
                        throw new EnumConstantNotPresentException(ValidationTarget.class, value.getName());
                }
            }
        }
    }

    void messageReceived(final Exchange exchange);

    void processFunctionIdentified(final Message message, final FunctionInfo functionInfo);

    void processMissingFunctionInfo(final Message message, final String lookupKey);

    void processStartOfMessageValidation(final Message message, final String validationContent,
                                         final ValidationTarget validationTarget);

    void processStartOfFunctionValidation(final Message functionMessage, final Message request, final Message response);

    void processValidationError(final Message message, final SAXParseException exception);

    void processMissingFollowUpMessage(final Message message, final String missingElementName,
                                       final int timeOutAfterInSeconds);

    void processWarning(final MonitorException exception);

    void onException(final Exchange exchange);
}

