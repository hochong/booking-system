package com.booking.processor.consumer;

import com.booking.processor.service.BookingDecisionService;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BookingRequestListener implements ConsumerSeekAware {

    private static final Logger log = LoggerFactory.getLogger(BookingRequestListener.class);

    private final BookingDecisionService decisionService;

    // Kept per-partition (not just the latest callback) because a rewind request needs to
    // seek every partition this instance currently owns, and which callback is "current"
    // can change across rebalances.
    private final Map<TopicPartition, ConsumerSeekCallback> seekCallbacks = new ConcurrentHashMap<>();

    public BookingRequestListener(BookingDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @KafkaListener(topics = "${app.kafka.topic.booking-requests}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("Received booking request {} for slot {}", event.getBookingId(), event.getTimeslotStart());
        decisionService.decide(event.getBookingId(), event.getTimeslotStart());
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.keySet().forEach(tp -> seekCallbacks.put(tp, callback));
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        partitions.forEach(seekCallbacks::remove);
    }

    /**
     * Rewinds every partition this consumer currently owns to the earliest offset at or
     * after the given time, so booking-requests published since then get redelivered and
     * reprocessed. Safe to call at any time - the seek is queued and applied on the
     * listener's own thread before its next poll, per Spring Kafka's ConsumerSeekAware contract.
     */
    public void rewindToTimestamp(long epochMillis) {
        seekCallbacks.forEach((tp, callback) -> callback.seekToTimestamp(tp.topic(), tp.partition(), epochMillis));
    }

    /** Rewinds every partition this consumer currently owns back to its earliest available offset. */
    public void rewindToBeginning() {
        seekCallbacks.forEach((tp, callback) -> callback.seekToBeginning(tp.topic(), tp.partition()));
    }
}
