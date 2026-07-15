import { useCallback, useEffect, useState } from "react";
import WeekCalendar from "./components/WeekCalendar";
import BookingModal from "./components/BookingModal";
import { createBooking, fetchWeekAvailability, getBooking } from "./api/client";
import type { BookingResponse, TimeslotAvailability } from "./types";
import { formatDateOnly, getMonday } from "./dateUtils";

const POLL_INTERVAL_MS = 3000;

export default function App() {
  const [weekStart, setWeekStart] = useState(() => getMonday(new Date()));
  const [slots, setSlots] = useState<TimeslotAvailability[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [selectedSlot, setSelectedSlot] = useState<TimeslotAvailability | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const [activeBooking, setActiveBooking] = useState<BookingResponse | null>(null);

  const loadWeek = useCallback(() => {
    setLoading(true);
    setLoadError(null);
    fetchWeekAvailability(formatDateOnly(weekStart))
      .then(setSlots)
      .catch((err: Error) => setLoadError(err.message))
      .finally(() => setLoading(false));
  }, [weekStart]);

  useEffect(() => {
    loadWeek();
  }, [loadWeek]);

  // Poll the booking-processor's decision until it moves off PENDING, then refresh the grid.
  useEffect(() => {
    if (!activeBooking || activeBooking.status !== "PENDING") return;

    const id = setInterval(() => {
      getBooking(activeBooking.bookingId)
        .then((updated) => {
          setActiveBooking(updated);
          if (updated.status !== "PENDING") {
            loadWeek();
          }
        })
        .catch(() => {
          // transient polling error - next tick will retry
        });
    }, POLL_INTERVAL_MS);

    return () => clearInterval(id);
  }, [activeBooking, loadWeek]);

  function handleSubmitBooking(name: string, message: string) {
    if (!selectedSlot) return;
    setSubmitting(true);
    setSubmitError(null);
    createBooking({
      timeslotStart: selectedSlot.timeslotStart,
      timeslotEnd: selectedSlot.timeslotEnd,
      name,
      message: message || undefined,
    })
      .then((booking) => {
        setActiveBooking(booking);
        setSelectedSlot(null);
        loadWeek();
      })
      .catch((err: Error) => setSubmitError(err.message))
      .finally(() => setSubmitting(false));
  }

  function shiftWeek(days: number) {
    setWeekStart((prev) => {
      const next = new Date(prev);
      next.setDate(next.getDate() + days);
      return getMonday(next);
    });
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Book an Appointment</h1>
        <div className="week-nav">
          <button className="btn secondary" onClick={() => shiftWeek(-7)}>
            &larr; Prev week
          </button>
          <span className="week-label">Week of {formatDateOnly(weekStart)}</span>
          <button className="btn secondary" onClick={() => shiftWeek(7)}>
            Next week &rarr;
          </button>
        </div>
      </header>

      {activeBooking && (
        <div className={`status-banner ${activeBooking.status.toLowerCase()}`}>
          {activeBooking.status === "PENDING" && (
            <span>Booking submitted for {activeBooking.name} — waiting for approval...</span>
          )}
          {activeBooking.status === "APPROVED" && <span>Booking approved for {activeBooking.name}.</span>}
          {activeBooking.status === "REJECTED" && (
            <span>Sorry, that slot filled up before your booking could be approved.</span>
          )}
          <button className="btn text" onClick={() => setActiveBooking(null)}>
            Dismiss
          </button>
        </div>
      )}

      {loading && <p>Loading timeslots...</p>}
      {loadError && <p className="form-error">{loadError}</p>}

      {!loading && !loadError && <WeekCalendar slots={slots} onSlotClick={setSelectedSlot} />}

      {selectedSlot && (
        <BookingModal
          slot={selectedSlot}
          submitting={submitting}
          error={submitError}
          onCancel={() => {
            setSelectedSlot(null);
            setSubmitError(null);
          }}
          onSubmit={handleSubmitBooking}
        />
      )}
    </div>
  );
}
