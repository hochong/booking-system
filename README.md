# Booking System

A weekly-calendar appointment booking system. Users click an open timeslot, enter
their name and an optional message, and the booking is approved or rejected
asynchronously based on how many people already hold that slot.

## Architecture

```
React frontend            booking-api (Spring Boot)         booking-processor (Spring Boot)
  |  GET  /api/timeslots ---->|                                        |
  |  POST /api/bookings  ---->| writes PENDING booking to DynamoDB     |
  |                           | publishes BookingCreatedEvent  ------->| Kafka topic: booking-requests
  |                           |                                        | counts APPROVED bookings for
  |  GET  /api/bookings/{id}  |                                        | that slot in DynamoDB
  |  (polls until decided) <--| reads current status from DynamoDB     | if < 6: APPROVED, else REJECTED
  |                                                                    | conditionally updates the item
  |                                                                    |
  |                                             notification-service --+ (separate consumer group -
  |                                             (Spring Boot)            same topic, own copy of every event)
  |                                               sends a notification email via SMTP
```

- **frontend** (React + TypeScript + Vite) — weekly grid, all 7 days / 9am–5pm in
  1-hour slots by default. Clicking an open cell opens a modal for name +
  optional message. After submitting, it polls the booking's status until the
  processor has made a decision.
- **booking-api** — REST API. Writes a `PENDING` booking to a DynamoDB
  `Bookings` table and publishes a `BookingCreatedEvent` to the Kafka topic
  `booking-requests`. Also serves timeslot availability for the calendar grid.
- **booking-processor** — Kafka consumer. On each event, counts existing
  `APPROVED` bookings for that timeslot; if fewer than 6 (configurable), it
  approves the booking, otherwise rejects it, and conditionally updates the
  DynamoDB item (only if still `PENDING`, so a redelivered message can't
  double-process a booking).
- **notification-service** — Kafka consumer on the same `booking-requests`
  topic (its own consumer group, so it gets every event independently of
  booking-processor). Sends a simple email via SMTP for each booking request
  to a single fixed address for now, not the requester's own — see its
  [README](services/notification-service/README.md).

### Data model

Single DynamoDB table `Bookings`:
- Partition key: `bookingId`
- GSI `timeslot-index` (partition key `timeslotStart`): lets both services
  count how many bookings exist for a given slot.
- Attributes: `timeslotStart`, `timeslotEnd`, `name`, `message`, `status`
  (`PENDING` / `APPROVED` / `REJECTED`), `createdAt`, `updatedAt`, `version`
  (optimistic locking).

## Running locally

### Quick start

```bash
./install.sh   # one-time: installs Node.js if missing, then Maven deps for both
               # services + npm install for the frontend
./run.sh       # starts Kafka, DynamoDB Local, both Java services, and the frontend
```

`install.sh` installs Node.js LTS via winget (Windows), Homebrew (macOS), or
apt (Linux) if `node`/`npm` aren't already on your PATH — skip it and install
Node yourself first if you'd rather not have the script do that.

`run.sh` tails all logs to the terminal (also written to `logs/*.log`) and
stops everything on Ctrl+C — including the underlying java/node processes
Maven and npm spawn, not just the wrapper shells. See below for what each
piece does individually, or if you'd rather run them by hand.

### 1. Start Kafka + DynamoDB Local

```bash
docker compose up -d
```

This starts:
- Kafka on `localhost:9092` (Kafka UI at `http://localhost:8081`)
- DynamoDB Local on `localhost:8000` (admin UI at `http://localhost:8002`)
- Mailpit (local SMTP catcher for notification-service) on `localhost:1025`,
  web UI to view "sent" emails at `http://localhost:8025`

### 2. Start booking-api

```bash
cd services/booking-api
mvn spring-boot:run
```

Runs on `http://localhost:8080`. On startup it auto-creates the `Bookings`
table against DynamoDB Local (disable with `AUTO_CREATE_TABLE=false` — in
real AWS, provision the table via IaC instead).

### 3. Start booking-processor

```bash
cd services/booking-processor
mvn spring-boot:run
```

Runs on `http://localhost:8090` and consumes from the `booking-requests` topic.

### 4. Start notification-service

```bash
cd services/notification-service
mvn spring-boot:run
```

Runs on `http://localhost:8095`, consumes the same `booking-requests` topic
(under its own consumer group, so it gets every event regardless of what
booking-processor does), and sends a notification email via SMTP for each one.

### 5. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. Copy `.env.example` to `.env` if you need to
point it at a non-default API URL.

## Configuration

All three Java services read these environment variables (defaults shown are
for local dev against the docker-compose stack):

| Variable | Default | Purpose |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `DYNAMODB_ENDPOINT` | `http://localhost:8000` | DynamoDB endpoint override; **unset in AWS** so the SDK resolves the real regional endpoint (booking-api/booking-processor only) |
| `AWS_REGION` | `us-east-1` | AWS region (booking-api/booking-processor only) |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | `local` / `local` | Only used for DynamoDB Local; in AWS rely on the default credential chain (IAM role, env vars) instead |
| `BOOKINGS_TABLE_NAME` | `Bookings` | DynamoDB table name (booking-api/booking-processor only) |
| `BOOKING_REQUESTS_TOPIC` | `booking-requests` | Kafka topic name |
| `BOOKING_CAPACITY_PER_TIMESLOT` | `6` | Max approved bookings per slot (booking-processor only) |
| `SMTP_HOST` / `SMTP_PORT` | `localhost` / `1025` | SMTP server (notification-service only); points at Mailpit locally |
| `NOTIFICATION_FROM_EMAIL` | `no-reply@booking-system.local` | From address on outgoing emails (notification-service only) |
| `NOTIFICATION_RECIPIENT_EMAIL` | `booking-notify@example.com` | Fixed recipient for now (notification-service only) — see its [README](services/notification-service/README.md) |

## Deploying to AWS

This repo only sets up local dev via docker-compose. For a real AWS deployment:
- Provision the `Bookings` DynamoDB table (with the `timeslot-index` GSI) via
  Terraform/CloudFormation/CDK, and set `AUTO_CREATE_TABLE=false`.
- Use Amazon MSK (or self-managed Kafka on EC2/EKS) for the `booking-requests`
  topic, and point `KAFKA_BOOTSTRAP_SERVERS` at it.
- Run all three services with IAM roles that grant DynamoDB read/write and
  Kafka access, instead of the static `local`/`local` credentials.
- Build the frontend (`npm run build`) and serve the `dist/` output from S3 +
  CloudFront, pointing `VITE_API_BASE_URL` at the deployed booking-api.
- Swap Mailpit for a real mail provider (Amazon SES, SendGrid, etc.) in
  notification-service's `SMTP_HOST`/`SMTP_PORT`, with real auth credentials.

## Known limitations / next steps

- Timeslot times are stored and compared as naive local datetime strings (no
  timezone). Fine for a single-timezone deployment; add a timezone field if
  you need multi-region support.
- The frontend learns about approval/rejection by polling every 3 seconds.
  For instant updates, swap in Server-Sent Events or WebSockets fed by a
  `booking-status-updates` Kafka topic published by booking-processor.
- `booking-api`'s pre-check on slot capacity is a fast-fail UX nicety only;
  the processor is the source of truth and uses a conditional DynamoDB update
  to stay correct under concurrent requests for the same slot.
- notification-service emails a single fixed address regardless of who made
  the booking, and fires on every request regardless of the eventual
  approve/reject decision (it consumes `booking-requests`, published before
  booking-processor decides). Once there's a `booking-status-updates` topic,
  point it there instead and use the booking's own contact info.
