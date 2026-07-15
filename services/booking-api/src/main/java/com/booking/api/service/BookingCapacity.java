package com.booking.api.service;

/** Shared capacity constant: a timeslot holds at most this many approved bookings. */
public final class BookingCapacity {

    public static final int MAX_PER_TIMESLOT = 6;

    private BookingCapacity() {
    }
}
