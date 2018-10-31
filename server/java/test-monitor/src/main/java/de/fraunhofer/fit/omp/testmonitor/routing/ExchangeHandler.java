package de.fraunhofer.fit.omp.testmonitor.routing;

import com.google.common.collect.ImmutableList;
import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Message;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public abstract class ExchangeHandler<FI extends FunctionInfo> {
    @Nonnull
    protected final InstanceValidator dataTypeValidator;
    @Nonnull
    @Getter
    protected final FI functionInfo;
    @Nonnull
    protected final List<Message> messages = new ArrayList<>();
    @Nonnull
    protected final InnerBodyExtractor innerBodyExtractor;

    public void addAdditionalMessage(final Message message) throws WrappingMonitorException {
        final Reporter.ValidationTarget validationTarget = determineMessageType(message);
        dataTypeValidator.validateSource(message, innerBodyExtractor, validationTarget);
        messages.add(message);
    }

    public Reporter.ValidationTarget determineMessageType(final Message message) {
        return Reporter.ValidationTarget.REQUEST;
    }

    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();

    public static @Nonnull
    QName determineRootNodeQName(final Message message,
                                 final InnerBodyExtractor innerBodyExtractor) throws WrappingMonitorException {
        try {
            final XMLEventReader xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader(new StringReader(innerBodyExtractor.extractInnerBody(message)));
            while (xmlEventReader.hasNext()) {
                final XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT) {
                    return xmlEvent.asStartElement().getName();
                }
            }
            throw new IllegalArgumentException("no element found in message!");
        } catch (final XMLStreamException exception) {
            throw new WrappingMonitorException(message, exception);
        }
    }

    public ImmutableList<Message> getMessages() {
        return ImmutableList.copyOf(this.messages);
    }
}
