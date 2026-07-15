package com.booking.api.controller;

import com.booking.api.dto.BookingResponse;
import com.booking.api.dto.CreateBookingRequest;
import com.booking.api.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        var booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.getBooking(bookingId));
    }
}
