import { FormEvent, useState } from "react";
import type { TimeslotAvailability } from "../types";
import { formatDayLabel, formatTimeLabel } from "../dateUtils";

interface Props {
  slot: TimeslotAvailability;
  submitting: boolean;
  error: string | null;
  onCancel: () => void;
  onSubmit: (name: string, message: string) => void;
}

export default function BookingModal({ slot, submitting, error, onCancel, onSubmit }: Props) {
  const [name, setName] = useState("");
  const [message, setMessage] = useState("");

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;
    onSubmit(name.trim(), message.trim());
  }

  return (
    <div className="modal-backdrop" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>Book Appointment</h2>
        <p className="modal-subtitle">
          {formatDayLabel(slot.timeslotStart)} · {formatTimeLabel(slot.timeslotStart)} – {formatTimeLabel(slot.timeslotEnd)}
        </p>

        <form onSubmit={handleSubmit}>
          <label className="field">
            <span>Name *</span>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={100}
              required
              autoFocus
            />
          </label>

          <label className="field">
            <span>Message (optional)</span>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              maxLength={500}
              rows={3}
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <div className="modal-actions">
            <button type="button" className="btn secondary" onClick={onCancel} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn primary" disabled={submitting || !name.trim()}>
              {submitting ? "Booking..." : "Book"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
