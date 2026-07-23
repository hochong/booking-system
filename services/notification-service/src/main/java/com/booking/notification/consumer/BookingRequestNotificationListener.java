package com.booking.notification.consumer;

import com.booking.notification.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingRequestNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(BookingRequestNotificationListener.class);

    private final EmailNotificationService emailNotificationService;

    public BookingRequestNotificationListener(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.booking-requests}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("Received booking request {} for slot {}", event.getBookingId(), event.getTimeslotStart());
        emailNotificationService.notifyBookingCreated(event);
    }
}
