export interface TimeslotAvailability {
  timeslotStart: string;
  timeslotEnd: string;
  bookedCount: number;
  capacity: number;
  available: boolean;
}

export type BookingStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface BookingResponse {
  bookingId: string;
  timeslotStart: string;
  timeslotEnd: string;
  name: string;
  message?: string;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  timeslotStart: string;
  timeslotEnd: string;
  name: string;
  message?: string;
}

export interface ApiError {
  message: string;
}
