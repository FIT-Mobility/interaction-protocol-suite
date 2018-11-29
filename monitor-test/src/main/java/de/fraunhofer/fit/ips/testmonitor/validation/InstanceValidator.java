package de.fraunhofer.fit.ips.testmonitor.validation;

import de.fraunhofer.fit.ips.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.ips.testmonitor.routing.InnerBodyExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Message;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class InstanceValidator extends ValidationBase {
    public InstanceValidator(final Reporter reporter, final String baseURI, final String dataTypeSchema)
            throws SAXException {
        this(reporter, null, baseURI, dataTypeSchema);
    }

    public InstanceValidator(final Reporter reporter, final LSResourceResolver resourceResolver,
                             final String baseURI, final String dataTypeSchema)
            throws SAXException {
        super(reporter, resourceResolver, baseURI, dataTypeSchema);
    }

    public InstanceValidator(final Reporter reporter, final Collection<URL> dataTypeSchemas) throws SAXException {
        super(reporter, dataTypeSchemas);
    }

    public void validateSource(final Message source,
                               final InnerBodyExtractor innerBodyExtractor,
                               final Reporter.ValidationTarget validationTarget)
            throws WrappingMonitorException {
        final String body = innerBodyExtractor.extractInnerBody(source);
        reporter.processStartOfMessageValidation(source, body, validationTarget);
        try {
            createValidator(exception -> reporter.processValidationError(source, exception))
                    .validate(wrapString(source.getMessageId(), body));
        } catch (SAXException | IOException e) {
            throw new WrappingMonitorException(source, e);
        }
    }
}
