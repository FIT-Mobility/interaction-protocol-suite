package de.fraunhofer.fit.ips.model.parser;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.impl.xs.opti.SchemaParsingConfig;
import org.apache.xerces.parsers.AbstractSAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.02.2018
 */
@Slf4j
public class PrefixExtractor {

    public static Map<String, String> fromUri(String uri) {
        InputSource inputSource = new InputSource(uri);
        PrefixSaxParser parser = createParser();
        try {
            parser.parse(inputSource);
        } catch (PrefixSaxParserFinishedException e) {
            // This is part of the master plan, no-op
        } catch (SAXException | IOException e) {
            log.error("Error occurred", e);
        }
        return parser.map;
    }

    public static Map<String, String> fromData(String data) {
        XMLInputSource input = new XMLInputSource(null, null, null, new StringReader(data), Charsets.UTF_8.toString());
        PrefixSaxParser parser = createParser();
        try {
            parser.parse(input);
        } catch (PrefixSaxParserFinishedException e) {
            // This is part of the master plan, no-op
        } catch (IOException e) {
            log.error("Error occurred", e);
        }
        return parser.map;
    }

    private static PrefixSaxParser createParser() {
        return new PrefixSaxParser(new SchemaParsingConfig());
    }

    /**
     * Only parses the attributes which are prefix declarations of the root (xs:schema) element.
     */
    private static class PrefixSaxParser extends AbstractSAXParser {

        private final HashMap<String, String> map = new HashMap<>();

        PrefixSaxParser(XMLParserConfiguration config) {
            super(config);
        }

        @Override
        public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
            if (!isXsdElement(element)) {
                throw new PrefixSaxParserFinishedException();
            }

            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getType(i) == null) {
                    continue;
                }

                String localName = attributes.getLocalName(i);  // e.g.: tns
                String qName = attributes.getQName(i);          // e.g.: xmlns:tns
                String attributeValue = attributes.getValue(i); // e.g.: http://www.ixsi-schnittstelle.de/

                if (attributeValue.trim().length() != attributeValue.length()) {
                    log.error("attribute {} contains whitespaces in value: {}", qName, attributeValue);
                    attributeValue = attributeValue.trim();
                }

                // the attributes which are not prefix declarations
                // for ex; elementFormDefault="qualified"
                if (localName.equals(qName)) {
                    continue;
                }

                map.put(attributeValue, localName);
            }
        }

        private static boolean isXsdElement(QName element) {
            return "schema".equals(element.localpart) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(element.uri);
        }

    }

    /**
     * SAX parser stops only if we throw an exception. So, we throw an exception, after we got what we wanted.
     */
    private static class PrefixSaxParserFinishedException extends RuntimeException {

        private static final long serialVersionUID = 3920966984601063019L;
    }
}
