package de.fraunhofer.fit.omp.testmonitor.routing.soap;

import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.routing.messagebased.MessageBasedExchangeHandlerFactory;
import de.fraunhofer.fit.omp.testmonitor.routing.messagebased.MessageBasedFunctionInfo;
import de.fraunhofer.fit.omp.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import org.apache.camel.Message;

import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class SoapExchangeHandlerFactory extends MessageBasedExchangeHandlerFactory {
    protected final Templates transformerTemplate;

    public SoapExchangeHandlerFactory(
            final InstanceValidator dataTypeValidator,
            final FunctionValidator functionValidator,
            final HashMap<QName, MessageBasedFunctionInfo> lookup) throws TransformerConfigurationException {
        super(dataTypeValidator, functionValidator, lookup);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        this.transformerTemplate = transformerFactory.newTemplates(new StreamSource(SoapExchangeHandlerFactory.class.getResource("/soap-unwrapper.xsl").toExternalForm()));
    }

    @Override
    public String extractInnerBody(final Message message) throws WrappingMonitorException {
        try {
            return extractBodyFromSoapEnvelope(message.getBody(String.class));
        } catch (TransformerException e) {
            throw new WrappingMonitorException(message, e);
        }
    }

    protected String extractBodyFromSoapEnvelope(final String wrapped) throws TransformerException {
        final Transformer transformer;
        transformer = this.transformerTemplate.newTransformer();
        final StringWriter mergeOutStream = new StringWriter();
        {
            final StreamResult mergeResult = new StreamResult(mergeOutStream);
            transformer.transform(
                    new StreamSource(new StringReader(wrapped), String.valueOf(UUID.randomUUID())),
                    mergeResult);
        }
        return mergeOutStream.toString();
    }
}