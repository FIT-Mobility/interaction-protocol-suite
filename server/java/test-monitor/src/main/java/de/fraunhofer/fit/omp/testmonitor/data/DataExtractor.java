package de.fraunhofer.fit.omp.testmonitor.data;

import de.fraunhofer.fit.omp.model.json.Assertion;
import de.fraunhofer.fit.omp.testmonitor.data.DocumentWrapper.SchemaWrapper;
import de.fraunhofer.fit.omp.testmonitor.data.DocumentWrapper.SchemaWrapper.ElementWrapper;
import de.fraunhofer.fit.omp.testmonitor.routing.FunctionInfo;
import de.fraunhofer.fit.omp.testmonitor.routing.messagebased.MessageBasedFunctionInfo;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class DataExtractor {
    public static String generateFunctionSchema(final Source dataTypeSchema,
                                                final List<FunctionData> functions,
                                                final Function<String, String> function2Service,
                                                final @Nullable Consumer<MessageBasedFunctionInfo> functionInfoConsumer)
            throws ParserConfigurationException, TransformerException {

        final DocumentWrapper documentWrapper;
        {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            documentWrapper = new DocumentWrapper(document);
        }

        final SchemaWrapper schemaNode = SchemaHeadCopy.fromString(documentWrapper, dataTypeSchema);

        {
            schemaNode.addElement(XMLConstants.W3C_XML_SCHEMA_NS_URI, "include")
                      .setAttribute("schemaLocation", dataTypeSchema.getSystemId());
        }
        for (final FunctionData function : functions) {
            final String functionNcname = function.getNcname();
            final QName functionElementName = new QName(schemaNode.targetNamespace, functionNcname);
            final String functionTypeName = functionElementName.getLocalPart() + "Type";
            @Nullable final QName inputElementName = function.getInputElementName();
            @Nullable final QName outputElementName = function.getOutputElementName();
            final List<Assertion> assertions = function.getAssertions();
            // construct functionInfo and publish to consumer
            if (null != functionInfoConsumer) {
                functionInfoConsumer.accept(
                        MessageBasedFunctionInfo.builder()
                                                .functionElementName(functionElementName)
                                                .functionName(functionNcname)
                                                .serviceName(function2Service.apply(functionNcname))
                                                .requestElementName(inputElementName)
                                                .responseElementName(outputElementName)
                                                .build()
                );
            }

            final ElementWrapper complexType = schemaNode.addElement(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType")
                                                         .setAttribute("name", functionTypeName);

            final ElementWrapper sequence = complexType.addElement(XMLConstants.W3C_XML_SCHEMA_NS_URI, "sequence");
            // TODO: in case the type has an output element only, this information is currently lost here, preserve in case we allow omitting one of the elements
            createElementRef(sequence, inputElementName);
            createElementRef(sequence, outputElementName);

            for (final Assertion assertion : assertions) {
                final ElementWrapper assertElement = complexType.addElement(XMLConstants.W3C_XML_SCHEMA_NS_URI, "assert")
                                                                .setAttribute("test", assertion.getTest());
                final String xpathDefaultNamespace = assertion.getXpathDefaultNamespace();
                if (StringUtils.isNotEmpty(xpathDefaultNamespace)) {
                    assertElement.setAttribute("xpathDefaultNamespace", xpathDefaultNamespace);
                }
            }
            schemaNode.addElement(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element")
                      .setAttribute("name", functionElementName.getLocalPart())
                      .setAttributeWithPrefixedValue("type", schemaNode.targetNamespace, functionTypeName);
        }

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource domSource = new DOMSource(documentWrapper.document);
        final StringWriter resultWriter = new StringWriter();
        final StreamResult result = new StreamResult(resultWriter);
        transformer.transform(domSource, result);

        return resultWriter.toString();
    }

    private static void createElementRef(final ElementWrapper sequence, @Nullable final QName elementName) {
        if (null != elementName) {
            sequence.addElement(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element")
                    .setAttributeWithPrefixedValue("ref", elementName.getNamespaceURI(), elementName.getLocalPart());
        }
    }
}
