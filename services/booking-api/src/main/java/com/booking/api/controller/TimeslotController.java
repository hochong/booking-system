package com.booking.api.controller;

import com.booking.api.dto.TimeslotAvailability;
import com.booking.api.service.TimeslotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
public class TimeslotController {

    private final TimeslotService timeslotService;

    public TimeslotController(TimeslotService timeslotService) {
        this.timeslotService = timeslotService;
    }

    @GetMapping
    public List<TimeslotAvailability> getWeekAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return timeslotService.getWeekAvailability(weekStart);
    }
}
