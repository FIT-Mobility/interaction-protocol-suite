package de.fraunhofer.fit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.fraunhofer.fit.omp.model.json.Function;
import de.fraunhofer.fit.omp.model.json.OmpToolProjectSchema;
import de.fraunhofer.fit.omp.model.json.Schema;
import de.fraunhofer.fit.omp.model.json.Service;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Impl {

    private static final QName QNAME_TARGET_NAMESPACE = new QName("targetNamespace");
    private static final String TNS = "tns";
    private static final String PORT_PREFIX = "port_";
    private static final String SOAP_PREFIX = "soap_";

    public static void createWSDL(final URL jsonFilePath, final String hostname, final File outputDirectory)
            throws IOException, TransformerException, ParserConfigurationException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final OmpToolProjectSchema ompToolProjectSchema = objectMapper.readValue(jsonFilePath, OmpToolProjectSchema.class);

        // FIXME: integrate BASEURI into import/include schemaLocation
        final Schema schema = ompToolProjectSchema.getSchema();
        final String xsd10 = downgradeXSD(schema.getXsd());
        final String targetNamespace = determineTargetNamespace(xsd10);

        final String projectTitle = ompToolProjectSchema.getProject().getTitle();

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final NamespaceManager namespaceManager = new NamespaceManager(targetNamespace, TNS);
        final DocumentWrapper document = new DocumentWrapper(documentBuilder.newDocument(), namespaceManager);

        final ImmutableMap<String, Function> functionLookup = Maps.uniqueIndex(ompToolProjectSchema.getFunctions(), Function::getNcname);

        final DocumentWrapper.ElementWrapper wsdlDefinitions = document.addChild(WSDL11Constants.EL_DEFINITIONS)
                                                                       .addPlainAttribute(QNAME_TARGET_NAMESPACE, targetNamespace)
                                                                       .addPlainAttribute(new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, TNS), targetNamespace);
        final DocumentWrapper.ElementWrapper wsdlService = wsdlDefinitions.addChild(WSDL11Constants.EL_SERVICE)
                                                                          .addPlainAttribute(WSDL11Constants.ATT_NAME, projectTitle);

        final LinkedHashSet<QName> elements = new LinkedHashSet<>();

        for (final Service jsonService : ompToolProjectSchema.getServices()) {
            // FIXME convert name to be a valid ncname!!!
            final String jsonServiceName = jsonService.getName().replaceAll("\\s", "");
            /*
            <port name="portDienst1" binding="tns:SoapDienst1">
                <soap12:address location="http://localhost:9876/Dienst1"/>
            </port>
            */

            // create port (under service), binding, and portType
            final DocumentWrapper.ElementWrapper wsdlPort = wsdlService.addChild(WSDL11Constants.EL_PORT)
                                                                       .addPlainAttribute(WSDL11Constants.ATT_NAME, PORT_PREFIX + jsonServiceName)
                                                                       .addPrefixedAttribute(WSDL11Constants.ATT_BINDING, new QName(targetNamespace, SOAP_PREFIX + jsonServiceName));
            wsdlPort.addChild(SOAP12BindingConstants.EL_ADDRESS)
                    .addPlainAttribute(SOAP12BindingConstants.ATT_LOCATION, hostname + "/" + jsonServiceName);

            final DocumentWrapper.ElementWrapper wsdlBinding = wsdlDefinitions.addChild(WSDL11Constants.EL_BINDING)
                                                                              .addPlainAttribute(WSDL11Constants.ATT_NAME, SOAP_PREFIX + jsonServiceName)
                                                                              .addPrefixedAttribute(WSDL11Constants.ATT_TYPE, new QName(targetNamespace, jsonServiceName));

            /*
            <binding name="SoapDienst1" type="tns:Dienst1">
                <soap12:binding transport="http://www.w3.org/2003/05/soap/bindings/HTTP/"/>
                <operation name="Function1">
                    <soap12:operation soapAction="/Function1" style="document"/>
                    <input>
                        <soap12:body use="literal"/>
                    </input>
                    <output>
                        <soap12:body use="literal"/>
                    </output>
                </operation>
            </binding>
            */

            wsdlBinding.addChild(SOAP12BindingConstants.EL_BINDING)
                       .addPlainAttribute(SOAP12BindingConstants.ATT_TRANSPORT, "http://www.w3.org/2003/05/soap/bindings/HTTP/");

            /*
            <portType name="Dienst1">
                <operation name="Function1">
                    <input message="tns:blablaRequest"/>
                    <output message="tns:asdResponse"/>
                </operation>
            </portType>
            */

            final DocumentWrapper.ElementWrapper wsdlPortType = wsdlDefinitions.addChild(WSDL11Constants.EL_PORT_TYPE)
                                                                               .addPlainAttribute(WSDL11Constants.ATT_NAME, jsonServiceName);

            for (final String jsonFunctionName : jsonService.getFunctions()) {
                final Function jsonFunction = functionLookup.get(jsonFunctionName);
                final QName jsonFunctionInputElementName = convert(jsonFunction.getInputElementName());
                final QName jsonFunctionOutputElementName = convert(jsonFunction.getOutputElementName());

                final DocumentWrapper.ElementWrapper wsdlBindingOperation = wsdlBinding.addChild(WSDL11Constants.EL_OPERATION)
                                                                                       .addPlainAttribute(WSDL11Constants.ATT_NAME, jsonFunctionName);
                wsdlBindingOperation.addChild(SOAP12BindingConstants.EL_OPERATION)
                                    .addPlainAttribute(SOAP12BindingConstants.ATT_SOAP_ACTION, "/" + jsonFunctionName)
                                    .addPlainAttribute(SOAP12BindingConstants.ATT_STYLE, SOAP12BindingConstants.STYLE_DOCUMENT);
                wsdlBindingOperation.addChild(WSDL11Constants.EL_INPUT)
                                    .addChild(SOAP12BindingConstants.EL_BODY)
                                    .addPlainAttribute(SOAP12BindingConstants.ATT_USE, SOAP12BindingConstants.USE_LITERAL);
                wsdlBindingOperation.addChild(WSDL11Constants.EL_OUTPUT)
                                    .addChild(SOAP12BindingConstants.EL_BODY)
                                    .addPlainAttribute(SOAP12BindingConstants.ATT_USE, SOAP12BindingConstants.USE_LITERAL);

                final DocumentWrapper.ElementWrapper wsdlPortTypeOperation = wsdlPortType.addChild(WSDL11Constants.EL_OPERATION)
                                                                                         .addPlainAttribute(WSDL11Constants.ATT_NAME, jsonFunctionName);
                wsdlPortTypeOperation.addChild(WSDL11Constants.EL_INPUT).addPrefixedAttribute(WSDL11Constants.ATT_MESSAGE,
                        distinguishLocalFromImported(namespaceManager, targetNamespace, jsonFunctionInputElementName)
                );
                wsdlPortTypeOperation.addChild(WSDL11Constants.EL_OUTPUT).addPrefixedAttribute(WSDL11Constants.ATT_MESSAGE,
                        distinguishLocalFromImported(namespaceManager, targetNamespace, jsonFunctionOutputElementName)
                );

                elements.add(jsonFunctionInputElementName);
                elements.add(jsonFunctionOutputElementName);
            }
        }

        for (final QName element : elements) {
            /*
            <message name="yoloRequest">
                <part name="body" element="tns:CountRequest"/>
            </message>
            */
            wsdlDefinitions.addChild(WSDL11Constants.EL_MESSAGE)
                           .addPlainAttribute(WSDL11Constants.ATT_NAME, distinguishLocalFromImported(namespaceManager, targetNamespace, element).getLocalPart())
                           .addChild(WSDL11Constants.EL_PART)
                           .addPlainAttribute(WSDL11Constants.ATT_NAME, "body")
                           .addPrefixedAttribute(WSDL11Constants.ATT_ELEMENT, element);
        }

        for (Map.Entry<String, String> ns2PrefixEntry : namespaceManager.namespace2Prefix.entrySet()) {
            final String namespace = ns2PrefixEntry.getKey();
            final String prefix = ns2PrefixEntry.getValue();
            wsdlDefinitions.addPlainAttribute(new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix), namespace);
        }

        wsdlDefinitions.addChild(WSDL11Constants.EL_TYPES);
        final FileOutputStream fileOutputStream = new FileOutputStream(Paths.get(outputDirectory.toURI()).resolve("generated.wsdl").toFile());
        embedXSD(new DOMSource(document.document), xsd10, new StreamResult(fileOutputStream));
    }

    private static QName distinguishLocalFromImported(final NamespaceManager namespaceManager,
                                                      final String targetNamespace,
                                                      final QName jsonFunctionXElementName) {
        final String namespaceURI = jsonFunctionXElementName.getNamespaceURI();
        return targetNamespace.equals(namespaceURI) ?
                jsonFunctionXElementName :
                new QName(targetNamespace, namespaceManager.getPrefix(namespaceURI) + "_" + jsonFunctionXElementName.getLocalPart());
    }

    private static QName convert(final de.fraunhofer.fit.omp.model.json.QName qName) {
        final URI namespaceuri = qName.getNamespaceuri();
        return new QName(namespaceuri == null ? null : namespaceuri.toString(), qName.getNcname());
    }

    private static String determineTargetNamespace(final String xsd10) {
        try {
            final XMLEventReader xmlEventReader = XMLInputFactory.newFactory().createXMLEventReader(new StringReader(xsd10));
            while (xmlEventReader.hasNext()) {
                final XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT) {
                    final StartElement element = xmlEvent.asStartElement();
                    final QName elementName = element.getName();
                    if (!("schema".equals(elementName.getLocalPart()) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(elementName.getNamespaceURI()))) {
                        throw new IllegalArgumentException("document didn't start with xs:schema!");
                    }

                    for (final Iterator<Attribute> attributes = element.getAttributes(); attributes.hasNext(); ) {
                        final Attribute attribute = attributes.next();
                        final QName name = attribute.getName();
                        if (QNAME_TARGET_NAMESPACE.equals(name)) {
                            return attribute.getValue();
                        }
                    }
                }
            }
            throw new IllegalArgumentException("no element found in message!");
        } catch (final XMLStreamException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private static String downgradeXSD(final String xsd11) throws TransformerException {
        final StreamSource xslSource = new StreamSource(Impl.class.getResource("/xsd11-assertion-remover.xsl").toExternalForm());
        final Templates templates = TransformerFactory.newInstance().newTemplates(xslSource);
        final StringWriter writer = new StringWriter();
        final StreamSource sourceSchema = new StreamSource(new ByteArrayInputStream(xsd11.getBytes(Charsets.UTF_8)), "schema.xsd");
        templates.newTransformer().transform(sourceSchema, new StreamResult(writer));
        return writer.toString();
    }

    private static void embedXSD(final Source withoutTypes, final String xsd, final StreamResult result)
            throws TransformerException {
        final Templates template = TransformerFactory.newInstance().newTemplates(new StreamSource(Impl.class.getResource("/transform.xsl").toExternalForm()));
        final Transformer transformer = template.newTransformer();
        {
            transformer.setURIResolver((href, base) -> {
                if ("schema.xsd".equals(href)) {
                    return new StreamSource(new StringReader(xsd));
                }
                return null;
            });
            transformer.transform(withoutTypes, result);
        }
    }

    static class NamespaceManager {
        final AtomicInteger nsCounter = new AtomicInteger();
        final HashMap<String, String> namespace2Prefix = new HashMap<>();

        public NamespaceManager(final String targetNamespace, final String targetNamespacePrefix) {
            this.namespace2Prefix.put(targetNamespace, targetNamespacePrefix);
        }

        private String getPrefix(final String namespace) {
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
                return XMLConstants.XMLNS_ATTRIBUTE;
            }
            if (XMLConstants.XML_NS_URI.equals(namespace)) {
                return XMLConstants.XML_NS_PREFIX;
            }
            return namespace2Prefix.computeIfAbsent(namespace, ignored -> "ns" + nsCounter.incrementAndGet());
        }
    }

    @RequiredArgsConstructor
    static class DocumentWrapper {
        final Document document;
        final NamespaceManager namespaceManager;

        String parse(final QName qname) {
            final String localPart = qname.getLocalPart();
            final String namespaceURI = qname.getNamespaceURI();
            if (null == namespaceURI || XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
                return localPart;
            }
            final String prefix = namespaceManager.getPrefix(namespaceURI);
            return prefix + ":" + localPart;
        }

        ElementWrapper addChild(final QName qname) {
            return addChild(this.document, qname);
        }

        ElementWrapper addChild(final Node father, QName qname) {
            final String domQname = parse(qname);
            final Element element = document.createElementNS(qname.getNamespaceURI(), domQname);
            father.appendChild(element);
            return new ElementWrapper(element);
        }

        @RequiredArgsConstructor
        class ElementWrapper {
            final Element element;

            ElementWrapper addChild(final QName qname) {
                return DocumentWrapper.this.addChild(element, qname);
            }

            ElementWrapper addPrefixedAttribute(final QName qname,
                                                final QName qValue) {
                final String domQvalue = parse(qValue);
                return addPlainAttribute(qname, domQvalue);
            }

            ElementWrapper addPlainAttribute(final QName qname, final String value) {
                final String domQname = parse(qname);
                element.setAttributeNS(qname.getNamespaceURI(), domQname, value);
                return this;
            }
        }
    }
}
