import type { BookingResponse, CreateBookingRequest, TimeslotAvailability } from "../types";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let message = `Request failed with status ${res.status}`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
    } catch {
      // response had no JSON body
    }
    throw new Error(message);
  }
  return res.json() as Promise<T>;
}

export function fetchWeekAvailability(weekStart: string): Promise<TimeslotAvailability[]> {
  return fetch(`${BASE_URL}/api/timeslots?weekStart=${weekStart}`).then((res) =>
    handle<TimeslotAvailability[]>(res)
  );
}

export function createBooking(payload: CreateBookingRequest): Promise<BookingResponse> {
  return fetch(`${BASE_URL}/api/bookings`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).then((res) => handle<BookingResponse>(res));
}

export function getBooking(bookingId: string): Promise<BookingResponse> {
  return fetch(`${BASE_URL}/api/bookings/${bookingId}`).then((res) => handle<BookingResponse>(res));
}
