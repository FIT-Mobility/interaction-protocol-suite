package de.fraunhofer.fit.omp.testmonitor.validation;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.routing.InnerBodyExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Message;
import org.apache.camel.impl.MessageSupport;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class FunctionValidator extends ValidationBase {
    private final Templates transformerTemplate;

    public FunctionValidator(final Reporter reporter, final LSResourceResolver resourceResolver, final String baseURI,
                             final String functionSchema)
            throws SAXException, TransformerConfigurationException {
        super(reporter, resourceResolver, baseURI, functionSchema);

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        this.transformerTemplate = transformerFactory.newTemplates(new StreamSource(FunctionValidator.class.getResource("/transform.xsl").toExternalForm()));
    }

    public void validateFunction(final QName functionElementName,
                                 final Message request,
                                 final Message response,
                                 final InnerBodyExtractor innerBodyExtractor)
            throws WrappingMonitorException {
        final Message functionMessage = request.copy();
        functionMessage.setMessageId(request.getMessageId() + "-function");
        if (functionMessage instanceof MessageSupport) {
            ((MessageSupport) functionMessage).setExchange(request.getExchange());
        }

        final Transformer transformer;
        try {
            transformer = this.transformerTemplate.newTransformer();
        } catch (final TransformerConfigurationException e) {
            throw new WrappingMonitorException(functionMessage, e);
        }
        final ByteArrayOutputStream mergeOutStream = new ByteArrayOutputStream();
        {
            transformer.setURIResolver((href, base) -> {
                try {
                    if ("request.xml".equals(href)) {
                        return new StreamSource(new StringReader(innerBodyExtractor.extractInnerBody(request)));
                    }
                    if ("response.xml".equals(href) && null != response) {
                        return new StreamSource(new StringReader(innerBodyExtractor.extractInnerBody(response)));
                    }
                    return null;
                } catch (final WrappingMonitorException e) {
                    throw new TransformerException(e);
                }
            });
            final StreamResult mergeResult = new StreamResult(mergeOutStream);
            try {
                transformer.transform(
                        new StreamSource(
                                new StringReader(String.format(
                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><%s xmlns=\"%s\"/>",
                                        functionElementName.getLocalPart(),
                                        functionElementName.getNamespaceURI())),
                                String.valueOf(UUID.randomUUID())),
                        mergeResult);
            } catch (final TransformerException e) {
                if (e.getCause() instanceof WrappingMonitorException) {
                    throw ((WrappingMonitorException) e.getCause());
                }
                throw new WrappingMonitorException(functionMessage, e);
            }
        }
        final String merged;
        try {
            merged = mergeOutStream.toString(StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new WrappingMonitorException(functionMessage, e);
        }

        functionMessage.setBody(merged, String.class);

        reporter.processStartOfFunctionValidation(functionMessage, request, response);
        try {
            createValidator(exception -> reporter.processValidationError(functionMessage, exception))
                    .validate(new StreamSource(new StringReader(merged), request.getMessageId()));
        } catch (final SAXException | IOException e) {
            throw new WrappingMonitorException(functionMessage, e);
        }
    }
}
