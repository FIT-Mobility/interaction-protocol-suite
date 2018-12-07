package de.fraunhofer.fit.ips.server;

import de.fraunhofer.fit.ips.proto.javabackend.JavaBackendGrpc;
import de.fraunhofer.fit.ips.proto.javabackend.ValidationRequest;
import de.fraunhofer.fit.ips.proto.javabackend.ValidationResponse;
import de.fraunhofer.fit.ips.proto.xsd.Schema;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class TrivialValidationTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Test
    public void trivialValidationTest() throws Exception {
        // Generate a unique in-process server name.
        final String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new MyJavaBackendImpl()).build().start());

        JavaBackendGrpc.JavaBackendBlockingStub blockingStub = JavaBackendGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));


        final String xsd = "<?xml version=\"1.0\"?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" targetNamespace=\"http://www.myshuttle.io\" xmlns:io=\"http://www.myshuttle.io\"></xs:schema>";
        final ValidationRequest request = ValidationRequest.newBuilder().setSchema(Schema.newBuilder().setXsd(xsd).build()).build();

        final ValidationResponse response = blockingStub.validate(request);
        final List<ValidationResponse.Error> errorsList = response.getErrorsList();
        assertEquals(0, errorsList.size());
    }
}
