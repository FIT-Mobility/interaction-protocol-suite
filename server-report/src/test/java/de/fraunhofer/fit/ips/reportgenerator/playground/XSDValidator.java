package de.fraunhofer.fit.ips.reportgenerator.playground;


import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class XSDValidator {
    public static void main(String[] args) throws SAXException, IOException {
        {
            final StreamSource[] metaSchema = new StreamSource[]{
                    new StreamSource(new File("src/test/resources/meta-schema/XMLSchema.xsd"))
            };

            final SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
            final Schema s = sf.newSchema(metaSchema);
            final Validator v = s.newValidator();

            v.validate(new StreamSource(new File("src/test/resources/xsd11-test/example.xsd")));
        }
        {
            final StreamSource[] xsd = new StreamSource[]{
                    new StreamSource(new File("src/test/resources/xsd11-test/example.xsd"))
            };

            final SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
            final Schema s = sf.newSchema(xsd);
            final Validator v = s.newValidator();

            try (final DirectoryStream<Path> files = Files.newDirectoryStream(
                    Paths.get("src/test/resources/xsd11-test"),
                    "example-instance*.xml")) {
                for (final Path file : files) {
                    System.out.println("validating " + file.getFileName() + ": ");
                    v.validate(new StreamSource(file.toFile()));
                }
            }
        }
    }
}
