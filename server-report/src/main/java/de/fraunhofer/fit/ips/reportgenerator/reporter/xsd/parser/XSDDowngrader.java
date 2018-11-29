package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.parser;

import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class XSDDowngrader {

    private static final Templates templates;
    static {
        final StreamSource xsl = new StreamSource(
                XSDDowngrader.class.getClassLoader().getResourceAsStream("xsd11-assertion-remover.xsl")
        );

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            templates = transformerFactory.newTemplates(xsl);
        } catch (final TransformerConfigurationException e) {
            log.error(String.valueOf(e), e);
            throw new IllegalStateException(e);
        }
    }

    public static String downgrade(final String xsd11) {
        final StringWriter downgraded = new StringWriter();

        final StreamSource input = new StreamSource(new StringReader(xsd11));

        final Transformer transformer;
        try {
            transformer = templates.newTransformer();
        } catch (final TransformerConfigurationException e) {
            log.error(String.valueOf(e), e);
            return xsd11;
        }
        final StreamResult result = new StreamResult(downgraded);
        try {
            transformer.transform(input, result);
        } catch (final TransformerException e) {
            log.error(String.valueOf(e), e);
            return xsd11;
        }

        return downgraded.toString();
    }
}
