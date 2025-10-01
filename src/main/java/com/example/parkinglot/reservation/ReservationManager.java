package com.example.parkinglot.reservation;

import com.example.parkinglot.model.*;
import com.example.parkinglot.ParkingLot;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Manages parking reservations with time-based allocation, priority queues, and expiration handling.
 */
public class ReservationManager {
    private final Map<String, ParkingReservation> reservations;
    private final Map<VehicleType, PriorityQueue<ParkingReservation>> waitlists;
    private final Map<LocalDateTime, Set<String>> timeBasedIndex;
    private final ScheduledExecutorService scheduler;
    private final ReservationPolicy policy;
    private final ParkingLot parkingLot;
    
    public ReservationManager(ParkingLot parkingLot, ReservationPolicy policy) {
        this.parkingLot = parkingLot;
        this.policy = policy;
        this.reservations = new ConcurrentHashMap<>();
        this.waitlists = new EnumMap<>(VehicleType.class);
        this.timeBasedIndex = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // Initialize priority queues for each vehicle type
        for (VehicleType type : VehicleType.values()) {
            waitlists.put(type, new PriorityQueue<>());
        }
        
        // Start background tasks
        startExpirationHandler();
        startWaitlistProcessor();
    }
    
    /**
     * Creates a new reservation request
     */
    public ReservationResult createReservation(String vehicleId, VehicleType vehicleType,
                                             LocalDateTime startTime, LocalDateTime endTime,
                                             String customerType) {
        // Validation
        if (startTime.isBefore(LocalDateTime.now())) {
            return ReservationResult.failure("Cannot make reservation for past time");
        }
        
        if (endTime.isBefore(startTime)) {
            return ReservationResult.failure("End time must be after start time");
        }
        
        if (!policy.isValidDuration(startTime, endTime)) {
            return ReservationResult.failure("Reservation duration violates policy: " + 
                "min: " + policy.getMinDurationMinutes() + " minutes, " +
                "max: " + policy.getMaxDurationMinutes() + " minutes");
        }
        
        // Create reservation based on customer type
        ParkingReservation reservation;
        switch (customerType.toUpperCase()) {
            case "VIP":
                reservation = ParkingReservation.createVipReservation(vehicleId, vehicleType, startTime, endTime);
                break;
            case "DISABLED":
                reservation = ParkingReservation.createDisabledReservation(vehicleId, vehicleType, startTime, endTime);
                break;
            default:
                reservation = ParkingReservation.createRegularReservation(vehicleId, vehicleType, startTime, endTime);
        }
        
        // Try immediate allocation
        if (tryImmediateAllocation(reservation)) {
            reservations.put(reservation.getReservationId(), reservation);
            indexReservationByTime(reservation);
            return ReservationResult.success(reservation, "Reservation confirmed immediately");
        }
        
        // Add to waitlist if no immediate space available
        addToWaitlist(reservation);
        reservations.put(reservation.getReservationId(), reservation);
        indexReservationByTime(reservation);
        
        return ReservationResult.waitlisted(reservation, 
            "Added to waitlist. Position: " + getWaitlistPosition(reservation));
    }
    
    /**
     * Attempts immediate space allocation for a reservation
     */
    private boolean tryImmediateAllocation(ParkingReservation reservation) {
        // Check if spaces will be available at the requested time
        List<String> conflictingReservations = findConflictingReservations(
            reservation.getStartTime(), reservation.getEndTime(), reservation.getVehicleType());
            
        // Simulate space availability at future time
        if (willSpaceBeAvailable(reservation.getStartTime(), reservation.getVehicleType(), 
                               conflictingReservations.size())) {
            reservation.confirm("RESERVED-" + reservation.getReservationId());
            scheduleActivation(reservation);
            return true;
        }
        
        return false;
    }
    
    /**
     * Adds reservation to appropriate priority waitlist
     */
    private void addToWaitlist(ParkingReservation reservation) {
        reservation.waitlist();
        PriorityQueue<ParkingReservation> waitlist = waitlists.get(reservation.getVehicleType());
        synchronized (waitlist) {
            waitlist.offer(reservation);
        }
    }
    
    /**
     * Background task to handle reservation expirations
     */
    private void startExpirationHandler() {
        scheduler.scheduleAtFixedRate(() -> {
            reservations.values().stream()
                .filter(ParkingReservation::isExpired)
                .forEach(reservation -> {
                    reservation.expire();
                    
                    // Remove from waitlists if present
                    PriorityQueue<ParkingReservation> waitlist = waitlists.get(reservation.getVehicleType());
                    synchronized (waitlist) {
                        waitlist.remove(reservation);
                    }
                    
                    notifyCustomer(reservation, "Reservation expired due to late arrival");
                });
                
        }, 1, 1, TimeUnit.MINUTES); // Check every minute
    }
    
