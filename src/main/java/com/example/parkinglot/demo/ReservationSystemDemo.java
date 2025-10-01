package com.example.parkinglot.demo;

import com.example.parkinglot.ParkingLot;
import com.example.parkinglot.model.*;
import com.example.parkinglot.reservation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstration of the parking lot reservation system with advanced features:
 * - Time-based space allocation
 * - Priority queues for waitlists
 * - Expiration handling
 * - VIP and disabled customer support
 */
public class ReservationSystemDemo {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public static void main(String[] args) {
        System.out.println("=== Parking Lot Reservation System Demo ===\n");
        
        // Initialize parking lot with limited spaces
        List<SpaceType[]> rowConfigurations = Arrays.asList(
            new SpaceType[]{SpaceType.COMPACT, SpaceType.REGULAR}, // Row 1: 2 spaces
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR}  // Row 2: 2 spaces
        );
        ParkingLot parkingLot = new ParkingLot(rowConfigurations);
        
        // Create reservation policy and manager
        ReservationPolicy policy = new ReservationPolicy();
        ReservationManager reservationManager = new ReservationManager(parkingLot, policy);
        
        try {
            // Demo scenarios
            demonstrateBasicReservations(reservationManager);
            demonstratePriorityHandling(reservationManager);
            demonstrateWaitlistManagement(reservationManager);
            demonstrateTimeBasedFeatures(reservationManager);
            
        } finally {
            // Clean shutdown
            reservationManager.shutdown();
            System.out.println("\n=== Demo Complete ===");
        }
    }
    
    private static void demonstrateBasicReservations(ReservationManager manager) {
        System.out.println("1. Basic Reservation Operations");
        System.out.println("=================================");
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Regular reservation
        ReservationResult result1 = manager.createReservation(
            "CAR001", VehicleType.CAR, startTime, endTime, "REGULAR");
        printReservationResult("Regular Customer", result1);
        
        // VIP reservation
        ReservationResult result2 = manager.createReservation(
            "CAR002", VehicleType.CAR, startTime.plusMinutes(30), endTime.plusMinutes(30), "VIP");
        printReservationResult("VIP Customer", result2);
        
        // Disabled customer reservation
        ReservationResult result3 = manager.createReservation(
            "CAR003", VehicleType.CAR, startTime.plusHours(1), endTime.plusHours(1), "DISABLED");
        printReservationResult("Disabled Customer", result3);
        
        System.out.println();
    }
    
    private static void demonstratePriorityHandling(ReservationManager manager) {
        System.out.println("2. Priority Queue and Waitlist Management");
        System.out.println("========================================");
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);
        
        // Fill up available spaces
        manager.createReservation("CAR010", VehicleType.CAR, startTime, endTime, "REGULAR");
        manager.createReservation("CAR011", VehicleType.CAR, startTime, endTime, "REGULAR");
        manager.createReservation("CAR012", VehicleType.CAR, startTime, endTime, "REGULAR");
        manager.createReservation("CAR013", VehicleType.CAR, startTime, endTime, "REGULAR");
        
        // These should go to waitlist
        ReservationResult regular = manager.createReservation(
            "CAR020", VehicleType.CAR, startTime, endTime, "REGULAR");
        printReservationResult("Regular (Waitlisted)", regular);
        
        ReservationResult vip = manager.createReservation(
            "CAR021", VehicleType.CAR, startTime, endTime, "VIP");
        printReservationResult("VIP (Waitlisted)", vip);
        
        System.out.println("Waitlist size for CAR: " + manager.getWaitlistSize(VehicleType.CAR));
        System.out.println("VIP has priority: " + 
            (vip.getReservation().getPriority() < regular.getReservation().getPriority()));
        
        System.out.println();
    }
    
    private static void demonstrateWaitlistManagement(ReservationManager manager) {
        System.out.println("3. Waitlist Processing");
        System.out.println("======================");
        
        System.out.println("Current waitlist sizes:");
        for (VehicleType type : VehicleType.values()) {
            System.out.println("  " + type + ": " + manager.getWaitlistSize(type));
        }
        
        // Show upcoming reservations
        List<ParkingReservation> upcoming = manager.getUpcomingReservations();
        System.out.println("\nUpcoming reservations (next hour): " + upcoming.size());
        for (ParkingReservation reservation : upcoming) {
            System.out.println("  " + reservation.getVehicleId() + " at " + 
                reservation.getStartTime().format(TIME_FORMAT) + 
                " (Priority: " + reservation.getPriority() + ")");
        }
        
        System.out.println();
    }
    
    private static void demonstrateTimeBasedFeatures(ReservationManager manager) {
        System.out.println("4. Time-based Features");
        System.out.println("======================");
        
        // Test invalid time scenarios
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        
        ReservationResult pastResult = manager.createReservation(
            "CAR030", VehicleType.CAR, pastTime, futureTime, "REGULAR");
        printReservationResult("Past Time Reservation", pastResult);
        
        // Test invalid duration
        LocalDateTime shortStart = LocalDateTime.now().plusHours(1);
        LocalDateTime shortEnd = shortStart.plusMinutes(15); // Too short
        
        ReservationResult shortResult = manager.createReservation(
            "CAR031", VehicleType.CAR, shortStart, shortEnd, "REGULAR");
        printReservationResult("Short Duration", shortResult);
        
        // Test end time before start time
        ReservationResult invalidResult = manager.createReservation(
            "CAR032", VehicleType.CAR, futureTime, pastTime, "REGULAR");
        printReservationResult("Invalid Time Range", invalidResult);
        
        System.out.println();
    }
    
    private static void printReservationResult(String scenario, ReservationResult result) {
        System.out.println(scenario + ":");
        System.out.println("  Success: " + result.isSuccess());
        System.out.println("  Type: " + result.getType());
        System.out.println("  Message: " + result.getMessage());
        
        if (result.getReservation() != null) {
            ParkingReservation reservation = result.getReservation();
            System.out.println("  Reservation ID: " + reservation.getReservationId());
            System.out.println("  Status: " + reservation.getStatus());
            System.out.println("  Priority: " + reservation.getPriority());
            System.out.println("  Customer Type: " + reservation.getCustomerType());
            System.out.println("  Start Time: " + reservation.getStartTime().format(TIME_FORMAT));
            System.out.println("  End Time: " + reservation.getEndTime().format(TIME_FORMAT));
        }
        System.out.println();
    }
}