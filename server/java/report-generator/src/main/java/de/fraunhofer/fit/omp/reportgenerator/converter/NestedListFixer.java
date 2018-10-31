package de.fraunhofer.fit.omp.reportgenerator.converter;

import com.google.common.collect.ImmutableList;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Stack;

/**
 * @author Fabian Ohler<fabian.ohler1@rwth-aachen.de>
 * @since 14.02.2018
 */
public class NestedListFixer {

    private static final String ORDERED_LIST = "ul";
    private static final String UNORDERED_LIST = "ol";
    private static final String INDENTATION = "ql-indent-";
    private static final String CLASS_ATTRIBUTE = "class";

    public static String fixQuillIndentation(final String string)
            throws SAXException, IOException, TransformerException {
        final Document document = documentFromString(string);
        scanTopLevelElements(document.getDocumentElement());
        return documentToString(document);
    }

    public static Document documentFromString(final String string) throws SAXException, IOException {
        final DOMParser domParser = new DOMParser();
        domParser.parse(new InputSource(new StringReader(string)));
        return domParser.getDocument();
    }

    public static String documentToString(final Document document) throws TransformerException {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        final StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private static void scanTopLevelElements(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int childIndex = 0, length = children.getLength(); childIndex < length; ++childIndex) {
            transform(children.item(childIndex));
        }
    }

    private static void transform(final Node node) {
        final String listType;
        {
            final String nodeName = node.getLocalName();
            if (ORDERED_LIST.equals(nodeName)) {
                listType = ORDERED_LIST;
            } else if (UNORDERED_LIST.equals(nodeName)) {
                listType = UNORDERED_LIST;
            } else {
                return;
            }
        }
        final Stack<Node> listStack = new Stack<>();
        listStack.push(node);
        final Node[] lastItems = new Node[10];
        final ImmutableList<Node> children = copyOfChildren(node);
        int currentIndentationLevel = 0;
        for (int i = 0; i < children.size(); i++) {
            final Node child = children.get(i);
            final int childIndentationLevel = getIndentationLevel(child);
            final int additionalIndent = childIndentationLevel - currentIndentationLevel;
            if (additionalIndent > 0) {
                // insert #additionalIndent many new nested lists

                // we can't use listStack.peek to work with here, since we should insert the
                // first nested list into the surrounding (aka parent) item in case there is one
                Node head = Optional.ofNullable(lastItems[currentIndentationLevel]).orElse(listStack.peek());
                node.removeChild(child);
                for (; currentIndentationLevel < childIndentationLevel; ++currentIndentationLevel) {
                    final Element newList = node.getOwnerDocument().createElement(listType);
                    listStack.push(newList);
                    head.appendChild(newList);
                    head = newList;
                }
                head.appendChild(child);
                lastItems[currentIndentationLevel] = child;
            } else if (additionalIndent < 0) {
                // finish -#additionalIndent many nested lists
                for (; currentIndentationLevel > childIndentationLevel; --currentIndentationLevel) {
                    listStack.pop();
                    lastItems[currentIndentationLevel] = null;
                }
                listStack.peek().appendChild(child);
                lastItems[currentIndentationLevel] = child;
            } else {
                // just copy to current target position
                node.removeChild(child);
                listStack.peek().appendChild(child);
                lastItems[currentIndentationLevel] = child;
            }
        }
    }

    private static ImmutableList<Node> copyOfChildren(final Node node) {
        final NodeList children = node.getChildNodes();
        final ImmutableList.Builder<Node> builder = ImmutableList.builder();
        for (int childIndex = 0, length = children.getLength(); childIndex < length; ++childIndex) {
            builder.add(children.item(childIndex));
        }
        return builder.build();
    }

    private static int getIndentationLevel(final Node node) {
        if (!node.hasAttributes()) {
            return 0;
        }
        final NamedNodeMap attributes = node.getAttributes();
        final String indentString
                = Optional.ofNullable(attributes.getNamedItem(CLASS_ATTRIBUTE))
                          .map(Node::getNodeValue)
                          .filter(cv -> cv.startsWith(INDENTATION))
                          .map(cv -> cv.substring(INDENTATION.length(), cv.length()))
                          .orElse("0");
        return Integer.parseUnsignedInt(indentString);
    }
}
