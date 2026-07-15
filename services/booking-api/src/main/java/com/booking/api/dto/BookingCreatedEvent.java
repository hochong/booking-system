package com.booking.api.dto;

/**
 * Payload published to the "booking-requests" Kafka topic. The booking-processor
 * service consumes this to decide whether to approve or reject the booking.
 */
public class BookingCreatedEvent {

    private String bookingId;
    private String timeslotStart;
    private String timeslotEnd;

    public BookingCreatedEvent() {
    }

    public BookingCreatedEvent(String bookingId, String timeslotStart, String timeslotEnd) {
        this.bookingId = bookingId;
        this.timeslotStart = timeslotStart;
        this.timeslotEnd = timeslotEnd;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTimeslotStart() {
        return timeslotStart;
    }

    public void setTimeslotStart(String timeslotStart) {
        this.timeslotStart = timeslotStart;
    }

    public String getTimeslotEnd() {
        return timeslotEnd;
    }

    public void setTimeslotEnd(String timeslotEnd) {
        this.timeslotEnd = timeslotEnd;
    }
}
