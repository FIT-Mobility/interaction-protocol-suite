package de.fraunhofer.fit.ips.testmonitor.data;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import java.util.Iterator;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class SchemaHeadCopy {
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();

    public static @Nonnull
    DocumentWrapper.SchemaWrapper fromString(final DocumentWrapper document,
                                             final Source dataTypeSchema) {
        try {
            final XMLEventReader xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader(dataTypeSchema);
            while (xmlEventReader.hasNext()) {
                final XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT) {
                    return copySchemaHead(document, xmlEvent.asStartElement());
                }
            }
            throw new IllegalArgumentException("no element found in message!");
        } catch (final XMLStreamException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static DocumentWrapper.SchemaWrapper copySchemaHead(final DocumentWrapper document,
                                                                final StartElement element) {
        final QName elementName = element.getName();
        if (!("schema".equals(elementName.getLocalPart()) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(elementName.getNamespaceURI()))) {
            throw new IllegalArgumentException("document didn't start with xs:schema!");
        }

        final DocumentWrapper.SchemaWrapper schemaNode = document.createSchema(elementName);

        for (final Iterator<Attribute> attributes = element.getAttributes(); attributes.hasNext(); ) {
            final Attribute attribute = attributes.next();
            final QName name = attribute.getName();
            final String value = helper(attribute, Attribute::getValue);
            schemaNode.setAttribute(name, value);
        }
        for (final Iterator<Namespace> namespaces = element.getNamespaces(); namespaces.hasNext(); ) {
            final Namespace namespace = namespaces.next();
            final String prefix = namespace.getPrefix();
            final String namespaceURI = helper(namespace, Namespace::getNamespaceURI);
            schemaNode.setNamespace(prefix, namespaceURI);
        }
        return schemaNode;
    }

    private static <X extends Attribute> String helper(final X attribute, final Function<X, String> getter) {
        final String value = getter.apply(attribute);
        if (value.trim().length() != value.length()) {
            log.error("attribute {} contains whitespaces in value: {}", attribute.getName(), value);
            return value.trim();
        }
        return value;
    }
}
