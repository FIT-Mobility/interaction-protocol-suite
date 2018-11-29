package de.fraunhofer.fit.ips.xsd;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class Validator {
    private static final XSImplementation XS_IMPLEMENTATION;

    static {
        System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            XS_IMPLEMENTATION = (XSImplementation) registry.getDOMImplementation("XS-Loader");
        } catch (final InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    interface LevelAgnosticErrorHandler extends DOMErrorHandler {
        void handle(final ValidationError.ErrorLocation errorLocation, final String message);

        default void extract(final DOMError exception) {
            handle(new ValidationError.ErrorLocation(exception.getLocation().getLineNumber(), exception.getLocation().getColumnNumber()), exception.getMessage());
        }

        @Override
        default boolean handleError(final DOMError error) {
            extract(error);
            return true;
        }
    }

    static class CollectingErrorHandler implements LevelAgnosticErrorHandler {
        final TreeSet<ValidationError> errors = new TreeSet<>();

        @Override
        public void handle(final ValidationError.ErrorLocation errorLocation, final String message) {
            errors.add(new ValidationError(errorLocation, message));
        }
    }

    @RequiredArgsConstructor
    static class ErrorHandlerAdapter implements LevelAgnosticErrorHandler {
        final CollectingErrorHandler base;
        final UnaryOperator<ValidationError.ErrorLocation> transformer;

        @Override
        public void handle(final ValidationError.ErrorLocation errorLocation, final String message) {
            base.handle(transformer.apply(errorLocation), message);
        }
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class ValidationError implements Comparable<ValidationError> {
        final ErrorLocation location;
        final String message;

        @Override
        public int compareTo(final ValidationError other) {
            return Comparator.comparing(ValidationError::getLocation)
                             .thenComparing(ValidationError::getMessage)
                             .compare(this, other);
        }

        @RequiredArgsConstructor
        @Getter
        @ToString
        @EqualsAndHashCode
        public static class ErrorLocation implements Comparable<ErrorLocation> {
            final int line, column;

            @Override
            public int compareTo(final ErrorLocation other) {
                return Comparator.comparingInt(ErrorLocation::getLine)
                                 .thenComparingInt(ErrorLocation::getColumn)
                                 .compare(this, other);
            }
        }
    }

    private static void validate(final DOMInputImpl domInput,
                                 final DOMErrorHandler errorHandler) {
        final XSLoader xsLoader = XS_IMPLEMENTATION.createXSLoader(null);
        xsLoader.getConfig().setParameter(
                Constants.XERCES_PROPERTY_PREFIX + Constants.XML_SCHEMA_VERSION_PROPERTY,
                Constants.W3C_XML_SCHEMA11_NS_URI);
        xsLoader.getConfig().setParameter("error-handler", errorHandler);
        xsLoader.load(domInput);
    }

    public static Collection<ValidationError> validate(final String xsd) {
        return validateExtractedSchema(xsd);
    }

    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newFactory();

    private static Collection<ValidationError> validateExtractedSchema(final String xsd) {
        // PHASE 1: VALIDATE AGAINST XSD 1.1
        final String systemIdAsUri = UUID.randomUUID().toString();


        final CollectingErrorHandler collectingErrorHandler = new CollectingErrorHandler();
        try (final StringReader reader = new StringReader(xsd)) {
            validate(new DOMInputImpl(null, systemIdAsUri, null, reader, Charsets.UTF_8.toString()), collectingErrorHandler);
        }

        // PHASE 2: STRIP AWAY ASSERT & ASSERTION & XPATHDEFAULTNAMESPACE, CREATE LOCATION-MAP

        try (final ByteArrayOutputStream bosSimplified = new ByteArrayOutputStream()) {
            final UnaryOperator<ValidationError.ErrorLocation> transformer;
            try (final StringReader reader = new StringReader(xsd)) {
                final XMLEventReader parser = XML_INPUT_FACTORY.createXMLEventReader(reader);
                final XMLEventWriter writer = XML_OUTPUT_FACTORY.createXMLEventWriter(bosSimplified);
                transformer = asd(parser, writer);
            } catch (final XMLStreamException e) {
                log.error(Objects.toString(e), e);
                return collectingErrorHandler.errors;
            }

            // PHASE 3: VALIDATE AGAINST XSD 1.0

            try (final ByteArrayInputStream bisSimplified = new ByteArrayInputStream(bosSimplified.toByteArray())) {
                final ErrorHandlerAdapter errorHandlerAdapter = new ErrorHandlerAdapter(collectingErrorHandler, transformer);
                validate(new DOMInputImpl(null, systemIdAsUri, null, bisSimplified, Charsets.UTF_8.toString()), errorHandlerAdapter);
            }
        } catch (final IOException e) {
            log.error(Objects.toString(e), e);
        }

        return collectingErrorHandler.errors;
    }

    private static final QName SCHEMA = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");

    private static final QName XPATH_DEFAULT_NAMESPACE = new QName("xpathDefaultNamespace");

    private static final QName ASSERT = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "assert");
    private static final QName ASSERTION = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "assertion");

    private static final Set<QName> NODES_TO_SKIP = Sets.newHashSet(
            ASSERT,
            ASSERTION
    );

    private static class Skip {
        final Deque<Location> locationStack = new ArrayDeque<>();
        final TreeMap<Integer, Integer> mapFromStartingLineToOffset = new TreeMap<>();

        Skip() {
            // fix off-by-one resulting from missing line break after <?xml version="1.0"?>
            mapFromStartingLineToOffset.put(0, 1);
        }

        boolean notSkipping() {
            return locationStack.isEmpty();
        }

        void startSkipNode(final Location startLocation) {
            locationStack.add(startLocation);
        }

        void endSkipNode(final Location endLocation) {
            final Location startLocation = locationStack.pop();
            final int startLineNumber = startLocation.getLineNumber();
            final int diff = endLocation.getLineNumber() - startLineNumber;
            final int offset = diff + Optional.ofNullable(mapFromStartingLineToOffset.lastEntry()).map(Map.Entry::getValue).orElse(0);
            mapFromStartingLineToOffset.put(startLineNumber, offset);
        }

        ValidationError.ErrorLocation transform(final ValidationError.ErrorLocation loc) {
            final int line = loc.line;
            return new ValidationError.ErrorLocation(line +
                    Optional.ofNullable(mapFromStartingLineToOffset.floorEntry(line))
                            .map(Map.Entry::getValue)
                            .orElse(0),
                    loc.column);
        }
    }

    private static UnaryOperator<ValidationError.ErrorLocation> asd(final XMLEventReader parser,
                                                                    final XMLEventWriter writer)
            throws XMLStreamException {
        final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
        final Skip skip = new Skip();
        while (parser.hasNext()) {
            final XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT: {
                    final StartElement element = event.asStartElement();
                    if (NODES_TO_SKIP.contains(element.getName())) {
                        skip.startSkipNode(event.getLocation());
                    }
                    if (skip.notSkipping()) {
                        if (SCHEMA.equals(element.getName()) &&
                                null != element.getAttributeByName(XPATH_DEFAULT_NAMESPACE)) {
                            // copy event removing the attributes to be ignored
                            writer.add(xmlEventFactory.createStartElement(
                                    element.getName().getPrefix(),
                                    element.getName().getNamespaceURI(),
                                    element.getName().getLocalPart(),
                                    Iterators.<Attribute>filter(element.getAttributes(),
                                            att -> !XPATH_DEFAULT_NAMESPACE.equals(att.getName())),
                                    element.getNamespaces(),
                                    element.getNamespaceContext()
                            ));
                        } else {
                            writer.add(event);
                        }
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    final EndElement element = event.asEndElement();
                    if (skip.notSkipping()) {
                        writer.add(event);
                    }
                    if (NODES_TO_SKIP.contains(element.getName())) {
                        skip.endSkipNode(event.getLocation());
                    }
                    break;
                }
                default:
                    if (skip.notSkipping()) {
                        writer.add(event);
                    }
                    break;
            }
        }
        parser.close();
        writer.close();
        return skip::transform;
    }

    public static void main(String[] args) {
        final Collection<ValidationError> validationErrors = validateExtractedSchema("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.dimo.de\" xmlns:tns=\"http://www.dimo.de\" xpathDefaultNamespace=\"http://www.dimo.de\" xmlns:omp=\"http://www.dimo.de/omp\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" elementFormDefault=\"qualified\" attributeFormDefault=\"qualified\">\n" +
                "    <complexType name=\"SomeRandomType\">\n" +
                "        <sequence>\n" +
                "            <element name=\"StringElement\" type=\"string\"/>\n" +
                "            <element name=\"IntElement\" type=\"int\"/>\n" +
                "            <element name=\"DoubleElement\" type=\"double\"/>\n" +
                "        </sequence>\n" +
                "        <!--<anyAttribute notNamespace=\"http://www.dimo.de/omp http://www.dimo.de/fuh\"/>-->\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "        <assert test=\"true\" xpathDefaultNamespace=\"##targetNamespace\"/>\n" +
                "    </complexType>\n" +
                "    <complexType name=\"AsdType\">\n" +
                "        <sequence>\n" +
                "            <element name=\"A\" type=\"string\"/>\n" +
                "        </sequence>\n" +
                "        <sequence>\n" +
                "            <element name=\"B\" type=\"string\"/>\n" +
                "        </sequence>\n" +
                "    </complexType>\n" +
                "</schema>\n");
        for (ValidationError validationError : validationErrors) {
            System.err.println(validationError.toString());
        }
    }
}
