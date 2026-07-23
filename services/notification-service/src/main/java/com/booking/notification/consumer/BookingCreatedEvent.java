package com.booking.notification.consumer;

/** Mirrors booking-api's BookingCreatedEvent - the payload read from "booking-requests". */
public class BookingCreatedEvent {

    private String bookingId;
    private String timeslotStart;
    private String timeslotEnd;

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
