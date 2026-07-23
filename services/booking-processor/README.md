# booking-processor

Kafka consumer that decides whether a booking gets approved or rejected.
Listens on `booking-requests`, published by `booking-api` whenever a new
booking is created.

See the [top-level README](../../README.md) for the full system architecture.

## Decision logic

For each `BookingCreatedEvent` received:
1. Look up the booking by `bookingId`. If it's not still `PENDING` (already
   processed by a prior delivery of the same message), skip it.
2. Query the `timeslot-index` GSI for all bookings sharing that
   `timeslotStart` and count how many are `APPROVED`.
3. If the count is below `BOOKING_CAPACITY_PER_TIMESLOT` (default `6`),
   approve; otherwise reject.
4. Conditionally update the DynamoDB item — the update only applies if the
   item is still `PENDING`, so redelivered Kafka messages (at-least-once
   delivery) can't double-process a booking.

## Run locally

Requires Kafka and DynamoDB Local running (`docker compose up -d` from the
repo root), and `booking-api` running first so the `Bookings` table exists.

```bash
mvn spring-boot:run
```

Starts on `http://localhost:8090` (only exposes `/actuator/health`,
`/actuator/info`, and the rewind endpoints below — there's no public
booking API here, it's mostly a pure consumer).

## Rewinding / replaying events

Since `decide()` only acts on bookings still `PENDING`, replaying already-processed
events is a no-op for them — safe to call any time.

```bash
# Replay everything published since a given instant (e.g. after fixing a bug
# in BookingDecisionService, to reprocess bookings from when it was broken):
curl -X POST "http://localhost:8090/admin/kafka/rewind?since=2026-07-15T00:00:00Z"

# Replay the whole booking-requests topic from the earliest retained offset:
curl -X POST http://localhost:8090/admin/kafka/rewind/beginning
```

Both return `202 Accepted` immediately — the actual seek happens on the
consumer's own thread before its next poll. This is unauthenticated and meant
for local/internal use; put it behind admin auth (or remove it) before
running anywhere shared.

## Configuration

See the environment variable table in the [top-level README](../../README.md#configuration).

## Test

```bash
mvn test
```
