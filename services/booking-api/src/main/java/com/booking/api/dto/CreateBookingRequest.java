package com.booking.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateBookingRequest {

    @NotNull
    @Future
    private LocalDateTime timeslotStart;

    @NotNull
    private LocalDateTime timeslotEnd; // end must be after start, but we don't validate that it is in future here as the booking service will check it anyway

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String message;

    public LocalDateTime getTimeslotStart() {
        return timeslotStart;
    }

    public void setTimeslotStart(LocalDateTime timeslotStart) {
        this.timeslotStart = timeslotStart;
    }

    public LocalDateTime getTimeslotEnd() {
        return timeslotEnd;
    }

    public void setTimeslotEnd(LocalDateTime timeslotEnd) {
        this.timeslotEnd = timeslotEnd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
