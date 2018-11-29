package de.fraunhofer.fit.ips.demoserver;

import io.myshuttle.ActualTimeType;
import io.myshuttle.BookingErrorCodeType;
import io.myshuttle.BookingErrorResponseType;
import io.myshuttle.BookingRequest;
import io.myshuttle.BookingResponse;
import io.myshuttle.BookingSuccessResponseType;
import io.myshuttle.FlexibleGeoLocationType;
import io.myshuttle.GeoLocationType;
import io.myshuttle.GetOffersErrorCodeType;
import io.myshuttle.GetOffersErrorResponseType;
import io.myshuttle.GetOffersRequest;
import io.myshuttle.GetOffersResponse;
import io.myshuttle.GetOffersSuccessResponseType;
import io.myshuttle.OfferType;
import io.myshuttle.PlugFest;
import io.myshuttle.ShuttleService;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import javax.xml.transform.TransformerConfigurationException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Test1 {
    private static final Application app = new Application();

    private static ShuttleService shuttleService;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @BeforeAll
    public static void setup() throws TransformerConfigurationException {
//        app.publishServices("http://localhost:8080", "https://treibhaus.informatik.rwth-aachen.de/testmonitor4");
//        app.publishServices("http://localhost:8080", "http://localhost:9985");
        app.publishServicesWithoutValidation("http://localhost:8080");

        shuttleService = PlugFest.create(PlugFest.WSDL_LOCATION, PlugFest.SERVICE).getPort(ShuttleService.class);
    }

    @AfterAll
    public static void tearDown() {
        app.stop();
    }

    @RepeatedTest(20)
    public void successfulTest() {
        final GetOffersRequest getOffersRequest = createRandomGetOffersRequest();
        final GetOffersResponse getOffersResponse = shuttleService.getOffersFunction(getOffersRequest);
        final List<OfferType> offers = checkGetOffersResponse(getOffersRequest, getOffersResponse);
        if (offers.isEmpty()) {
            return;
        }

        final OfferType randomOffer = offers.get(random.nextInt(offers.size()));

        final BookingResponse bookingResponse = shuttleService.bookingFunction(new BookingRequest()
                .withOfferID(randomOffer.getOfferID())
                .withCustomerID(UUID.randomUUID().toString())
        );
        if (bookingResponse.isSetBookingError()) {
            final BookingErrorResponseType bookingError = bookingResponse.getBookingError();
            final BookingErrorCodeType reason = bookingError.getReason();
            switch (reason) {
                case UNKNOWN___OFFER___ID:
                    Assertions.fail("unknown offer id error despite valid offer id");
                    break;
                case OFFER___EXPIRED: {
                    if (randomOffer.getOfferValidUntil().isAfter(OffsetDateTime.now())) {
                        Assertions.fail("offer has not expired yet, but was declined for that reason: " + randomOffer);
                    }
                    break;
                }
            }
            return;
        }
        final BookingSuccessResponseType bookingSuccess = bookingResponse.getBookingSuccess();
        Assertions.assertNotNull(bookingSuccess.getBookingID(), "booking id must not be null");
        Assertions.assertNotNull(bookingSuccess.getVehicleID(), "vehicle id must not be null");
    }

    @RepeatedTest(2)
    public void unknownOfferIdTest() {
        final BookingResponse bookingResponse = shuttleService.bookingFunction(new BookingRequest()
                .withOfferID(UUID.randomUUID().toString())
                .withCustomerID(UUID.randomUUID().toString())
        );
        Assertions.assertTrue(bookingResponse.isSetBookingError(), "expected booking error, since offer expired");
        final BookingErrorResponseType bookingError = bookingResponse.getBookingError();
        final BookingErrorCodeType reason = bookingError.getReason();
        switch (reason) {
            case UNKNOWN___OFFER___ID:
                // this is what we wanted
                break;
            default:
                // note: UNKNOWN_CUSTOMER_ID would also be ok
                Assertions.fail("unexpected error: " + reason);
        }
    }

    @RepeatedTest(3)
    public void expiryTest() {
        final GetOffersRequest getOffersRequest = createRandomGetOffersRequest();
        final GetOffersResponse getOffersResponse = shuttleService.getOffersFunction(getOffersRequest);

        final GetOffersSuccessResponseType offersSuccess = getOffersResponse.getGetOffersSuccess();
        if (null == offersSuccess) {
            return;
        }
        final List<OfferType> offers = offersSuccess.getOffers();
        if (offers.isEmpty()) {
            return;
        }
        final OfferType minOffer = offers.stream().min(Comparator.comparing(OfferType::getOfferValidUntil)).orElseThrow(Error::new);
        final Duration timeStillValid = Duration.between(OffsetDateTime.now(), minOffer.getOfferValidUntil());
        if (!timeStillValid.minus(Duration.ofMinutes(2)).isNegative()) {
            return;
        }
        while (!Duration.between(OffsetDateTime.now(), minOffer.getOfferValidUntil()).isNegative()) {
            try {
                Thread.sleep(Duration.between(OffsetDateTime.now(), minOffer.getOfferValidUntil()).toMillis());
            } catch (final InterruptedException ignored) {
            }
        }
        final BookingResponse bookingResponse = shuttleService.bookingFunction(new BookingRequest()
                .withOfferID(minOffer.getOfferID())
                .withCustomerID(UUID.randomUUID().toString())
        );
        Assertions.assertTrue(bookingResponse.isSetBookingError(), "expected booking error, since offer expired");
        final BookingErrorResponseType bookingError = bookingResponse.getBookingError();
        final BookingErrorCodeType reason = bookingError.getReason();
        switch (reason) {
            case OFFER___EXPIRED:
                // this is what we wanted
                break;
            default:
                // note: UNKNOWN_OFFER_ID (and UNKNOWN_CUSTOMER_ID) would also be ok
                Assertions.fail("unexpected error: " + reason);
        }
    }

    private GetOffersRequest createRandomGetOffersRequest() {
        final BigInteger maxWalkwayFrom = BigInteger.valueOf(random.nextInt(150, 251));
        final BigInteger maxWalkwayTo = BigInteger.valueOf(random.nextInt(50, 151));
        final Duration timeFlexibility = Duration.ofMinutes(random.nextInt(3, 10));
        final GetOffersRequest getOffersRequest = new GetOffersRequest()
                .withFrom(new FlexibleGeoLocationType()
                        .withLocation(createRandomGeoLocation())
                        .withFlexibilityInMeters(maxWalkwayFrom)
                )
                .withTo(new FlexibleGeoLocationType()
                        .withLocation(createRandomGeoLocation())
                        .withFlexibilityInMeters(maxWalkwayTo))
                .withTimeFlexibility(timeFlexibility)
                .withNumberOfPassengers(BigInteger.valueOf(random.nextInt(1, 4)));
        if (random.nextBoolean()) {
            getOffersRequest.setDepartureTime(OffsetDateTime.now());
        } else {
            getOffersRequest.setArrivalTime(OffsetDateTime.now().plus(Duration.ofHours(7)));
        }
        return getOffersRequest;
    }

    private GeoLocationType createRandomGeoLocation() {
        return new GeoLocationType().withLatitude(random.nextDouble(-90, 90)).withLongitude(random.nextDouble(-180, 180));
    }

    private List<OfferType> checkGetOffersResponse(GetOffersRequest getOffersRequest,
                                                   GetOffersResponse getOffersResponse) {
        if (getOffersResponse.isSetGetOffersError()) {
            final GetOffersErrorResponseType offersError = getOffersResponse.getGetOffersError();
            Assertions.assertNotNull(offersError);
            final GetOffersErrorCodeType reason = offersError.getReason();
            Assertions.assertNotNull(reason);
            switch (reason) {
                case INTERNAL___SERVER___ERROR:
                    Assertions.fail("internal server error in service");
                    break;
                case ARRIVAL___IN___THE___PAST:
                    Assertions.fail("arrival allegedly in the past");
                    break;
                case DEPARTURE___IN___THE___PAST:
                    Assertions.fail("departure allegedly in the past");
                    break;
            }
            return Collections.emptyList();
        }
        final GetOffersSuccessResponseType getOffersSuccess = getOffersResponse.getGetOffersSuccess();
        Assertions.assertNotNull(getOffersSuccess);
        final List<OfferType> offers = getOffersSuccess.getOffers();
        Assertions.assertNotNull(offers);
        for (final OfferType offer : offers) {
            Assertions.assertNotNull(offer);
            {
                final OffsetDateTime offerValidUntil = offer.getOfferValidUntil();
                Assertions.assertNotNull(offerValidUntil);
                if (offerValidUntil.isBefore(OffsetDateTime.now())) {
                    Assertions.fail("offer " + offer + " already expired");
                }
            }
            {
                final BigInteger walkwayFrom = offer.getFrom().getWalkWayInMeters();
                Assertions.assertNotNull(walkwayFrom);
                if (!Range.between(BigInteger.ZERO, getOffersRequest.getFrom().getFlexibilityInMeters()).contains(walkwayFrom)) {
                    Assertions.fail("invalid walkway in `from` actual location: " + walkwayFrom);
                }
            }
            {
                final BigInteger walkwayTo = offer.getTo().getWalkWayInMeters();
                Assertions.assertNotNull(walkwayTo);
                if (!Range.between(BigInteger.ZERO, getOffersRequest.getTo().getFlexibilityInMeters()).contains(walkwayTo)) {
                    Assertions.fail("invalid walkway in `to` actual location: " + walkwayTo);
                }
            }
            final Duration timeFlexibility = getOffersRequest.getTimeFlexibility();
            Assertions.assertNotNull(timeFlexibility);
            if (getOffersRequest.isSetDepartureTime()) {
                final ActualTimeType actualDepartureTime = offer.getDepartureTime();
                Assertions.assertNotNull(actualDepartureTime);
                final Duration deviation = actualDepartureTime.getDeviation();
                Assertions.assertNotNull(deviation);
                final OffsetDateTime offeredDepartureTime = actualDepartureTime.getTime();
                Assertions.assertNotNull(offeredDepartureTime);
                final OffsetDateTime requestedDepartureTime = getOffersRequest.getDepartureTime();
                Assertions.assertEquals(Duration.between(requestedDepartureTime, offeredDepartureTime).abs(), deviation, "deviation calculated incorrectly");
                if (!Range.between(Duration.ofSeconds(0), timeFlexibility).contains(deviation)) {
                    Assertions.fail("invalid time deviation: " + deviation);
                }
                if (!Range.between(requestedDepartureTime, requestedDepartureTime.plus(timeFlexibility)).contains(offeredDepartureTime)) {
                    Assertions.fail("offered departure time outside of valid range");
                }
            } else {
                final ActualTimeType actualArrivalTime = offer.getArrivalTime();
                Assertions.assertNotNull(actualArrivalTime);
                final Duration deviation = actualArrivalTime.getDeviation();
                Assertions.assertNotNull(deviation);
                final OffsetDateTime offeredArrivalTime = actualArrivalTime.getTime();
                Assertions.assertNotNull(offeredArrivalTime);
                final OffsetDateTime requestedArrivalTime = getOffersRequest.getArrivalTime();
                Assertions.assertEquals(Duration.between(requestedArrivalTime, offeredArrivalTime).abs(), deviation, "deviation calculated incorrectly");
                if (!Range.between(Duration.ofSeconds(0), timeFlexibility).contains(deviation)) {
                    Assertions.fail("invalid time deviation: " + deviation);
                }
                if (!Range.between(requestedArrivalTime.minus(timeFlexibility), requestedArrivalTime).contains(offeredArrivalTime)) {
                    Assertions.fail("offered arrival time outside of valid range");
                }
            }
        }
        return offers;
    }
}
