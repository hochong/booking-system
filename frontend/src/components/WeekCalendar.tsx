import type { TimeslotAvailability } from "../types";
import { datePart, formatDayLabel, formatTimeLabel, timePart } from "../dateUtils";

interface Props {
  slots: TimeslotAvailability[];
  onSlotClick: (slot: TimeslotAvailability) => void;
}

export default function WeekCalendar({ slots, onSlotClick }: Props) {
  if (slots.length === 0) {
    return <p className="empty-state">No timeslots configured for this week.</p>;
  }

  const dayKeys = Array.from(new Set(slots.map((s) => datePart(s.timeslotStart)))).sort();
  const timeKeys = Array.from(new Set(slots.map((s) => timePart(s.timeslotStart)))).sort();

  const slotsByDayAndTime = new Map<string, TimeslotAvailability>();
  for (const slot of slots) {
    slotsByDayAndTime.set(`${datePart(slot.timeslotStart)}|${timePart(slot.timeslotStart)}`, slot);
  }

  const dayHeaderLabel = new Map<string, string>();
  const timeRowLabel = new Map<string, string>();
  for (const slot of slots) {
    dayHeaderLabel.set(datePart(slot.timeslotStart), formatDayLabel(slot.timeslotStart));
    timeRowLabel.set(timePart(slot.timeslotStart), formatTimeLabel(slot.timeslotStart));
  }

  return (
    <div className="calendar-scroll">
      <table className="calendar">
        <thead>
          <tr>
            <th className="time-col" />
            {dayKeys.map((day) => (
              <th key={day}>{dayHeaderLabel.get(day)}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {timeKeys.map((time) => (
            <tr key={time}>
              <td className="time-col">{timeRowLabel.get(time) ?? time}</td>
              {dayKeys.map((day) => {
                const slot = slotsByDayAndTime.get(`${day}|${time}`);
                if (!slot) {
                  return <td key={day} className="slot-cell empty" />;
                }
                return (
                  <td key={day} className="slot-cell">
                    <button
                      type="button"
                      className={slot.available ? "slot available" : "slot full"}
                      disabled={!slot.available}
                      onClick={() => onSlotClick(slot)}
                    >
                      {slot.available ? `${slot.bookedCount}/${slot.capacity} booked` : "Full"}
                    </button>
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
