package com.booking.api.model;

import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * Item shape for the "Bookings" table.
 * Partition key: bookingId.
 * GSI "timeslot-index" partition key: timeslotStart — lets the processor
 * count how many bookings already exist for a given slot.
 */
@DynamoDbBean
public class Booking {

    private String bookingId;
    private String timeslotStart;
    private String timeslotEnd;
    private String name;
    private String message;
    private String status;
    private String createdAt;
    private String updatedAt;
    private Long version;

    @DynamoDbPartitionKey
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "timeslot-index")
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @DynamoDbVersionAttribute
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
