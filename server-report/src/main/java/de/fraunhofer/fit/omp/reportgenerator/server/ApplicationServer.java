package de.fraunhofer.fit.omp.reportgenerator.server;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.11.2017
 */
public interface ApplicationServer {
    void start();

    void stop();

    void join();
}
