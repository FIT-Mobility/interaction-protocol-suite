package de.fraunhofer.fit.omp.reportgenerator;

import de.fraunhofer.fit.omp.reportgenerator.server.ApplicationServer;
import de.fraunhofer.fit.omp.reportgenerator.server.JettyServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.11.2017
 */
@Slf4j
public class Application {
    private final ApplicationServer server;

    public static void main(String[] args) throws Exception {
        ApplicationConfig prodConfig = ApplicationConfig.builder()
                                                        .reportingCacheReports(true)
                                                        .build();
        Application app = new Application(prodConfig);
        app.start();
        app.join();
    }

    public Application(ApplicationConfig config) {
        log.info("Starting with {}", config);
        server = new JettyServer(config);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public void join() {
        server.join();
    }
}
