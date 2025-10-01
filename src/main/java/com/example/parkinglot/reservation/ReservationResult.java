package com.example.parkinglot.reservation;

import com.example.parkinglot.model.ParkingReservation;

/**
 * Represents the result of a reservation operation.
 */
public class ReservationResult {
    private final boolean success;
    private final String message;
    private final ParkingReservation reservation;
    private final ReservationResultType type;
    
    private ReservationResult(boolean success, String message, 
                            ParkingReservation reservation, ReservationResultType type) {
        this.success = success;
        this.message = message;
        this.reservation = reservation;
        this.type = type;
    }
    
    public static ReservationResult success(ParkingReservation reservation, String message) {
        return new ReservationResult(true, message, reservation, ReservationResultType.CONFIRMED);
    }
    
    public static ReservationResult waitlisted(ParkingReservation reservation, String message) {
        return new ReservationResult(true, message, reservation, ReservationResultType.WAITLISTED);
    }
    
    public static ReservationResult failure(String message) {
        return new ReservationResult(false, message, null, ReservationResultType.FAILED);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public ParkingReservation getReservation() { return reservation; }
    public ReservationResultType getType() { return type; }
    
    public enum ReservationResultType {
        CONFIRMED, WAITLISTED, FAILED
    }
}