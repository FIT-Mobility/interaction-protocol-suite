package de.fraunhofer.fit.ips.server;

import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportResponse;
import de.fraunhofer.fit.ips.proto.javabackend.JavaBackendGrpc;
import de.fraunhofer.fit.ips.proto.javabackend.ReportType;
import de.fraunhofer.fit.ips.proto.javabackend.SchemaAndProjectStructure;
import de.fraunhofer.fit.ips.proto.structure.Project;
import de.fraunhofer.fit.ips.proto.xsd.Schema;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class SimpleTest {
    private static final String PRIMARY_LANGUAGE = "de";
    private static final String ADDITIONAL_LANGUAGE = "en";
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Test
    public void simpleReportingTest() throws Exception {
        // Generate a unique in-process server name.
        final String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new MyJavaBackendImpl()).build().start());

        JavaBackendGrpc.JavaBackendBlockingStub blockingStub = JavaBackendGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));


        final Helper helper = new Helper(PRIMARY_LANGUAGE, ADDITIONAL_LANGUAGE);

        final CreateReportRequest.Builder builder = CreateReportRequest.newBuilder();
        builder.setConfiguration(helper.getDefaultConfiguration());
        final String xsd = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\"\n" +
                "           targetNamespace=\"http://www.myshuttle.io\" xmlns:io=\"http://www.myshuttle.io\">\n" +
                "    <xs:complexType name=\"ExampleRequestStructure\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element name=\"RequestElement\" type=\"xs:nonNegativeInteger\"/>\n" +
                "        </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"ExampleResponseStructure\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element name=\"ResponseElement\" type=\"xs:nonNegativeInteger\"/>\n" +
                "        </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "    <xs:element name=\"ExampleRequest\" type=\"io:ExampleRequestStructure\"/>\n" +
                "    <xs:element name=\"ExampleResponse\" type=\"io:ExampleResponseStructure\"/>\n" +
                "</xs:schema>";
        final Project.Builder projectBuilder = Project.newBuilder();
        {
            projectBuilder.setIdentifier(Util.newIdentifier());
            projectBuilder.setTitle("Test-Projekt");
            projectBuilder.addChildren(Util.newPLevel(true, l ->
                    l.setHeadingTitle(helper.mlPlaintext("Test-Level", "test level with malicious comment-start-tag [#--"))
                     .addChildren(Util.newLText(helper.mlRichtext(
                             "<p xmlns=\"http://www.w3.org/1999/xhtml\">Der Buchungsdienst ${octopod} bietet zwei Funktionen:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Buchung</li><li>Stornierung</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>",
                             "<p xmlns=\"http://www.w3.org/1999/xhtml\">The Booking service offers two functions:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Booking</li><li>Cancellation</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>"
                     )))));
            projectBuilder.addChildren(Util.newPLevel(l ->
                    l.setHeadingTitle(helper.mlPlaintext("Test-Level mit Nummerierung", "test level with numbering"))
                     .addChildren(Util.newLText(helper.mlRichtext(
                             "<p xmlns=\"http://www.w3.org/1999/xhtml\">Der Buchungsdienst bietet zwei Funktionen:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Buchung</li><li>Stornierung</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>",
                             "<p xmlns=\"http://www.w3.org/1999/xhtml\">The Booking service offers two functions:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Booking</li><li>Cancellation</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>"
                     )))));
            projectBuilder.addChildren(Util.newService("ExampleService", s ->
                    s.setHeadingTitle(helper.mlPlaintext("Beispieldienst", "example service"))
                     .addChildren(Util.newFunction("ExampleFunction", fb ->
                             fb.setHeadingTitle(helper.mlPlaintext("Beispielfunktion", "example function"))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Beispielzusicherung", "example assertion"))
                                        // .setTest("every $offer in GetOffersResponse/offer satisfies $offer/ArrivalTime/Deviation le GetOffersRequest/TimeFlexibility")
                                        .setTest("ExampleRequest/RequestElement le ExampleResponse/ResponseElement")
                                        .setXpathDefaultNamespace("##targetNamespace")
                                        .setDescription(helper.mlRichtext("Erklärung zur Zusicherung", "Explanation of the assertion"))))
                               .addChildren(Util.newRequest(req ->
                                       req.setHeadingTitle(helper.mlPlaintext("Beispielanfrage", "example request"))
                                          .setQName(Util.newQName("http://www.myshuttle.io", "ExampleRequest"))))
                               .addChildren(Util.newResponse(res ->
                                       res.setHeadingTitle(helper.mlPlaintext("Beispielantwort", "example response"))
                                          .setQName(Util.newQName("http://www.myshuttle.io", "ExampleResponse"))))
                     ))));
        }
        final Project project = projectBuilder.build();
        builder.setSchemaAndProjectStructure(SchemaAndProjectStructure.newBuilder()
                                                                      .setProject(project)
                                                                      .setSchema(Schema.newBuilder().setXsd(xsd).build())
                                                                      .build());
        builder.addReportTypes(ReportType.REPORT_TYPE_DOCX);
        builder.addReportTypes(ReportType.REPORT_TYPE_PDF);
        final CreateReportRequest request = builder.build();
        final CreateReportResponse response = blockingStub.createReport(request);
        assertEquals(2, response.getReportsCount());
        final Set<ReportType> resultReportTypes = response.getReportsList().stream().map(CreateReportResponse.Report::getReportType).collect(Collectors.toSet());
        Assertions.assertEquals(Sets.newHashSet(ReportType.REPORT_TYPE_DOCX, ReportType.REPORT_TYPE_PDF), resultReportTypes);
        for (final CreateReportResponse.Report report : response.getReportsList()) {
            switch (report.getReportType()) {
                case REPORT_TYPE_PDF:
                    try (final FileOutputStream out = new FileOutputStream("target/simple-test-result.pdf")) {
                        out.write(report.getReport().toByteArray());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case REPORT_TYPE_DOCX:
                    try (final FileOutputStream out = new FileOutputStream("target/simple-test-result.docx")) {
                        out.write(report.getReport().toByteArray());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }
}
