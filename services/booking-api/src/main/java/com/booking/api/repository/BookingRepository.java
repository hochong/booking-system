package com.booking.api.repository;

import com.booking.api.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {

    private final DynamoDbTable<Booking> table;

    public BookingRepository(DynamoDbEnhancedClient enhancedClient,
                              @Value("${app.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Booking.class));
    }

    public Booking save(Booking booking) {
        table.putItem(booking);
        return booking;
    }

    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(bookingId).build()));
    }

    public List<Booking> findByTimeslotStart(String timeslotStart) {
        List<Booking> results = new ArrayList<>();
        table.index("timeslot-index")
                .query(QueryConditional.keyEqualTo(Key.builder().partitionValue(timeslotStart).build()))
                .forEach(page -> results.addAll(page.items()));
        return results;
    }

    public Booking update(Booking booking) {
        return table.updateItem(booking);
    }
}
