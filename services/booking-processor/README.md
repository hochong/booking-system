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

Starts on `http://localhost:8090` (only exposes `/actuator/health` and
`/actuator/info` — there's no public REST API here, it's a pure consumer).

## Configuration

See the environment variable table in the [top-level README](../../README.md#configuration).

## Test

```bash
mvn test
```