    /**
     * Background task to process waitlists
     */
    private void startWaitlistProcessor() {
        scheduler.scheduleAtFixedRate(() -> {
            for (VehicleType vehicleType : VehicleType.values()) {
                processWaitlist(vehicleType);
            }
        }, 5, 5, TimeUnit.MINUTES); // Process every 5 minutes
    }
    
    private void processWaitlist(VehicleType vehicleType) {
        PriorityQueue<ParkingReservation> waitlist = waitlists.get(vehicleType);
        
        synchronized (waitlist) {
            while (!waitlist.isEmpty()) {
                ParkingReservation reservation = waitlist.peek();
                
                // Skip expired reservations
                if (reservation.isExpired()) {
                    waitlist.poll();
                    reservation.expire();
                    continue;
                }
                
                // Try to allocate space
                if (tryImmediateAllocation(reservation)) {
                    waitlist.poll(); // Remove from waitlist
                    notifyCustomer(reservation, "Reservation confirmed! Space allocated.");
                } else {
                    break; // Can't allocate, stop processing for now
                }
            }
        }
    }
    
    // Helper methods
    private void indexReservationByTime(ParkingReservation reservation) {
        timeBasedIndex.computeIfAbsent(reservation.getStartTime(), k -> ConcurrentHashMap.newKeySet())
                     .add(reservation.getReservationId());
    }
    
    private List<String> findConflictingReservations(LocalDateTime startTime, LocalDateTime endTime, 
                                                     VehicleType vehicleType) {
        return reservations.values().stream()
            .filter(r -> r.getVehicleType() == vehicleType)
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.ACTIVE)
            .filter(r -> overlapsWithTimeRange(r, startTime, endTime))
            .map(ParkingReservation::getReservationId)
            .collect(Collectors.toList());
    }
    
    private boolean overlapsWithTimeRange(ParkingReservation reservation, 
                                        LocalDateTime startTime, LocalDateTime endTime) {
        return reservation.getStartTime().isBefore(endTime) && 
               reservation.getEndTime().isAfter(startTime);
    }
    
    private boolean willSpaceBeAvailable(LocalDateTime time, VehicleType vehicleType, int conflictCount) {
        // Simplified logic - in reality, this would be more sophisticated
        LotStatus status = parkingLot.getLotStatus();
        int totalSpaces = status.getTotalSpaces();
        int estimatedOccupancy = conflictCount;
        
        return (totalSpaces - estimatedOccupancy) > 0;
    }
    
    private int getWaitlistPosition(ParkingReservation reservation) {
        PriorityQueue<ParkingReservation> waitlist = waitlists.get(reservation.getVehicleType());
        synchronized (waitlist) {
            List<ParkingReservation> sortedList = new ArrayList<>(waitlist);
            return sortedList.indexOf(reservation) + 1;
        }
    }
    
    private void scheduleActivation(ParkingReservation reservation) {
        long delayMinutes = reservation.getMinutesUntilStart();
        if (delayMinutes > 0) {
            scheduler.schedule(() -> {
                notifyCustomer(reservation, "Reservation starts in 15 minutes. Please arrive soon.");
            }, Math.max(0, delayMinutes - 15), TimeUnit.MINUTES);
        }
    }
    
    private void notifyCustomer(ParkingReservation reservation, String message) {
        // In a real system, this would send SMS, email, or push notifications
        System.out.println("NOTIFICATION for " + reservation.getVehicleId() + ": " + message);
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    // Public accessor methods for testing and monitoring
    public Optional<ParkingReservation> getReservation(String reservationId) {
        return Optional.ofNullable(reservations.get(reservationId));
    }
    
    public int getWaitlistSize(VehicleType vehicleType) {
        PriorityQueue<ParkingReservation> waitlist = waitlists.get(vehicleType);
        synchronized (waitlist) {
            return waitlist.size();
        }
    }
    
    public List<ParkingReservation> getActiveReservations() {
        return reservations.values().stream()
            .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
            .collect(Collectors.toList());
    }
    
    public List<ParkingReservation> getUpcomingReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);
        
        return reservations.values().stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .filter(r -> r.getStartTime().isAfter(now) && r.getStartTime().isBefore(nextHour))
            .sorted(Comparator.comparing(ParkingReservation::getStartTime))
            .collect(Collectors.toList());
    }
}