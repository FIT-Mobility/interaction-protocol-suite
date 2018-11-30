package de.fraunhofer.fit.ips.model.parser;

import de.fraunhofer.fit.ips.model.xsd.Documentations;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.apache.xerces.impl.xs.opti.SchemaParsingConfig;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.parsers.AbstractSAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.02.2018
 */
public class DocumentationsExtractor {

    private final DocumentationSaxParser parser = new DocumentationSaxParser();

    public Documentations fromAnnotations(XSObjectList annotations) {
        if (annotations.getLength() == 0) {
            return Documentations.EMPTY;
        }

        parser.newDocs();

        for (int i = 0; i < annotations.getLength(); i++) {
            XSAnnotationImpl ann = (XSAnnotationImpl) annotations.get(i);
            try {
                InputSource is = new InputSource(new StringReader(ann.getAnnotationString()));
                parser.parse(is);
            } catch (SAXException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return parser.docs;
    }

    public Documentations fromAnnotation(XSObject annotation) {
        if (annotation == null) {
            return Documentations.EMPTY;
        }

        return fromAnnotations(new XSObjectListImpl(new XSObject[]{annotation}, 1));
    }

    private static class DocumentationSaxParser extends AbstractSAXParser {

        private Documentations docs;

        private boolean startedParsing = false;

        private String language = "";
        private StringBuilder content = new StringBuilder();

        private DocumentationSaxParser() {
            super(new SchemaParsingConfig());
        }

        @Override
        public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
            if (isDoc(element)) {
                language = extractLanguage(attributes);
                startedParsing = true;
            } else {
                startedParsing = false;
            }
        }

        @Override
        public void endElement(QName element, Augmentations augs) throws XNIException {
            if (isDoc(element)) {
                docs.getDocs()
                    .computeIfAbsent(language, k -> new ArrayList<>())
                    .addAll(cleanUp(content.toString()));
                resetFields();
            }
            startedParsing = false;
        }

        @Override
        public void characters(XMLString text, Augmentations augs) throws XNIException {
            if (startedParsing) {
                content.append(text.toString());
            }
        }

        /**
         * Before starting the parsing process
         */
        private void newDocs() {
            docs = new Documentations();
        }

        /**
         * After one documentation entry is finished
         */
        private void resetFields() {
            language = "";
            content = new StringBuilder();
        }

        private static String extractLanguage(XMLAttributes attributes) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String localName = attributes.getLocalName(i);
                String uri = attributes.getURI(i);
                if (isLang(localName, uri)) {
                    return attributes.getValue(i);
                }
            }
            return "";
        }

        private static boolean isLang(String localName, String uri) {
            return "lang".equals(localName) && XMLConstants.XML_NS_URI.equals(uri);
        }

        private static boolean isDoc(QName element) {
            return "documentation".equals(element.localpart) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(element.uri);
        }

        private static final Pattern DOUBLE_LINEBREAKS = Pattern.compile("(?:\\r\\n|\\r|\\n)[ \\t]*(?:\\r\\n|\\r|\\n)[ \\t]+(\\S)");
        private static final Pattern INDENTED_LINES = Pattern.compile("(?:\\r\\n|\\r|\\n)[ \\t]+(\\S)");


        private static List<String> cleanUp(String str) {
            return Optional.ofNullable(str)
                           .map(s -> DOUBLE_LINEBREAKS.matcher(s).replaceAll("\n$1"))
                           .map(s -> INDENTED_LINES.matcher(s).replaceAll(" $1"))
                           .map(StringUtils::strip)
                           .map(StringUtils::chomp)
                           .map(s -> Arrays.asList(s.split("\\r\\n|\\r|\\n")))
                           .orElse(Collections.emptyList());
        }
    }
}
