package com.example.parkinglot;

import com.example.parkinglot.model.*;
import java.util.*;

/**
 * Main class demonstrating the Parking Lot Management System
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Welcome to Parking Lot Management System!");
        System.out.println("=========================================");
        
        // Create a demo parking lot configuration
        List<SpaceType[]> config = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.COMPACT},
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.REGULAR},
            new SpaceType[]{SpaceType.COMPACT, SpaceType.REGULAR, SpaceType.REGULAR}
        );
        
        ParkingLot parkingLot = new ParkingLot(config);
        
        System.out.println("\nInitial Parking Lot Status:");
        printStatus(parkingLot);
        
        // Demonstrate parking operations
        System.out.println("\nDemonstrating Parking Operations:");
        System.out.println("=================================");
        
        // Park some vehicles
        demonstrateParkingOperation(parkingLot, "BIKE001", VehicleType.MOTORCYCLE);
        demonstrateParkingOperation(parkingLot, "CAR001", VehicleType.CAR);
        demonstrateParkingOperation(parkingLot, "VAN001", VehicleType.VAN);
        demonstrateParkingOperation(parkingLot, "CAR002", VehicleType.CAR);
        
        System.out.println("\nCurrent Status after parking:");
        printStatus(parkingLot);
        
        // Try to park the same vehicle again
        System.out.println("\nTrying to park the same vehicle again:");
        demonstrateParkingOperation(parkingLot, "CAR001", VehicleType.CAR);
        
        // Remove a vehicle
        System.out.println("\nRemoving vehicle CAR001:");
        boolean removed = parkingLot.removeVehicle("CAR001");
        System.out.println("Removal result: " + removed);
        
        System.out.println("\nFinal Status:");
        printStatus(parkingLot);
        
        // Show row summaries
        System.out.println("\nRow Summaries:");
        for (String summary : parkingLot.getRowSummaries()) {
            System.out.println(summary);
        }
    }
    
    private static void demonstrateParkingOperation(ParkingLot parkingLot, String vehicleId, VehicleType vehicleType) {
        System.out.println(String.format("\nParking %s (%s):", vehicleId, vehicleType));
        ParkingResult result = parkingLot.parkVehicle(vehicleId, vehicleType);
        System.out.println("Result: " + result.getMessage());
        if (result.isSuccess() && !result.getAllocatedSpaces().isEmpty()) {
            System.out.println("Allocated spaces: " + result.getAllocatedSpaces());
        }
    }
    
    private static void printStatus(ParkingLot parkingLot) {
        LotStatus status = parkingLot.getLotStatus();
        System.out.println("Total spaces: " + status.getTotalSpaces());
        System.out.println("Available spaces: " + status.getAvailableSpaces());
        System.out.println("Occupied spaces: " + status.getOccupiedSpaces());
        System.out.println("Compact spaces (total/available/occupied): " + 
                          status.getTotalCompactSpaces() + "/" + 
                          status.getAvailableCompactSpaces() + "/" + 
                          status.getOccupiedCompactSpaces());
        System.out.println("Regular spaces (total/available/occupied): " + 
                          status.getTotalRegularSpaces() + "/" + 
                          status.getAvailableRegularSpaces() + "/" + 
                          status.getOccupiedRegularSpaces());
        System.out.println("Van occupied spaces: " + status.getVanOccupiedSpaces());
        System.out.println("Lot full: " + status.isFull());
        System.out.println("Lot empty: " + status.isEmpty());
        System.out.println("All compact spaces occupied: " + status.isAllCompactOccupied());
        System.out.println("All regular spaces occupied: " + status.isAllRegularOccupied());
    }
}