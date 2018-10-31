package de.fraunhofer.fit.omp.testmonitor.data;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class DocumentWrapper {
    final Document document;

    public SchemaWrapper createSchema(final QName qualifiedName) throws DOMException {
        return new SchemaWrapper(internalCreateElementWithPrefixedName(document, qualifiedName.getNamespaceURI(), qNameToPrefixedName(qualifiedName)));
    }

    private static String qNameToPrefixedName(final QName qName) {
        final String prefix = qName.getPrefix();
        return (StringUtils.isEmpty(prefix) ? "" : prefix + ":") + qName.getLocalPart();
    }

    private Element internalCreateElementWithPrefixedName(Node father, String nsURI, String prefixedName) {
        final Element newElement = document.createElementNS(nsURI, prefixedName);
        father.appendChild(newElement);
        return newElement;
    }

    @RequiredArgsConstructor
    public class SchemaWrapper {
        final Element element;
        String targetNamespace;
        final HashMap<String, String> nsToPrefix = new HashMap<>();

        public void setAttribute(final QName name, final String value) {
            if ("targetNamespace".equals(name.getLocalPart())) {
                this.targetNamespace = value;
            }
            this.element.setAttributeNS(name.getNamespaceURI(), qNameToPrefixedName(name), value);
        }

        public void setNamespace(final String prefix, final String namespaceURI) {
            nsToPrefix.put(namespaceURI, prefix);
            this.element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                    XMLConstants.XMLNS_ATTRIBUTE + (StringUtils.isEmpty(prefix) ? "" : ':' + prefix),
                    namespaceURI);
        }

        protected Element internalCreateElement(final Node father, final String nsURI, final String localName) {
            return internalCreateElementWithPrefixedName(father, nsURI, createPrefixedName(nsURI, localName));
        }

        private String createPrefixedName(final String nsURI, final String localName) {
            final String prefix = nsToPrefix.get(nsURI);
            return StringUtils.isEmpty(prefix) ? localName : prefix + ':' + localName;
        }

        public SchemaWrapper.ElementWrapper addElement(final String nsURI, final String localName) {
            return new ElementWrapper(internalCreateElement(this.element, nsURI, localName));
        }

        @RequiredArgsConstructor
        public class ElementWrapper {
            final Element element;

            public ElementWrapper setAttribute(final String name, final String value) throws DOMException {
                element.setAttribute(name, value);
                return this;
            }

            public ElementWrapper setAttributeWithPrefixedValue(final String name, final String valueNamespace,
                                                                final String value) throws DOMException {
                element.setAttribute(name, createPrefixedName(valueNamespace, value));
                return this;
            }

            public SchemaWrapper.ElementWrapper addElement(final String nsURI, final String localName) {
                return new ElementWrapper(internalCreateElement(this.element, nsURI, localName));
            }
        }

    }
}
