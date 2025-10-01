package com.example.parkinglot.model;

/**
 * Enumeration representing the various states of a parking reservation.
 */
public enum ReservationStatus {
    PENDING,     // Reservation created, waiting for confirmation
    CONFIRMED,   // Space allocated, waiting for arrival
    ACTIVE,      // Vehicle has arrived and is parked
    EXPIRED,     // Reservation expired due to late arrival
    CANCELLED,   // Reservation cancelled by user
    COMPLETED,   // Reservation fulfilled and vehicle departed
    WAITLISTED   // No space available, added to waitlist
}