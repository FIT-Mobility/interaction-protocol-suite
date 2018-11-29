package de.fraunhofer.fit.ips.reportgenerator.playground;

import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class FunctionValidator {

    private static final String FUNCTION_SCHEMA = "src/test/resources/xsd11-test/function-schema.xsd";
    private static final Path BASEDIR = Paths.get("src/test/resources/function-composition-test/");

    public static void main(String[] args) throws SAXException, IOException {
        {
            final StreamSource[] metaSchema = new StreamSource[]{
                    new StreamSource(new File("src/test/resources/meta-schema/XMLSchema.xsd"))
            };

            final SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
            final Schema s = sf.newSchema(metaSchema);
            final Validator v = s.newValidator();

            v.validate(new StreamSource(new File(FUNCTION_SCHEMA)));
        }

        final ByteArrayOutputStream mergeOutStream = new ByteArrayOutputStream();
        {
            final StreamSource request = new StreamSource(new FileInputStream(BASEDIR.resolve("request.xml").toFile()));
            final StreamSource response = new StreamSource(new FileInputStream(BASEDIR.resolve("response.xml").toFile()));

            final StreamSource template = new StreamSource(new FileInputStream(BASEDIR.resolve("template.xml").toFile()));
            final StreamSource xsl = new StreamSource(new FileInputStream(BASEDIR.resolve("transform.xsl").toFile()));

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer;
            try {
                transformer = transformerFactory.newTransformer(xsl);
            } catch (final TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }
            transformer.setURIResolver((href, base) -> {
                if ("request.xml".equals(href)) {
                    return request;
                }
                if ("response.xml".equals(href)) {
                    return response;
                }
                return null;
            });
            final StreamResult mergeResult = new StreamResult(mergeOutStream);
            try {
                transformer.transform(template, mergeResult);
            } catch (final TransformerException e) {
                throw new RuntimeException(e);
            }
        }
        final ByteArrayInputStream mergeInStream = new ByteArrayInputStream(mergeOutStream.toByteArray());

        {
            final StreamSource[] xsd = new StreamSource[]{
                    new StreamSource(new File(FUNCTION_SCHEMA))
            };

            final SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
            final Schema s = sf.newSchema(xsd);
            final Validator v = s.newValidator();

            v.validate(new StreamSource(mergeInStream));

        }
    }
}
