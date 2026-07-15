package com.booking.processor.service;

import com.booking.processor.model.Booking;
import com.booking.processor.model.BookingStatus;
import com.booking.processor.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BookingDecisionService {

    private static final Logger log = LoggerFactory.getLogger(BookingDecisionService.class);

    private final BookingRepository repository;
    private final int capacityPerTimeslot;

    public BookingDecisionService(BookingRepository repository,
                                   @Value("${app.booking.capacity-per-timeslot:6}") int capacityPerTimeslot) {
        this.repository = repository;
        this.capacityPerTimeslot = capacityPerTimeslot;
    }

    /** Approves or rejects the given booking based on how many bookings already hold that timeslot. */
    public void decide(String bookingId, String timeslotStart) {
        Booking booking = repository.findById(bookingId).orElse(null);
        if (booking == null) { // Booking not found - could be that the booking was deleted or not yet created
            log.warn("Booking {} not found (yet) - skipping", bookingId);
            return;
        }

        if (!BookingStatus.PENDING.name().equals(booking.getStatus())) { // Booking already processed by another consumer
            log.info("Booking {} already processed as {} - skipping", bookingId, booking.getStatus());
            return;
        }

        List<Booking> slotBookings = repository.findByTimeslotStart(timeslotStart);
        long approvedCount = slotBookings.stream()
                .filter(b -> BookingStatus.APPROVED.name().equals(b.getStatus()))
                .count();

        BookingStatus decision = approvedCount < capacityPerTimeslot ? BookingStatus.APPROVED : BookingStatus.REJECTED;

        booking.setStatus(decision.name());
        booking.setUpdatedAt(Instant.now().toString());

        boolean applied = repository.applyDecisionIfPending(booking);
        if (applied) { // Booking was still pending and the decision was applied successfully, can be approved or rejected
            log.info("Booking {} for slot {} -> {} ({} of {} approved slots taken)",
                    bookingId, timeslotStart, decision, approvedCount, capacityPerTimeslot);
        } else {
            log.info("Booking {} was already processed by another consumer - skipping", bookingId);
        }
    }
}
