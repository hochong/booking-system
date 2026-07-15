package com.booking.api.service;

import com.booking.api.dto.TimeslotAvailability;
import com.booking.api.model.BookingStatus;
import com.booking.api.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeslotService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final BookingRepository repository;
    private final LocalTime businessStart;
    private final LocalTime businessEnd;
    private final int slotMinutes;

    public TimeslotService(BookingRepository repository,
                            @Value("${app.business-hours.start:09:00}") String businessStart,
                            @Value("${app.business-hours.end:17:00}") String businessEnd,
                            @Value("${app.business-hours.slot-minutes:60}") int slotMinutes) {
        this.repository = repository;
        this.businessStart = LocalTime.parse(businessStart);
        this.businessEnd = LocalTime.parse(businessEnd);
        this.slotMinutes = slotMinutes;
    }

    /** Returns every bookable slot for the full Mon-Sun week containing weekStart, with live availability. */
    public List<TimeslotAvailability> getWeekAvailability(LocalDate weekStart) {
        LocalDate monday = weekStart.with(DayOfWeek.MONDAY);
        List<TimeslotAvailability> slots = new ArrayList<>();

        for (int day = 0; day < 7; day++) {
            LocalDate date = monday.plusDays(day);
            LocalTime cursor = businessStart;
            while (cursor.plusMinutes(slotMinutes).compareTo(businessEnd) <= 0) {
                LocalDateTime start = LocalDateTime.of(date, cursor);
                LocalDateTime end = start.plusMinutes(slotMinutes);

                String startStr = start.format(ISO);
                long booked = repository.findByTimeslotStart(startStr).stream()
                        .filter(b -> !BookingStatus.REJECTED.name().equals(b.getStatus()))
                        .count();

                slots.add(new TimeslotAvailability(startStr, end.format(ISO), (int) booked, BookingCapacity.MAX_PER_TIMESLOT));
                cursor = cursor.plusMinutes(slotMinutes);
            }
        }

        return slots;
    }
}
