package de.fraunhofer.fit.omp.reportgenerator.server;

import de.fraunhofer.fit.omp.reportgenerator.ApplicationConfig;
import de.fraunhofer.fit.omp.reportgenerator.server.servlet.AsyncReportServlet;
import de.fraunhofer.fit.omp.reportgenerator.server.servlet.TemplateServlet;
import de.fraunhofer.fit.omp.reportgenerator.server.servlet.ValidationServlet;
import de.fraunhofer.fit.omp.reportgenerator.service.ReportService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.MultipartConfigElement;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.11.2017
 */
public class JettyServer implements ApplicationServer {

    private final Server server;

    public JettyServer(ApplicationConfig config) {
        QueuedThreadPool threadPool = new QueuedThreadPool(
                config.getServerMaxThreads(),
                config.getServerMinThreads(),
                config.getServerIdleTimeoutMillis()
        );

        server = new Server(threadPool);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendXPoweredBy(false);

        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setHost(config.getServerHost());
        connector.setPort(config.getServerPort());
        server.setConnectors(new Connector[]{connector});

        if (config.isServerLogRequests()) {
            server.setRequestLog(new Slf4jRequestLog());
        }

        ServletContextHandler context = new ServletContextHandler();
        context.setErrorHandler(new JsonErrorHandler());
        server.setHandler(context);

        ReportService reportService = new ReportService(config);

        ServletHolder asyncHolder = new ServletHolder(new AsyncReportServlet(reportService, config));
        asyncHolder.setInitOrder(0); // init and load the servlet at startup
        context.addServlet(asyncHolder, config.getApiAsyncReportPath() + "/*");

        ServletHolder templateHolder = new ServletHolder(new TemplateServlet(reportService));
        templateHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(""));
        context.addServlet(templateHolder, config.getApiTemplateUploadPath());

        ServletHolder validationHolder = new ServletHolder(new ValidationServlet());
        context.addServlet(validationHolder, config.getApiValidatorPath());
    }

    @Override
    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void join() {
        try {
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
