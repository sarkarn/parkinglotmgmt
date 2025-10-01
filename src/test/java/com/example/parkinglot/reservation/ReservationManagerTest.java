package com.example.parkinglot.reservation;

import com.example.parkinglot.ParkingLot;
import com.example.parkinglot.model.*;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the reservation system.
 */
public class ReservationManagerTest {
    
    private ParkingLot parkingLot;
    private ReservationPolicy policy;
    private ReservationManager reservationManager;
    
    @BeforeEach
    void setUp() {
        // Create a parking lot with limited spaces for testing
        List<SpaceType[]> rowConfigurations = Arrays.asList(
            new SpaceType[]{SpaceType.COMPACT, SpaceType.REGULAR}, // Row 1: 2 spaces
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR}  // Row 2: 2 spaces
        );
        parkingLot = new ParkingLot(rowConfigurations);
        
        // Create a standard reservation policy
        policy = new ReservationPolicy();
        
        // Create reservation manager
        reservationManager = new ReservationManager(parkingLot, policy);
    }
    
    @AfterEach
    void tearDown() {
        if (reservationManager != null) {
            reservationManager.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should create immediate reservation when space is available")
    void testImmediateReservation() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getReservation());
        assertEquals(ReservationStatus.CONFIRMED, result.getReservation().getStatus());
    }
    
    @Test
    @DisplayName("Should reject reservation for past time")
    void testPastTimeReservation() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("past time"));
    }
    
    @Test
    @DisplayName("Should reject reservation with invalid duration")
    void testInvalidDurationReservation() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(15); // Too short
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("duration violates policy"));
    }
    
    @Test
    @DisplayName("Should create VIP reservation with high priority")
    void testVipReservation() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "VIP");
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getReservation());
        assertEquals(10, result.getReservation().getPriority()); // VIP priority
    }
    
    @Test
    @DisplayName("Should create disabled reservation with accessible space preference")
    void testDisabledReservation() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "DISABLED");
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getReservation());
        assertEquals("DISABLED", result.getReservation().getCustomerType());
    }
    
    @Test
    @DisplayName("Should add to waitlist when no immediate space available")
    void testWaitlistFunctionality() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Fill up all car spaces with reservations
        reservationManager.createReservation("CAR001", VehicleType.CAR, startTime, endTime, "REGULAR");
        reservationManager.createReservation("CAR002", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        // This should go to waitlist
        ReservationResult result = reservationManager.createReservation(
            "CAR003", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertEquals(ReservationResult.ReservationResultType.WAITLISTED, result.getType());
        assertNotNull(result.getReservation());
        assertEquals(ReservationStatus.WAITLISTED, result.getReservation().getStatus());
        assertTrue(result.getMessage().contains("waitlist"));
    }
    
    @Test
    @DisplayName("Should prioritize VIP customers in waitlist")
    void testWaitlistPriority() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Fill up car spaces
        reservationManager.createReservation("CAR001", VehicleType.CAR, startTime, endTime, "REGULAR");
        reservationManager.createReservation("CAR002", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        // Add regular customer to waitlist
        ReservationResult regularResult = reservationManager.createReservation(
            "CAR003", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        // Add VIP customer to waitlist
        ReservationResult vipResult = reservationManager.createReservation(
            "CAR004", VehicleType.CAR, startTime, endTime, "VIP");
        
        assertEquals(ReservationResult.ReservationResultType.WAITLISTED, regularResult.getType());
        assertEquals(ReservationResult.ReservationResultType.WAITLISTED, vipResult.getType());
        
        // VIP should have higher priority
        assertTrue(vipResult.getReservation().getPriority() > regularResult.getReservation().getPriority());
    }
    
    @Test
    @DisplayName("Should track waitlist size correctly")
    void testWaitlistSizeTracking() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        assertEquals(0, reservationManager.getWaitlistSize(VehicleType.CAR));
        
        // Fill up car spaces and add to waitlist
        reservationManager.createReservation("CAR001", VehicleType.CAR, startTime, endTime, "REGULAR");
        reservationManager.createReservation("CAR002", VehicleType.CAR, startTime, endTime, "REGULAR");
        reservationManager.createReservation("CAR003", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertEquals(1, reservationManager.getWaitlistSize(VehicleType.CAR));
    }
    
    @Test
    @DisplayName("Should retrieve reservations by ID")
    void testReservationRetrieval() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertTrue(result.isSuccess());
        String reservationId = result.getReservation().getReservationId();
        
        assertTrue(reservationManager.getReservation(reservationId).isPresent());
        assertEquals("CAR123", reservationManager.getReservation(reservationId).get().getVehicleId());
    }
    
    @Test
    @DisplayName("Should list upcoming reservations")
    void testUpcomingReservations() {
        LocalDateTime startTime1 = LocalDateTime.now().plusMinutes(30);
        LocalDateTime endTime1 = startTime1.plusHours(1);
        LocalDateTime startTime2 = LocalDateTime.now().plusMinutes(45);
        LocalDateTime endTime2 = startTime2.plusHours(1);
        
        reservationManager.createReservation("CAR001", VehicleType.CAR, startTime1, endTime1, "REGULAR");
        reservationManager.createReservation("CAR002", VehicleType.CAR, startTime2, endTime2, "REGULAR");
        
        List<ParkingReservation> upcoming = reservationManager.getUpcomingReservations();
        
        assertEquals(2, upcoming.size());
        // Should be sorted by start time
        assertTrue(upcoming.get(0).getStartTime().isBefore(upcoming.get(1).getStartTime()));
    }
    
    @Test
    @DisplayName("Should handle different vehicle types independently")
    void testDifferentVehicleTypes() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Reserve spaces for different vehicle types
        ReservationResult carResult = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "REGULAR");
        ReservationResult motorcycleResult = reservationManager.createReservation(
            "BIKE123", VehicleType.MOTORCYCLE, startTime, endTime, "REGULAR");
        ReservationResult vanResult = reservationManager.createReservation(
            "VAN123", VehicleType.VAN, startTime, endTime, "REGULAR");
        
        assertTrue(carResult.isSuccess());
        assertTrue(motorcycleResult.isSuccess());
        assertTrue(vanResult.isSuccess());
        
        // All should be confirmed since they're different vehicle types
        assertEquals(ReservationStatus.CONFIRMED, carResult.getReservation().getStatus());
        assertEquals(ReservationStatus.CONFIRMED, motorcycleResult.getReservation().getStatus());
        assertEquals(ReservationStatus.CONFIRMED, vanResult.getReservation().getStatus());
    }
    
    @Test
    @DisplayName("Should handle end time before start time")
    void testInvalidTimeRange() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1); // Before start time
        
        ReservationResult result = reservationManager.createReservation(
            "CAR123", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("End time must be after start time"));
    }
    
    @Test
    @DisplayName("Should handle concurrent reservation requests")
    void testConcurrentReservations() throws InterruptedException {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Simulate concurrent requests
        Thread thread1 = new Thread(() -> {
            reservationManager.createReservation("CAR001", VehicleType.CAR, startTime, endTime, "REGULAR");
        });
        
        Thread thread2 = new Thread(() -> {
            reservationManager.createReservation("CAR002", VehicleType.CAR, startTime, endTime, "REGULAR");
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Both should complete without exceptions
        // At least one should be successful
        assertTrue(reservationManager.getReservation("CAR001").isPresent() || 
                  reservationManager.getReservation("CAR002").isPresent());
    }
}