package de.fraunhofer.fit.ips.server;

import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportResponse;
import de.fraunhofer.fit.ips.proto.javabackend.JavaBackendGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Client implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private final ManagedChannel channel;
    private final JavaBackendGrpc.JavaBackendBlockingStub blockingStub;

    public Client(final String host, final int port) {
        this(ManagedChannelBuilder
                .forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    public Client(final ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = JavaBackendGrpc.newBlockingStub(channel);
    }

    @Override
    public void close() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void testRequest() {
        final CreateReportRequest request = CreateReportRequest.newBuilder().build();
        final CreateReportResponse response;
        try {
            response = blockingStub.createReport(request);
        } catch (final StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Success!");
    }


    public static void main(String[] args) throws InterruptedException {
        try (final Client client = new Client("localhost", 50051)) {
            client.testRequest();
        }
    }
}
