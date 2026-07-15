package com.booking.processor.consumer;

import com.booking.processor.service.BookingDecisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingRequestListener {

    private static final Logger log = LoggerFactory.getLogger(BookingRequestListener.class);

    private final BookingDecisionService decisionService;

    public BookingRequestListener(BookingDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @KafkaListener(topics = "${app.kafka.topic.booking-requests}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("Received booking request {} for slot {}", event.getBookingId(), event.getTimeslotStart());
        decisionService.decide(event.getBookingId(), event.getTimeslotStart());
    }
}
