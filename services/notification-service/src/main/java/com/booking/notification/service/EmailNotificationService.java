package com.booking.notification.service;

import com.booking.notification.consumer.BookingCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String recipientEmail;

    public EmailNotificationService(JavaMailSender mailSender,
                                     @Value("${app.notification.from-email}") String fromEmail,
                                     @Value("${app.notification.recipient-email}") String recipientEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.recipientEmail = recipientEmail;
    }

    /**
     * Sends a plain-text notification for a newly created booking. The recipient is a single
     * fixed address for now (not per-booking) - swap this for the requester's own address, or
     * a templated HTML email, once there's a real place to send it.
     */
    public void notifyBookingCreated(BookingCreatedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("New booking request received");
        message.setText("""
                A new booking request was submitted.

                Booking ID: %s
                Timeslot: %s - %s
                """.formatted(event.getBookingId(), event.getTimeslotStart(), event.getTimeslotEnd()));

        mailSender.send(message);
        log.info("Sent booking-created notification for {} to {}", event.getBookingId(), recipientEmail);
    }
}
