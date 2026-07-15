# booking-api

REST API for the booking system. Accepts booking requests, writes them to
DynamoDB with status `PENDING`, and publishes a `BookingCreatedEvent` to the
Kafka topic `booking-requests` for `booking-processor` to decide on. Also
serves weekly timeslot availability for the calendar UI.

See the [top-level README](../../README.md) for the full system architecture.

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/timeslots?weekStart=YYYY-MM-DD` | Availability for every slot in that 7-day week |
| `POST` | `/api/bookings` | Create a booking: `{ timeslotStart, timeslotEnd, name, message? }` |
| `GET` | `/api/bookings/{bookingId}` | Look up a booking's current status |

## Run locally

Requires Kafka and DynamoDB Local running (`docker compose up -d` from the
repo root).

```bash
mvn spring-boot:run
```

Starts on `http://localhost:8080`. On startup it auto-creates the `Bookings`
DynamoDB table if it doesn't exist (`AUTO_CREATE_TABLE=false` to disable —
do this in real AWS and provision the table via IaC instead).

## Configuration

See the environment variable table in the [top-level README](../../README.md#configuration).

## Test

```bash
mvn test
```
