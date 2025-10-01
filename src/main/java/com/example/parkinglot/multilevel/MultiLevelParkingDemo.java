package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.*;
import java.util.*;

/**
 * Demonstration of the multi-level parking lot system
 */
public class MultiLevelParkingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Multi-Level Parking Lot System Demo ===\n");
        
        // Create multi-level parking lot
        MultiLevelParkingLot parkingLot = new MultiLevelParkingLot("DOWNTOWN-PLAZA");
        
        // Configure levels
        setupParkingLevels(parkingLot);
        
        // Configure elevators
        setupElevators(parkingLot);
        
        // Display initial status
        displaySystemStatus(parkingLot);
        
        // Simulate parking operations
        simulateParkingOperations(parkingLot);
        
        // Display final status
        System.out.println("\n=== Final System Status ===");
        displaySystemStatus(parkingLot);
        
        // Demonstrate elevator operations
        System.out.println("\n=== Elevator System Operations ===");
        demonstrateElevatorOperations(parkingLot);
    }
    
    private static void setupParkingLevels(MultiLevelParkingLot parkingLot) {
        System.out.println("Setting up parking levels...");
        
        // Underground Level -1 (Compact vehicles only)
        List<SpaceType[]> undergroundConfig = Arrays.asList(
            new SpaceType[]{SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT},
            new SpaceType[]{SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT}
        );
        ParkingLevel underground = new ParkingLevel(
            -1, "Underground Level B1", LevelType.UNDERGROUND,
            undergroundConfig, true, false,
            Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR)
        );
        parkingLot.addLevel(underground);
        
        // Ground Level 0 (Mixed)
        List<SpaceType[]> groundConfig = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.COMPACT, SpaceType.COMPACT},
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.REGULAR},
            new SpaceType[]{SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT}
        );
        ParkingLevel ground = new ParkingLevel(
            0, "Ground Level", LevelType.GROUND,
            groundConfig, true, true,
            Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.VAN)
        );
        parkingLot.addLevel(ground);
        
        // Elevated Level 1 (Premium with security preference)
        List<SpaceType[]> elevatedConfig = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.REGULAR},
            new SpaceType[]{SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.REGULAR}
        );
        ParkingLevel elevated = new ParkingLevel(
            1, "Elevated Level 1", LevelType.ELEVATED,
            elevatedConfig, true, true,
            Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR)
        );
        parkingLot.addLevel(elevated);
        
        // Elevated Level 2 (Motorcycles preferred)
        SpaceType[] topRow = new SpaceType[]{SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT, SpaceType.COMPACT};
        List<SpaceType[]> topConfig = new ArrayList<>();
        topConfig.add(topRow);
        ParkingLevel top = new ParkingLevel(
            2, "Top Level 2", LevelType.ELEVATED,
            topConfig, false, true, // Stairs only for motorcycles
            Set.of(VehicleType.MOTORCYCLE)
        );
        parkingLot.addLevel(top);
        
        System.out.println("✓ Configured 4 levels with different vehicle type preferences");
    }
    
    private static void setupElevators(MultiLevelParkingLot parkingLot) {
        System.out.println("Setting up elevator system...");
        
        // Main elevator (serves all levels except top)
        Elevator mainElevator = new Elevator(
            "MAIN-01", Arrays.asList(-1, 0, 1), 3, true, 0
        );
        parkingLot.addElevator(mainElevator);
        
        // Secondary elevator (ground to elevated only)
        Elevator secondaryElevator = new Elevator(
            "SEC-02", Arrays.asList(0, 1, 2), 2, false, 0
        );
        parkingLot.addElevator(secondaryElevator);
        
        System.out.println("✓ Configured 2 elevators with different service patterns");
    }
    
    private static void displaySystemStatus(MultiLevelParkingLot parkingLot) {
        MultiLevelLotStatus status = parkingLot.getLotStatus();
        
        System.out.println("Overall Status: " + status);
        System.out.println("\nLevel Details:");
        for (LevelStatus levelStatus : status.getLevelStatuses()) {
            System.out.println("  " + levelStatus);
        }
        
        System.out.println("\nElevator Status:");
        for (ElevatorStatus elevatorStatus : status.getElevatorStatuses()) {
            System.out.println("  " + elevatorStatus);
        }
        
        System.out.println("\nElevator System: " + status.getElevatorStats());
    }
    
    private static void simulateParkingOperations(MultiLevelParkingLot parkingLot) {
        System.out.println("\n=== Simulating Parking Operations ===");
        
        // Test vehicles
        List<Vehicle> vehicles = Arrays.asList(
            new Vehicle("BIKE-001", VehicleType.MOTORCYCLE),
            new Vehicle("CAR-001", VehicleType.CAR),
            new Vehicle("VAN-001", VehicleType.VAN),
            new Vehicle("BIKE-002", VehicleType.MOTORCYCLE),
            new Vehicle("CAR-002", VehicleType.CAR),
            new Vehicle("CAR-003", VehicleType.CAR),
            new Vehicle("BIKE-003", VehicleType.MOTORCYCLE)
        );
        
        // Park vehicles
        for (Vehicle vehicle : vehicles) {
            System.out.println("\nParking " + vehicle + "...");
            MultiLevelParkingResult result = parkingLot.parkVehicle(vehicle);
            System.out.println("Result: " + result.getMessage());
            
            if (result.isSuccessful()) {
                System.out.println("Assigned to: " + result.getLevelName() + 
                    " in spaces " + result.getAssignedSpaces());
                
                if (result.requiresElevator()) {
                    System.out.println("Elevator request: " + result.getElevatorRequestId());
                }
            }
        }
        
        // Show vehicle locations
        System.out.println("\n=== Vehicle Locations ===");
        for (Vehicle vehicle : vehicles) {
            Optional<VehicleLocation> location = parkingLot.findVehicleLocation(vehicle.getId());
            if (location.isPresent()) {
                System.out.println(location.get());
            }
        }
        
        // Test vehicle removal
        System.out.println("\n=== Testing Vehicle Removal ===");
        MultiLevelParkingResult removalResult = parkingLot.removeVehicle("CAR-001");
        System.out.println("Removal result: " + removalResult.getMessage());
        if (removalResult.requiresElevator()) {
            System.out.println("Elevator request for retrieval: " + removalResult.getElevatorRequestId());
        }
    }
    
    private static void demonstrateElevatorOperations(MultiLevelParkingLot parkingLot) {
        // Process elevator operations
        parkingLot.processElevatorOperations();
        
        // Show elevator statistics
        ElevatorSystemStats stats = parkingLot.getElevatorManager().getSystemStats();
        System.out.println("System Statistics: " + stats);
        
        // Demonstrate urgent request
        System.out.println("\nTesting urgent elevator request...");
        ElevatorRequest urgentRequest = parkingLot.requestUrgentElevator(
            "EMERGENCY-VEH", 1, 0, VehicleType.CAR
        );
        System.out.println("Urgent request: " + urgentRequest);
        
        // Show updated elevator statuses
        System.out.println("\nUpdated Elevator Statuses:");
        for (ElevatorStatus status : parkingLot.getElevatorManager().getElevatorStatuses()) {
            System.out.println("  " + status);
        }
    }
}