package de.fraunhofer.fit.ips.demoserver.validation;

import de.fraunhofer.fit.ips.vaas.VaasConstants;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Taken from http://cxf.apache.org/docs/service-routing.html and modified.
 *
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class ValidationInterceptor {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ValidationInterceptor.class);

    private static final String UNIQUE_KEY = "24042997-57ca-4698-8e7c-92e95f74a679";
    private static final ContentType XML_UTF_8 = ContentType.create(ContentType.TEXT_XML.getMimeType(), StandardCharsets.UTF_8);

    private final String validationEndpointURI;

    public ValidationInterceptor(final String validationEndpointURI) {
        this.validationEndpointURI = validationEndpointURI;
    }

    private void sendToValidationService(final byte[] content, final String exchangeId, final boolean outbound) {
        final HttpPost httpPost = new HttpPost(validationEndpointURI);
        httpPost.setHeader(VaasConstants.HTTP_HEADER_EXCHANGE_ID, exchangeId);
        httpPost.setHeader(VaasConstants.HTTP_HEADER_MESSAGE_TYPE,
                outbound ? VaasConstants.HTTP_HEADER_MESSAGE_TYPE_RESPONSE
                        : VaasConstants.HTTP_HEADER_MESSAGE_TYPE_REQUEST);
        httpPost.setEntity(new ByteArrayEntity(content, XML_UTF_8));
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(httpPost);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public class In extends AbstractSoapInterceptor {
        private final Templates transformerTemplate;

        public In() throws TransformerConfigurationException {
            super(Phase.POST_STREAM);
            super.addBefore(StaxInInterceptor.class.getName());
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            this.transformerTemplate = transformerFactory.newTemplates(new StreamSource(ValidationInterceptor.class.getResource("/soap-unwrapper.xsl").toExternalForm()));
        }

        public byte[] extractBodyFromSoapEnvelope(final byte[] wrapped) throws TransformerException {
            final Transformer transformer;
            transformer = this.transformerTemplate.newTransformer();
            final ByteArrayOutputStream mergeOutStream = new ByteArrayOutputStream();
            {
                final StreamResult mergeResult = new StreamResult(mergeOutStream);
                transformer.transform(
                        new StreamSource(new ByteArrayInputStream(wrapped), String.valueOf(UUID.randomUUID())),
                        mergeResult);
            }
            return mergeOutStream.toByteArray();
        }

        @Override
        public void handleMessage(final SoapMessage message) throws Fault {
            final Exchange exchange = message.getExchange();

            final String exchangeId = UUID.randomUUID().toString();
            exchange.put(UNIQUE_KEY, exchangeId);

            final InputStream is = message.getContent(InputStream.class);
            try {
                final byte[] soapBytes = IOUtils.toByteArray(is);
                message.setContent(InputStream.class, new ByteArrayInputStream(soapBytes));

                final byte[] content = extractBodyFromSoapEnvelope(soapBytes);
                sendToValidationService(content, exchangeId, false);
            } catch (final IOException | TransformerException e) {
                log.error("Exception occurred", e);
            }
        }
    }

    public class Out extends AbstractSoapInterceptor {
        public Out() {
            super(Phase.USER_PROTOCOL);
        }

        @Override
        public void handleMessage(final SoapMessage message) throws Fault {
            final Exchange exchange = message.getExchange();

            final String exchangeId = String.valueOf(exchange.get(UNIQUE_KEY));

            final List listContent = message.getContent(List.class);
            if (null == listContent) {
                log.error("no bean found in outbound message!");
                return;
            }
            final Object bean = listContent.get(0);
            try {
                final JAXBContext jaxbContext = JAXBContext.newInstance(bean.getClass());
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                jaxbContext.createMarshaller().marshal(bean, byteStream);
                sendToValidationService(byteStream.toByteArray(), exchangeId, true);
            } catch (final JAXBException e) {
                log.error("Exception occurred", e);
            }
        }
    }
}
