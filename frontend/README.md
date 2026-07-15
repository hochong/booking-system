# frontend

React + TypeScript + Vite frontend for the booking system. Shows a full
7-day weekly calendar grid; clicking an open slot opens a modal to enter a name and
optional message, then polls for the approve/reject decision.

See the [top-level README](../README.md) for the full system architecture.

## Run locally

Requires `booking-api` running (default `http://localhost:8080`).

```bash
npm install
npm run dev
```

Starts on `http://localhost:5173`. Copy `.env.example` to `.env` to point at
a non-default API URL via `VITE_API_BASE_URL`.

## Structure

- `src/App.tsx` — week navigation, data loading, booking submission, status polling
- `src/components/WeekCalendar.tsx` — renders the grid from `TimeslotAvailability[]`
- `src/components/BookingModal.tsx` — name + optional message form
- `src/api/client.ts` — fetch wrappers for the booking-api endpoints

## Build

```bash
npm run build
```

Outputs static assets to `dist/` — deploy behind any static host (S3 +
CloudFront, Nginx, etc.) pointed at your deployed `booking-api`.
