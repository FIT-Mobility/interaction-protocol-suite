package de.fraunhofer.fit.ips.server;

import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportResponse;
import de.fraunhofer.fit.ips.proto.javabackend.JavaBackendGrpc;
import de.fraunhofer.fit.ips.proto.javabackend.ReportType;
import de.fraunhofer.fit.ips.proto.javabackend.SchemaAndProjectStructure;
import de.fraunhofer.fit.ips.proto.structure.Function;
import de.fraunhofer.fit.ips.proto.structure.Level;
import de.fraunhofer.fit.ips.proto.structure.MultilingualPlaintext;
import de.fraunhofer.fit.ips.proto.structure.MultilingualRichtext;
import de.fraunhofer.fit.ips.proto.structure.Project;
import de.fraunhofer.fit.ips.proto.structure.QName;
import de.fraunhofer.fit.ips.proto.structure.Request;
import de.fraunhofer.fit.ips.proto.structure.Response;
import de.fraunhofer.fit.ips.proto.structure.Service;
import de.fraunhofer.fit.ips.proto.structure.Text;
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
import java.util.UUID;
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


        final CreateReportRequest.Builder builder = CreateReportRequest.newBuilder();
        builder.setConfiguration(CreateReportRequest.Configuration.newBuilder()
                                                                  .addLanguages(PRIMARY_LANGUAGE)
                                                                  .addLanguages(ADDITIONAL_LANGUAGE)
                                                                  .build());
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
            projectBuilder.setIdentifier(UUID.randomUUID().toString());
            projectBuilder.setTitle("Test-Projekt");
            projectBuilder.addChildren(Project.ProjectChild
                    .newBuilder()
                    .setLevel(Level
                            .newBuilder()
                            .setIdentifier(UUID.randomUUID().toString())
                            .setSuppressNumbering(true)
                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Test-Level")
                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "test level with malicious comment-start-tag [#--")
                                                                  .build())
                            .addChildren(Level.LevelChild
                                    .newBuilder()
                                    .setText(Text
                                            .newBuilder()
                                            .setRtContent(MultilingualRichtext
                                                    .newBuilder()
                                                    .putLanguageToRichtext(PRIMARY_LANGUAGE, "<p xmlns=\"http://www.w3.org/1999/xhtml\">Der Buchungsdienst ${octopod} bietet zwei Funktionen:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Buchung</li><li>Stornierung</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>")
                                                    .putLanguageToRichtext(ADDITIONAL_LANGUAGE, "<p xmlns=\"http://www.w3.org/1999/xhtml\">The Booking service offers two functions:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Booking</li><li>Cancellation</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>")
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
            projectBuilder.addChildren(Project.ProjectChild
                    .newBuilder()
                    .setLevel(Level
                            .newBuilder()
                            .setIdentifier(UUID.randomUUID().toString())
                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Test-Level mit Nummerierung")
                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "test level with numbering")
                                                                  .build())
                            .addChildren(Level.LevelChild
                                    .newBuilder()
                                    .setText(Text
                                            .newBuilder()
                                            .setRtContent(MultilingualRichtext
                                                    .newBuilder()
                                                    .putLanguageToRichtext(PRIMARY_LANGUAGE, "<p xmlns=\"http://www.w3.org/1999/xhtml\">Der Buchungsdienst bietet zwei Funktionen:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Buchung</li><li>Stornierung</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>")
                                                    .putLanguageToRichtext(ADDITIONAL_LANGUAGE, "<p xmlns=\"http://www.w3.org/1999/xhtml\">The Booking service offers two functions:</p><ol xmlns=\"http://www.w3.org/1999/xhtml\"><li>Booking</li><li>Cancellation</li></ol><p xmlns=\"http://www.w3.org/1999/xhtml\"><br /></p>")
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
            projectBuilder.addChildren(Project.ProjectChild
                    .newBuilder()
                    .setService(Service
                            .newBuilder()
                            .setIdentifier(UUID.randomUUID().toString())
                            .setName("ExampleService")
                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Beispieldienst")
                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "example service")
                                                                  .build())
                            .addChildren(Service.ServiceChild
                                    .newBuilder()
                                    .setFunction(Function
                                            .newBuilder()
                                            .setIdentifier(UUID.randomUUID().toString())
                                            .setName("ExampleFunction")
                                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Beispielfunktion")
                                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "example function")
                                                                                  .build())
                                            .addChildren(Function.FunctionChild
                                                    .newBuilder()
                                                    .setAssertion(Function.Assertion
                                                            .newBuilder()
                                                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Beispielzusicherung")
                                                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "example assertion")
                                                                                                  .build())
                                                            // .setTest("every $offer in GetOffersResponse/offer satisfies $offer/ArrivalTime/Deviation le GetOffersRequest/TimeFlexibility")
                                                            .setTest("ExampleRequest/RequestElement le ExampleResponse/ResponseElement")
                                                            .setXpathDefaultNamespace("##targetNamespace")
                                                            .setDescription(MultilingualRichtext
                                                                    .newBuilder()
                                                                    .putLanguageToRichtext(PRIMARY_LANGUAGE, "Erklärung zur Zusicherung")
                                                                    .putLanguageToRichtext(ADDITIONAL_LANGUAGE, "Explanation of the assertion")
                                                                    .build())
                                                            .build())
                                                    .build())
                                            .addChildren(Function.FunctionChild
                                                    .newBuilder()
                                                    .setRequest(Request
                                                            .newBuilder()
                                                            .setIdentifier(UUID.randomUUID().toString())
                                                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Beispielanfrage")
                                                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "example request")
                                                                                                  .build())
                                                            .setQName(QName.newBuilder().setNamespaceUri("http://www.myshuttle.io").setNcName("ExampleRequest").build())
                                                            .build())
                                                    .build())
                                            .addChildren(Function.FunctionChild
                                                    .newBuilder()
                                                    .setResponse(Response
                                                            .newBuilder()
                                                            .setIdentifier(UUID.randomUUID().toString())
                                                            .setHeadingTitle(MultilingualPlaintext.newBuilder()
                                                                                                  .putLanguageToPlaintext(PRIMARY_LANGUAGE, "Beispielantwort")
                                                                                                  .putLanguageToPlaintext(ADDITIONAL_LANGUAGE, "example response")
                                                                                                  .build())
                                                            .setQName(QName.newBuilder().setNamespaceUri("http://www.myshuttle.io").setNcName("ExampleResponse").build())
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
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
