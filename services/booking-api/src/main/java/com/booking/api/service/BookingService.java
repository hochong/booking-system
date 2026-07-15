package com.booking.api.service;

import com.booking.api.dto.BookingCreatedEvent;
import com.booking.api.dto.CreateBookingRequest;
import com.booking.api.model.Booking;
import com.booking.api.model.BookingStatus;
import com.booking.api.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final BookingRepository repository;
    private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;
    private final String topic;

    public BookingService(BookingRepository repository,
                           KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate,
                           @Value("${app.kafka.topic.booking-requests}") String topic) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public Booking createBooking(CreateBookingRequest request) {
        if (!request.getTimeslotEnd().isAfter(request.getTimeslotStart())) {
            throw new IllegalArgumentException("timeslotEnd must be after timeslotStart");
        }

        String timeslotStart = request.getTimeslotStart().format(ISO);
        String timeslotEnd = request.getTimeslotEnd().format(ISO);

        // Fast-fail so the UI can reject obviously-full slots immediately. The processor
        // still makes the authoritative approve/reject decision to avoid races between
        // concurrent requests for the same slot.
        long existing = repository.findByTimeslotStart(timeslotStart).stream()
                .filter(b -> !BookingStatus.REJECTED.name().equals(b.getStatus()))
                .count();
        if (existing >= BookingCapacity.MAX_PER_TIMESLOT) {
            throw new TimeslotFullException(timeslotStart);
        }

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setTimeslotStart(timeslotStart);
        booking.setTimeslotEnd(timeslotEnd);
        booking.setName(request.getName());
        booking.setMessage(request.getMessage());
        booking.setStatus(BookingStatus.PENDING.name());
        String now = Instant.now().toString();
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);

        repository.save(booking);
        log.info("Created booking {} for slot {}", booking.getBookingId(), timeslotStart);

        BookingCreatedEvent event = new BookingCreatedEvent(booking.getBookingId(), timeslotStart, timeslotEnd);
        kafkaTemplate.send(topic, timeslotStart, event);

        return booking;
    }

    public Booking getBooking(String bookingId) {
        return repository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));
    }

    public List<Booking> getBookingsForTimeslot(String timeslotStart) {
        return repository.findByTimeslotStart(timeslotStart);
    }

    public static class TimeslotFullException extends RuntimeException {
        public TimeslotFullException(String timeslotStart) {
            super("Timeslot " + timeslotStart + " is already full");
        }
    }
}
