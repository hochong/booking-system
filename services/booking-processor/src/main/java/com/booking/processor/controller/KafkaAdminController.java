package com.booking.processor.controller;

import com.booking.processor.consumer.BookingRequestListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Lets an operator replay booking-requests events - e.g. after fixing a bug in
 * BookingDecisionService, to reprocess bookings that were already (mis)decided.
 * Safe to call at any time: BookingDecisionService.decide() only acts on bookings still
 * PENDING, so redelivering already-decided events is a no-op for them.
 *
 * Unauthenticated and intended for local/internal use only - put this behind admin auth
 * (or drop it entirely) before running in a shared environment.
 */
@RestController
public class KafkaAdminController {

    private final BookingRequestListener listener;

    public KafkaAdminController(BookingRequestListener listener) {
        this.listener = listener;
    }

    /** Rewinds to the earliest offset at/after the given instant (defaults to now if omitted). */
    @PostMapping("/admin/kafka/rewind")
    public ResponseEntity<Void> rewindToTimestamp(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        listener.rewindToTimestamp((since != null ? since : Instant.now()).toEpochMilli());
        return ResponseEntity.accepted().build();
    }

    /** Rewinds all the way back to the earliest retained offset - replays the whole topic. */
    @PostMapping("/admin/kafka/rewind/beginning")
    public ResponseEntity<Void> rewindToBeginning() {
        listener.rewindToBeginning();
        return ResponseEntity.accepted().build();
    }
}
