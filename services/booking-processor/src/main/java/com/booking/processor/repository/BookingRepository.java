package com.booking.processor.repository;

import com.booking.processor.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

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

    /**
     * Transitions a booking away from PENDING only if it is still PENDING, so a redelivered
     * Kafka message (at-least-once delivery) can't double-process an already-decided booking.
     *
     * @return true if the update was applied, false if it was already processed.
     */
    public boolean applyDecisionIfPending(Booking booking) {
        try {
            table.updateItem(UpdateItemEnhancedRequest.builder(Booking.class)
                    .item(booking)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_exists(bookingId) AND #status = :pending")
                            .putExpressionName("#status", "status")
                            .putExpressionValue(":pending", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                                    .s("PENDING").build())
                            .build())
                    .build());
            return true;
        } catch (ConditionalCheckFailedException e) {
            return false;
        }
    }
}
