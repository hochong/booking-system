package com.booking.api.dto;

import com.booking.api.model.Booking;

public class BookingResponse {

    private String bookingId;
    private String timeslotStart;
    private String timeslotEnd;
    private String name;
    private String message;
    private String status;
    private String createdAt;
    private String updatedAt;

    public static BookingResponse from(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.bookingId = booking.getBookingId();
        response.timeslotStart = booking.getTimeslotStart();
        response.timeslotEnd = booking.getTimeslotEnd();
        response.name = booking.getName();
        response.message = booking.getMessage();
        response.status = booking.getStatus();
        response.createdAt = booking.getCreatedAt();
        response.updatedAt = booking.getUpdatedAt();
        return response;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getTimeslotStart() {
        return timeslotStart;
    }

    public String getTimeslotEnd() {
        return timeslotEnd;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
