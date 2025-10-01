package com.example.parkinglot.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a parking space reservation with time-based allocation.
 */
public class Reservation {
    private final String reservationId;
    private final String vehicleId;
    private final VehicleType vehicleType;
    private final LocalDateTime reservationTime;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime expirationTime;
    private ReservationStatus status;
    private String allocatedSpaces; // JSON string of space IDs
    private int priority;
    
    public Reservation(String reservationId, String vehicleId, VehicleType vehicleType,
                      LocalDateTime startTime, LocalDateTime endTime, int priority) {
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.reservationTime = LocalDateTime.now();
        this.startTime = startTime;
        this.endTime = endTime;
        this.expirationTime = startTime.plusMinutes(15); // 15 min grace period
        this.status = ReservationStatus.PENDING;
        this.priority = priority;
    }
    
    // Getters
    public String getReservationId() { return reservationId; }
    public String getVehicleId() { return vehicleId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public ReservationStatus getStatus() { return status; }
    public String getAllocatedSpaces() { return allocatedSpaces; }
    public int getPriority() { return priority; }
    
    // Status management
    public void confirm(String allocatedSpaces) {
        this.allocatedSpaces = allocatedSpaces;
        this.status = ReservationStatus.CONFIRMED;
    }
    
    public void activate() {
        this.status = ReservationStatus.ACTIVE;
    }
    
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
    
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }
    
    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime) && 
               status == ReservationStatus.PENDING;
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime) &&
               (status == ReservationStatus.CONFIRMED || status == ReservationStatus.ACTIVE);
    }
    
    public boolean canBeActivated() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime.minusMinutes(5)) && // 5 min early check-in
               status == ReservationStatus.CONFIRMED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
    
    @Override
    public String toString() {
        return String.format("Reservation{id='%s', vehicle='%s', type=%s, start=%s, end=%s, status=%s}",
                reservationId, vehicleId, vehicleType, startTime, endTime, status);
    }
}