package de.fraunhofer.fit.ips.testmonitor;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import de.fraunhofer.fit.ips.testmonitor.configuration.Configuration;
import de.fraunhofer.fit.ips.testmonitor.configuration.ConfigurationBase;
import de.fraunhofer.fit.ips.testmonitor.configuration.HttpConfiguration;
import de.fraunhofer.fit.ips.testmonitor.configuration.MqttConfiguration;
import de.fraunhofer.fit.ips.testmonitor.configuration.SoapConfiguration;
import de.fraunhofer.fit.ips.testmonitor.configuration.VaasConfiguration;
import de.fraunhofer.fit.ips.testmonitor.fuh.MqttBootstrapper;
import de.fraunhofer.fit.ips.testmonitor.reporting.MqttReporter;
import de.fraunhofer.fit.ips.testmonitor.reporting.ReporterBase;
import de.fraunhofer.fit.ips.testmonitor.reporting.VaasReporter;
import de.fraunhofer.fit.ips.testmonitor.routing.MqttCamel;
import de.fraunhofer.fit.ips.testmonitor.routing.SoapCamel;
import de.fraunhofer.fit.ips.testmonitor.routing.messagebased.MessageBasedExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.soap.SoapExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.vaas.VaasExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.configuration.visitor.BaseVisitor;
import de.fraunhofer.fit.ips.testmonitor.data.ValidatorFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.HttpCamel;
import de.fraunhofer.fit.ips.testmonitor.routing.VaasCamel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.main.Main;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xquery.XQException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class Application {
    private final Main camelMain = new Main();
    @Parameter(names = {"--config", "-f"}, description = "path to configuration file", required = true)
    private String configPath;

    public static void main(String[] args) throws Exception {
        final Application app = new Application();
        final JCommander jCommander = JCommander.newBuilder()
                                                .addObject(app)
                                                .build();
        jCommander.setProgramName("ips-test-monitor");
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
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(Application.class.getResource("/configuration.xsd"));
            unmarshaller.setSchema(schema);
            config = (Configuration) unmarshaller.unmarshal(configFile);
        }
        log.info("Starting with {}", config);

        final ConfigurationVisitor visitor = new ConfigurationVisitor(camelMain);
        for (final ConfigurationBase mqttOrHTTPOrVAAS : config.getMQTTOrHTTPOrVAAS()) {
            mqttOrHTTPOrVAAS.accept(visitor);
        }
    }

    public void start() throws Exception {
        camelMain.start();
    }

    @RequiredArgsConstructor
    private static class ConfigurationVisitor extends BaseVisitor<Object, Exception> {
        final Main camelMain;

        @Override
        public Object visit(final HttpConfiguration httpConfiguration)
                throws TransformerException, ParserConfigurationException, IOException, SAXException, IllegalDocumentStructureException {
            final ReporterBase reporter = new ReporterBase(httpConfiguration);
            final ValidatorFactory.RegularOperations operations = ValidatorFactory.newRegularOperations(reporter, URI.create(httpConfiguration.getProtoSchemaAndProjectStructureURI()));
            new HttpCamel(
                    httpConfiguration,
                    reporter,
                    new MessageBasedExchangeHandlerFactory(
                            operations.getInstanceValidator(),
                            operations.getFunctionValidator(),
                            operations.getLookup())
            ).registerTo(camelMain);
            return null;
        }

        @Override
        public Object visit(final MqttConfiguration mqttConfiguration)
                throws TransformerException, ParserConfigurationException, IOException, SAXException, XQException {
            final MqttReporter reporter = new MqttReporter(mqttConfiguration);
            final MqttBootstrapper bootstrapper = MqttBootstrapper.bootstrap(reporter, mqttConfiguration);
            new MqttCamel(
                    mqttConfiguration,
                    reporter,
                    bootstrapper.getMqttMessageExchangeHandlerFactory(),
                    bootstrapper.getMqttRequestReplyExchangeHandlerFactory()
            ).registerTo(camelMain);
            return null;
        }

        @Override
        public Object visit(final VaasConfiguration vaasConfiguration)
                throws TransformerException, ParserConfigurationException, IOException, SAXException, IllegalDocumentStructureException {
            // TODO add information about sender to reporter?!
            final VaasReporter reporter = new VaasReporter(vaasConfiguration);
            final ValidatorFactory.RegularOperations operations = ValidatorFactory.newRegularOperations(reporter, URI.create(vaasConfiguration.getProtoSchemaAndProjectStructureURI()));
            new VaasCamel(
                    vaasConfiguration,
                    reporter,
                    new VaasExchangeHandlerFactory(reporter,
                            operations.getInstanceValidator(),
                            operations.getFunctionValidator(),
                            operations.getLookup())
            ).registerTo(camelMain);
            return null;
        }

        @Override
        public Object visit(final SoapConfiguration soapConfiguration)
                throws TransformerException, ParserConfigurationException, IOException, SAXException, IllegalDocumentStructureException {
            final ReporterBase reporter = new ReporterBase(soapConfiguration);
            final ValidatorFactory.RegularOperations operations = ValidatorFactory.newRegularOperations(reporter, URI.create(soapConfiguration.getProtoSchemaAndProjectStructureURI()));
            new SoapCamel(
                    soapConfiguration,
                    reporter,
                    new SoapExchangeHandlerFactory(operations.getInstanceValidator(),
                            operations.getFunctionValidator(),
                            operations.getLookup())
            ).registerTo(camelMain);
            return null;
        }
    }
}
