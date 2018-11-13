package de.fraunhofer.fit.omp.demoserver;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.fraunhofer.fit.omp.demoserver.configuration.Configuration;
import de.fraunhofer.fit.omp.demoserver.configuration.HostConfig;
import de.fraunhofer.fit.omp.demoserver.impl.ShuttleService;
import de.fraunhofer.fit.omp.demoserver.validation.ValidationInterceptor;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Application {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);

    @Parameter(names = {"--config", "-f"}, description = "path to configuration file", required = true)
    private String configPath;

    public List<Server> createdEndpoints = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        final Application app = new Application();
        final JCommander jCommander = JCommander.newBuilder()
                                                .addObject(app)
                                                .build();
        jCommander.setProgramName("omp-demo-server");
        try {
            jCommander.parse(args);
        } catch (final RuntimeException e) {
            jCommander.usage();
            throw e;
        }
        app.init();
        app.start();
    }

    public void init() throws Exception {
        log.info("Reading configuration {}", configPath);
        final Configuration config;
        try (final FileInputStream configFile = new FileInputStream(configPath)) {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            config = (Configuration) unmarshaller.unmarshal(configFile);
        }
        log.info("Starting with {}", config);

        final HostConfig localHostConfig = config.getLocalHostConfig();
        final String host = String.format("http://%s:%d", localHostConfig.getHostname(), localHostConfig.getPort());
        publishServices(host, config.getValidationHost());
    }

    public void publishServicesWithoutValidation(final String host) throws TransformerConfigurationException {
        publishServices(host, null);
    }

    public void publishServices(final String host, final @Nullable String validationEndpointURI)
            throws TransformerConfigurationException {
        createService(new ShuttleService(), host + "/ShuttleService", validationEndpointURI);
    }

    private void createService(final Object serviceBean,
                               final String address,
                               final @Nullable String validationEndpointURI) throws TransformerConfigurationException {
        JaxWsServerFactoryBean f = new JaxWsServerFactoryBean();
        f.setBus(BusFactory.getDefaultBus());
//        {
//            final LoggingFeature loggingFeature = new LoggingFeature();
//            loggingFeature.setSender(new Slf4jVerboseEventSender());
//            f.getFeatures().add(loggingFeature);
//        }
//        f.setBindingId("http://schemas.xmlsoap.org/wsdl/http/");
        f.setBindingId(SOAPBinding.SOAP12HTTP_BINDING);
        f.setServiceBean(serviceBean);
        f.setAddress(address);
        if (null != validationEndpointURI) {
            final ValidationInterceptor validationInterceptor = new ValidationInterceptor(validationEndpointURI);
            f.getInInterceptors().add(validationInterceptor.new In());
            f.getOutInterceptors().add(validationInterceptor.new Out());
        }
        createdEndpoints.add(f.create());
    }

    public void start() throws Exception {
        Thread.currentThread().join();
    }

    public void stop() {
        for (final Server endpoint : createdEndpoints) {
            endpoint.stop();
            endpoint.destroy();
        }
    }
}
