package de.fraunhofer.fit.ips.server;

import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
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
public class DemoTest {
    private static final String PRIMARY_LANGUAGE = "de";
    private static final String ADDITIONAL_LANGUAGE = "en";
    private static final String NAMESPACE_URI = "http://www.myshuttle.io";
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

        final Project.Builder projectBuilder = Project.newBuilder();
        {
            projectBuilder.setIdentifier(Util.newIdentifier());
            projectBuilder.setTitle("PlugFest");
            projectBuilder.addChildren(Util.newPLevel(true, l ->
                    l.setHeadingTitle(helper.mlPlaintext("Einleitung", "Introduction"))
                     .addChildren(Util.newLText(helper.mlRichtext(
                             "<p xmlns=\"http://www.w3.org/1999/xhtml\">Dieses Projekt dient primär der Demonstration der Werkzeugunterstützung im Rahmen des AP 4.1. Es enthält lediglich einen Dienst namens Shuttle-Service. Dieser bietet Funktionen für die Nutzung eines On-Demand-Ride-Sharing-Dienstes an.</p>",
                             "<p xmlns=\"http://www.w3.org/1999/xhtml\"></p>"
                     )))));
            projectBuilder.addChildren(Util.newService("ShuttleService", s ->
                    s.setHeadingTitle(helper.mlPlaintext("Dienst ShuttleService", "Service ShuttleService"))
                     .addChildren(Util.newSText(helper.slRichtext("<p xmlns=\"http://www.w3.org/1999/xhtml\">Der Dienst ShuttleService stellt Funktionen für die Beauskunftung und Buchung eines On-Demand-Ride-Sharing-Angebots zur Verfügung.</p>")))
                     .addChildren(Util.newFunction("GetOffersFunction", fb ->
                             fb.setHeadingTitle(helper.slPlaintext("GetOffersFunction"))
                               // spatial flexibility
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Örtliche Abweichung am Start-Ort", "Spatial deviation at the start location"))
                                        .setTest("every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies $offer/From/WalkWayInMeters le GetOffersRequest/From/FlexibilityInMeters")
                                        .setDescription(helper.mlRichtext("Angebote berücksichtigen die angegebene, maximale örtliche Abweichung am Start-Ort.",
                                                "Offers obey the given maximum spatial deviation for the start location."))))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Örtliche Abweichung am Ziel-Ort", "Spatial deviation at the target location"))
                                        .setTest("every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies $offer/To/WalkWayInMeters le GetOffersRequest/To/FlexibilityInMeters")
                                        .setDescription(helper.mlRichtext("Angebote berücksichtigen die angegebene, maximale örtliche Abweichung am Ziel-Ort.",
                                                "Offers obey the given maximum spatial deviation for the target location."))))
                               // temporal flexibility
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Zeitliche Abweichung am Start-Ort", "Temporal deviation at the start location"))
                                        .setTest("every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies xs:dayTimeDuration(fn:string($offer/DepartureTime/Deviation)) le xs:dayTimeDuration(fn:string(GetOffersRequest/TimeFlexibility))")
                                        .setDescription(helper.mlRichtext("Angebote berücksichtigen die angegebene, maximale zeitliche Abweichung am Start-Ort.",
                                                "Offers obey the given maximum temporal deviation for the target location."))))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Zeitliche Abweichung am Ziel-Ort", "Temporal deviation at the target location"))
                                        .setTest("every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies xs:dayTimeDuration(fn:string($offer/ArrivalTime/Deviation)) le xs:dayTimeDuration(fn:string(GetOffersRequest/TimeFlexibility))")
                                        .setDescription(helper.mlRichtext("Angebote berücksichtigen die angegebene, maximale zeitliche Abweichung am Ziel-Ort.",
                                                "Offers obey the given maximum temporal deviation for the target location."))))
                               // arrival time deviation
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Keine Ankunftszeitabweichung bei gegebener Abfahrtszeit", "No arrival time deviation when departure time was specified"))
                                        .setTest("if (boolean(GetOffersRequest/DepartureTime)) then (every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies $offer/ArrivalTime/Deviation eq xs:duration('PT0S')) else true()")
                                        .setDescription(helper.mlRichtext("Falls die Abfahrtszeit angegeben wurde, entspricht die Abweichung der Ankunftszeit 0 Sekunden.",
                                                "In case the departure time was specified, the arrival time deviation is 0 seconds."))))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Abfahrtszeit-Abweichung auf 2 Sekunden genau berechnet", "Departure time deviation calculated accurate to 2 seconds"))
                                        .setTest("if (boolean(GetOffersRequest/DepartureTime)) then (every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies (xs:dayTimeDuration(fn:string($offer/DepartureTime/Deviation)) - ($offer/DepartureTime/Time - GetOffersRequest/DepartureTime)) le xs:dayTimeDuration('PT2S')) else true()")
                                        .setDescription(helper.mlRichtext("",
                                                ""))))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Abfahrtszeit nach Wunschzeit", "Departure time after specified time"))
                                        .setTest("if (boolean(GetOffersRequest/DepartureTime)) then (every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies $offer/DepartureTime/Time ge GetOffersRequest/DepartureTime) else true()")
                                        .setDescription(helper.mlRichtext("",
                                                ""))))
                               // departure time deviation
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Keine Abfahrtszeitabweichung bei gegebener Ankunftszeit", "No departure time deviation when arrival time was specified"))
                                        .setTest("if (boolean(GetOffersRequest/ArrivalTime)) then (every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies $offer/DepartureTime/Deviation eq xs:duration('PT0S')) else true()")
                                        .setDescription(helper.mlRichtext("Falls die Ankunftszeit angegeben wurde, entspricht die Abweichung der Abfahrtszeit 0 Sekunden.",
                                                "In case the arrival time was specified, the departure time deviation is 0 seconds."))))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Ankunftszeit-Abweichung auf 2 Sekunden genau berechnet", "Arrival time deviation calculated accurate to 2 seconds"))
                                        .setTest("if (boolean(GetOffersRequest/ArrivalTime)) then (every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies (xs:dayTimeDuration(fn:string($offer/ArrivalTime/Deviation)) - (GetOffersRequest/ArrivalTime - $offer/ArrivalTime/Time)) le xs:dayTimeDuration('PT2S')) else true()")
                                        .setDescription(helper.mlRichtext("",
                                                ""))))
                               .addChildren(Util.newAssertion(a ->
                                       a.setHeadingTitle(helper.mlPlaintext("Ankunftszeit vor Wunschzeit", "Arrival time before specified time"))
                                        .setTest("if (boolean(GetOffersRequest/ArrivalTime)) then (every $offer in GetOffersResponse/GetOffersSuccess/Offer satisfies $offer/ArrivalTime/Time le GetOffersRequest/ArrivalTime) else true()")
                                        .setDescription(helper.mlRichtext("",
                                                ""))))

                               // request
                               .addChildren(Util.newRequest(req -> req.setHeadingTitle(helper.slPlaintext("Request"))
                                                                      .setQName(Util.newQName(NAMESPACE_URI, "GetOffersRequest"))
                                                                      .addChildren(Util.newRParticle(NAMESPACE_URI, "GetOffersRequestType"))
                                                                      .addChildren(Util.newRParticle(NAMESPACE_URI, "FlexibleGeoLocationType"))
                               ))
                               // response
                               .addChildren(Util.newResponse(res -> res.setHeadingTitle(helper.slPlaintext("Response"))
                                                                       .setQName(Util.newQName(NAMESPACE_URI, "GetOffersResponse"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "GetOffersResponseType"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "GetOffersSuccessResponseType"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "OfferType"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "ActualGeoLocationType"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "ActualTimeType"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "GetOffersErrorResponseType"))
                                                                       .addChildren(Util.newRParticle(NAMESPACE_URI, "GetOffersErrorCodeType"))
                               ))
                     ))
                     .addChildren(Util.newFunction("BookingFunction", fb ->
                             fb.setHeadingTitle(helper.slPlaintext("GetOffersFunction"))

                               // request
                               .addChildren(Util.newRequest(req ->
                                       req.setHeadingTitle(helper.slPlaintext("Request"))
                                          .setQName(Util.newQName(NAMESPACE_URI, "BookingRequest"))
                                          .addChildren(Util.newRParticle(NAMESPACE_URI, "BookingRequestType"))
                               ))
                               // response
                               .addChildren(Util.newResponse(res ->
                                       res.setHeadingTitle(helper.slPlaintext("Response"))
                                          .setQName(Util.newQName(NAMESPACE_URI, "BookingResponse"))
                                          .addChildren(Util.newRParticle(NAMESPACE_URI, "BookingResponseType"))
                                          .addChildren(Util.newRParticle(NAMESPACE_URI, "BookingSuccessResponseType"))
                                          .addChildren(Util.newRParticle(NAMESPACE_URI, "BookingErrorResponseType"))
                                          .addChildren(Util.newRParticle(NAMESPACE_URI, "BookingErrorCodeType"))
                               ))
                     ))
            ));
            projectBuilder.addChildren(Util.newPLevel(level ->
                    level.setHeadingTitle(helper.slPlaintext("Common Data Types"))
                         .addChildren(Util.newLParticle(NAMESPACE_URI, "GeneralErrorType"))
                         .addChildren(Util.newLParticle(NAMESPACE_URI, "GeoLocationType"))
                         .addChildren(Util.newLParticle(NAMESPACE_URI, "LatitudeType"))
                         .addChildren(Util.newLParticle(NAMESPACE_URI, "LongitudeType"))
            ));
        }
        final Project project = projectBuilder.build();
        final SchemaAndProjectStructure schemaAndProjectStructure = SchemaAndProjectStructure
                .newBuilder()
                .setProject(project)
                .setSchema(Schema.newBuilder().setXsdBytes(ByteString.readFrom(DemoTest.class.getResourceAsStream("/plugfest-schema.xsd"))).build())
                .build();

        try (final FileOutputStream fileOutputStream = new FileOutputStream("target/demo-schema-and-project-structure.ser")) {
            schemaAndProjectStructure.writeDelimitedTo(fileOutputStream);
        }

        builder.setSchemaAndProjectStructure(schemaAndProjectStructure);
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
                    try (final FileOutputStream out = new FileOutputStream("target/demo-test-result.pdf")) {
                        out.write(report.getReport().toByteArray());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case REPORT_TYPE_DOCX:
                    try (final FileOutputStream out = new FileOutputStream("target/demo-test-result.docx")) {
                        out.write(report.getReport().toByteArray());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }
}
