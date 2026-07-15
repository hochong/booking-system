export function getMonday(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay(); // 0 = Sunday
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  d.setHours(0, 0, 0, 0);
  return d;
}

export function formatDateOnly(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

export function formatDayLabel(isoDateTime: string): string {
  const date = new Date(isoDateTime);
  return date.toLocaleDateString(undefined, { weekday: "short", month: "short", day: "numeric" });
}

export function formatTimeLabel(isoDateTime: string): string {
  const date = new Date(isoDateTime);
  return date.toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" });
}

export function datePart(isoDateTime: string): string {
  return isoDateTime.slice(0, 10);
}

export function timePart(isoDateTime: string): string {
  return isoDateTime.slice(11, 16);
}
