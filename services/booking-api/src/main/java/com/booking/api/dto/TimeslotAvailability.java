package com.booking.api.dto;

public class TimeslotAvailability {

    private String timeslotStart;
    private String timeslotEnd;
    private int bookedCount;
    private int capacity;
    private boolean available;

    public TimeslotAvailability(String timeslotStart, String timeslotEnd, int bookedCount, int capacity) {
        this.timeslotStart = timeslotStart;
        this.timeslotEnd = timeslotEnd;
        this.bookedCount = bookedCount;
        this.capacity = capacity;
        this.available = bookedCount < capacity;
    }

    public String getTimeslotStart() {
        return timeslotStart;
    }

    public String getTimeslotEnd() {
        return timeslotEnd;
    }

    public int getBookedCount() {
        return bookedCount;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isAvailable() {
        return available;
    }
}
