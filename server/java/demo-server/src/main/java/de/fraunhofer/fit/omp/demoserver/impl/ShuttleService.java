package de.fraunhofer.fit.omp.demoserver.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.myshuttle.ActualGeoLocationType;
import io.myshuttle.ActualTimeType;
import io.myshuttle.BookingErrorCodeType;
import io.myshuttle.BookingErrorResponseType;
import io.myshuttle.BookingRequest;
import io.myshuttle.BookingResponse;
import io.myshuttle.BookingSuccessResponseType;
import io.myshuttle.FlexibleGeoLocationType;
import io.myshuttle.GeneralErrorType;
import io.myshuttle.GetOffersErrorCodeType;
import io.myshuttle.GetOffersErrorResponseType;
import io.myshuttle.GetOffersRequest;
import io.myshuttle.GetOffersResponse;
import io.myshuttle.GetOffersSuccessResponseType;
import io.myshuttle.OfferType;

import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class ShuttleService implements io.myshuttle.ShuttleService {

    private static final Duration OFFER_VALIDITY_DURATION = Duration.ofMinutes(4);
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Cache<String, OfferType> offerCache = CacheBuilder.newBuilder().expireAfterWrite(OFFER_VALIDITY_DURATION.plusSeconds(20)).build();

    @Override
    public GetOffersResponse getOffersFunction(GetOffersRequest body) {
        final boolean arrivalTimeSet = body.isSetArrivalTime();
        final OffsetDateTime specifiedTime = arrivalTimeSet ? body.getArrivalTime() : body.getDepartureTime();
        if (specifiedTime.isBefore(OffsetDateTime.now().minus(Duration.ofSeconds(5)))) {
            return createOffersError(arrivalTimeSet
                    ? GetOffersErrorCodeType.ARRIVAL___IN___THE___PAST
                    : GetOffersErrorCodeType.DEPARTURE___IN___THE___PAST);
        }
        return new GetOffersResponse().withGetOffersSuccess(new GetOffersSuccessResponseType()
                .withOffers(Stream.generate(() -> createOffer(body))
                                  .limit(random.nextInt(1, 12)).collect(Collectors.toList()))
        );
    }

    private OfferType createOffer(GetOffersRequest body) {
        final FlexibleGeoLocationType from = body.getFrom();
        final FlexibleGeoLocationType to = body.getTo();
        final boolean arrivalTimeSet = body.isSetArrivalTime();
        final OffsetDateTime specifiedTime = arrivalTimeSet ? body.getArrivalTime() : body.getDepartureTime();
        final Duration timeFlexibility = body.getTimeFlexibility();

        final Duration timeFlexibilityUsed = timeFlexibility.multipliedBy(random.nextInt(0, 100)).dividedBy(100);
        final Duration travelDuration = Duration.ofMinutes(random.nextInt(15, 60));

        final ActualTimeType departure, arrival;
        if (arrivalTimeSet) {
            final OffsetDateTime arrivalTime = specifiedTime.minus(timeFlexibilityUsed);
            departure = new ActualTimeType().withTime(arrivalTime.minus(travelDuration)).withDeviation(Duration.ZERO);
            arrival = new ActualTimeType().withTime(arrivalTime).withDeviation(Duration.between(arrivalTime, specifiedTime).abs());
        } else {
            final OffsetDateTime departureTime = specifiedTime.plus(timeFlexibilityUsed);
            departure = new ActualTimeType().withTime(departureTime).withDeviation(Duration.between(specifiedTime, departureTime).abs());
            arrival = new ActualTimeType().withTime(departureTime.plus(travelDuration)).withDeviation(Duration.ZERO);
        }

        final OfferType offer = new OfferType()
                .withOfferID(UUID.randomUUID().toString())
                .withFrom(
                        new ActualGeoLocationType()
                                .withLocation(from.getLocation())
                                .withWalkWayInMeters(BigInteger.valueOf(random.nextInt(from.getFlexibilityInMeters().intValue())))
                )
                .withTo(
                        new ActualGeoLocationType()
                                .withLocation(to.getLocation())
                                .withWalkWayInMeters(BigInteger.valueOf(random.nextInt(to.getFlexibilityInMeters().intValue())))
                )
                .withArrivalTime(arrival)
                .withDepartureTime(departure)
                .withPriceInEuroCents(BigInteger.valueOf(799))
                .withOfferValidUntil(OffsetDateTime.now().plus(OFFER_VALIDITY_DURATION));
        offerCache.put(offer.getOfferID(), offer);
        return offer;
    }

    private GetOffersResponse createOffersError(GetOffersErrorCodeType errorCode) {
        return new GetOffersResponse().withGetOffersError(createError(GetOffersErrorResponseType::new).withReason(errorCode));
    }

    private <E extends GeneralErrorType> E createError(final Supplier<E> ctor) {
        final E error = ctor.get();
        error.setSystemMessage("English system message describing the error.");
        error.setUserMessage("Deutsche Fehlermeldung, die dem Nutzer angezeigt wird und den eigentlichen Fehler erläutert.");
        return error;
    }

    @Override
    public BookingResponse bookingFunction(BookingRequest body) {
        final OfferType offer = offerCache.getIfPresent(body.getOfferID());
        if (null == offer) {
            return createBookingError(BookingErrorCodeType.UNKNOWN___OFFER___ID);
        }
        if (offer.getOfferValidUntil().isBefore(OffsetDateTime.now())) {
            return createBookingError(BookingErrorCodeType.OFFER___EXPIRED);
        }
        return new BookingResponse()
                .withBookingSuccess(new BookingSuccessResponseType()
                        .withBookingID("MyShuttle-" + random.ints(7).mapToObj(String::valueOf).collect(Collectors.joining()))
                        .withVehicleID("AC-MS-" + random.ints(4).mapToObj(String::valueOf).collect(Collectors.joining()))
                );
    }

    private BookingResponse createBookingError(BookingErrorCodeType unknown_offer_id) {
        return new BookingResponse().withBookingError(createError(BookingErrorResponseType::new).withReason(unknown_offer_id));
    }
}
