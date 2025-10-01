package com.example.parkinglot.reservation;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Defines policies and constraints for parking reservations.
 */
public class ReservationPolicy {
    private final int minDurationMinutes;
    private final int maxDurationMinutes;
    private final int maxAdvanceBookingDays;
    private final int graceperiodMinutes;
    
    public ReservationPolicy() {
        this.minDurationMinutes = 30;          // Minimum 30 minutes
        this.maxDurationMinutes = 24 * 60;     // Maximum 24 hours
        this.maxAdvanceBookingDays = 30;       // Can book up to 30 days in advance
        this.graceperiodMinutes = 15;          // 15 minutes grace period for arrival
    }
    
    public ReservationPolicy(int minDurationMinutes, int maxDurationMinutes, 
                           int maxAdvanceBookingDays, int graceperiodMinutes) {
        this.minDurationMinutes = minDurationMinutes;
        this.maxDurationMinutes = maxDurationMinutes;
        this.maxAdvanceBookingDays = maxAdvanceBookingDays;
        this.graceperiodMinutes = graceperiodMinutes;
    }
    
    public boolean isValidDuration(LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return minutes >= minDurationMinutes && minutes <= maxDurationMinutes;
    }
    
    public boolean isValidAdvanceBooking(LocalDateTime startTime) {
        long daysInAdvance = Duration.between(LocalDateTime.now(), startTime).toDays();
        return daysInAdvance <= maxAdvanceBookingDays;
    }
    
    // Getters
    public int getMinDurationMinutes() { return minDurationMinutes; }
    public int getMaxDurationMinutes() { return maxDurationMinutes; }
    public int getMaxAdvanceBookingDays() { return maxAdvanceBookingDays; }
    public int getGraceperiodMinutes() { return graceperiodMinutes; }
}