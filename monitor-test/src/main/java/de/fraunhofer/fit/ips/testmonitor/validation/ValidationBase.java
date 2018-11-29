package de.fraunhofer.fit.ips.testmonitor.validation;

import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class ValidationBase {
    protected final Schema schema;
    protected final Reporter reporter;

    public interface LevelAgnosticErrorHandler extends ErrorHandler {
        void handle(final SAXParseException exception);

        @Override
        default void warning(final SAXParseException exception) {
            handle(exception);
        }

        @Override
        default void error(final SAXParseException exception) {
            handle(exception);
        }

        @Override
        default void fatalError(final SAXParseException exception) {
            handle(exception);
        }
    }


    protected ValidationBase(final Reporter reporter, final LSResourceResolver resourceResolver, final String baseURI,
                             final String schemaContent) throws SAXException {
        this(reporter, resourceResolver, new StreamSource[]{wrapString(baseURI, schemaContent)});
    }

    protected ValidationBase(final Reporter reporter, final Collection<URL> schemaContents) throws SAXException {
        this(reporter, null, schemaContents.stream().map(url -> new StreamSource(url.toExternalForm())).toArray(StreamSource[]::new));
    }

    protected ValidationBase(final Reporter reporter, final LSResourceResolver resourceResolver,
                             final StreamSource[] schemaSources)
            throws SAXException {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
        schemaFactory.setResourceResolver(resourceResolver);
        this.schema = schemaFactory.newSchema(schemaSources);
        this.reporter = reporter;
    }

    protected Validator createValidator(final LevelAgnosticErrorHandler levelAgnosticErrorHandler) {
        final Validator validator = schema.newValidator();
        validator.setErrorHandler(levelAgnosticErrorHandler);
        return validator;
    }

    protected static StreamSource wrapString(final String systemId, final String content) {
        return new StreamSource(new StringReader(content), systemId);
    }
}
