# notification-service

Kafka consumer that sends a notification email for every booking request.
Listens on `booking-requests` — the same topic `booking-processor` consumes —
under its own consumer group, so it gets an independent copy of every event
regardless of what `booking-processor` decides.

See the [top-level README](../../README.md) for the full system architecture.

## Current behavior (dummy / placeholder)

For each `BookingCreatedEvent` received, it sends a plain-text email via SMTP
to a single **fixed** recipient address (`NOTIFICATION_RECIPIENT_EMAIL`, not
the person who made the booking) with the booking ID and timeslot. This fires
on every booking *request*, not on the eventual approve/reject decision,
since `booking-requests` is published before `booking-processor` decides.

This is intentionally a placeholder to prove the wiring works end-to-end.
Reasonable next steps once there's a real requirement:
- Consume a `booking-status-updates` topic (published by booking-processor
  after its decision) instead, so the email reflects APPROVED/REJECTED.
- Use the requester's own contact info instead of one fixed address — that
  means adding an email field to the booking request/DynamoDB item.
- Replace the plain-text `SimpleMailMessage` with an HTML template.

## Run locally

Requires Kafka and Mailpit running (`docker compose up -d` from the repo
root) and `booking-api` running so there's something to publish events.

```bash
mvn spring-boot:run
```

Starts on `http://localhost:8095` (only exposes `/actuator/health` and
`/actuator/info` — there's no public REST API here, it's a pure consumer).
Sent emails land in Mailpit's web UI at `http://localhost:8025` — nothing is
sent to a real inbox.

## Configuration

See the environment variable table in the [top-level README](../../README.md#configuration).
Notification-specific ones: `SMTP_HOST`, `SMTP_PORT`, `NOTIFICATION_FROM_EMAIL`,
`NOTIFICATION_RECIPIENT_EMAIL`.

## Test

```bash
mvn test
```
