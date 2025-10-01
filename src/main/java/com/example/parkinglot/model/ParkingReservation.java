package com.example.parkinglot.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a parking space reservation with time-based allocation and priority handling.
 */
public class ParkingReservation implements Comparable<ParkingReservation> {
    private final String reservationId;
    private final String vehicleId;
    private final VehicleType vehicleType;
    private final LocalDateTime reservationTime;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime expirationTime;
    private final int priority;
    private final String customerType; // REGULAR, VIP, DISABLED
    
    private ReservationStatus status;
    private String allocatedSpaces; // JSON string of space IDs
    private LocalDateTime confirmationTime;
    private LocalDateTime arrivalTime;
    private String failureReason;
    
    public ParkingReservation(String vehicleId, VehicleType vehicleType,
                      LocalDateTime startTime, LocalDateTime endTime, 
                      int priority, String customerType) {
        this.reservationId = UUID.randomUUID().toString();
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.reservationTime = LocalDateTime.now();
        this.startTime = startTime;
        this.endTime = endTime;
        this.expirationTime = startTime.plusMinutes(15); // 15-minute grace period
        this.priority = priority;
        this.customerType = customerType;
        this.status = ReservationStatus.PENDING;
    }
    
    // Static factory methods for different reservation types
    public static ParkingReservation createRegularReservation(String vehicleId, VehicleType vehicleType,
                                                     LocalDateTime startTime, LocalDateTime endTime) {
        return new ParkingReservation(vehicleId, vehicleType, startTime, endTime, 5, "REGULAR");
    }
    
    public static ParkingReservation createVipReservation(String vehicleId, VehicleType vehicleType,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        return new ParkingReservation(vehicleId, vehicleType, startTime, endTime, 1, "VIP");
    }
    
    public static ParkingReservation createDisabledReservation(String vehicleId, VehicleType vehicleType,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        return new ParkingReservation(vehicleId, vehicleType, startTime, endTime, 2, "DISABLED");
    }
    
    // Getters
    public String getReservationId() { return reservationId; }
    public String getVehicleId() { return vehicleId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public int getPriority() { return priority; }
    public String getCustomerType() { return customerType; }
    public ReservationStatus getStatus() { return status; }
    public String getAllocatedSpaces() { return allocatedSpaces; }
    public LocalDateTime getConfirmationTime() { return confirmationTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public String getFailureReason() { return failureReason; }
    
    // Status management methods
    public void confirm(String allocatedSpaces) {
        this.allocatedSpaces = allocatedSpaces;
        this.status = ReservationStatus.CONFIRMED;
        this.confirmationTime = LocalDateTime.now();
    }
    
    public void activate() {
        this.status = ReservationStatus.ACTIVE;
        this.arrivalTime = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        this.status = ReservationStatus.CANCELLED;
        this.failureReason = reason;
    }
    
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
        this.failureReason = "Customer did not arrive within grace period";
    }
    
    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
    
    public void waitlist() {
        this.status = ReservationStatus.WAITLISTED;
    }
    
    // Time-based validation methods
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(expirationTime) && 
               (status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED);
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime) &&
               (status == ReservationStatus.CONFIRMED || status == ReservationStatus.ACTIVE);
    }
    
    public boolean canBeActivated() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime.minusMinutes(5)) && // 5 minutes early check-in allowed
               now.isBefore(expirationTime) &&
               status == ReservationStatus.CONFIRMED;
    }
    
    public boolean overlaps(ParkingReservation other) {
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
    
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    public long getMinutesUntilStart() {
        return java.time.Duration.between(LocalDateTime.now(), startTime).toMinutes();
    }
    
    // Priority queue comparison - higher priority (lower number) comes first
    @Override
    public int compareTo(ParkingReservation other) {
        // Primary: Priority (VIP=1, DISABLED=2, REGULAR=5)
        int priorityComparison = Integer.compare(this.priority, other.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        
        // Secondary: Reservation time (first come, first served for same priority)
        return this.reservationTime.compareTo(other.reservationTime);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingReservation)) return false;
        ParkingReservation that = (ParkingReservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
    
    @Override
    public String toString() {
        return String.format("Reservation{id='%s', vehicle='%s', type=%s, start=%s, end=%s, status=%s, priority=%d}",
                reservationId, vehicleId, vehicleType, startTime, endTime, status, priority);
    }
}