package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Comprehensive tests for the multi-level parking system
 */
public class MultiLevelParkingLotTest {
    
    private MultiLevelParkingLot parkingLot;
    
    @BeforeEach
    void setUp() {
        parkingLot = new MultiLevelParkingLot("TEST-LOT");
        setupTestLevels();
        setupTestElevators();
    }
    
    private void setupTestLevels() {
        // Ground level - mixed spaces
        List<SpaceType[]> groundConfig = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR},
            new SpaceType[]{SpaceType.COMPACT, SpaceType.COMPACT}
        );
        ParkingLevel ground = new ParkingLevel(
            0, "Ground", LevelType.GROUND,
            groundConfig, true, true,
            Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.VAN)
        );
        parkingLot.addLevel(ground);
        
        // Elevated level - cars and motorcycles
        SpaceType[] elevatedRow = new SpaceType[]{SpaceType.REGULAR, SpaceType.COMPACT};
        List<SpaceType[]> elevatedConfig = new ArrayList<>();
        elevatedConfig.add(elevatedRow);
        ParkingLevel elevated = new ParkingLevel(
            1, "Level 1", LevelType.ELEVATED,
            elevatedConfig, true, false,
            Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR)
        );
        parkingLot.addLevel(elevated);
    }
    
    private void setupTestElevators() {
        Elevator mainElevator = new Elevator("MAIN", Arrays.asList(0, 1), 2, true, 0);
        parkingLot.addElevator(mainElevator);
    }
    
    @Test
    @DisplayName("Should park vehicle on optimal level")
    void testParkVehicleOptimalLevel() {
        Vehicle car = new Vehicle("CAR-001", VehicleType.CAR);
        MultiLevelParkingResult result = parkingLot.parkVehicle(car);
        
        assertTrue(result.isSuccessful());
        assertNotNull(result.getLevelNumber());
        assertFalse(result.getAssignedSpaces().isEmpty());
        assertEquals("CAR-001", result.getVehicleId());
    }
    
    @Test
    @DisplayName("Should request elevator for non-ground level parking")
    void testElevatorRequestForParking() {
        // Fill ground level first to force upper level allocation
        fillGroundLevel();
        
        Vehicle car = new Vehicle("CAR-002", VehicleType.CAR);
        MultiLevelParkingResult result = parkingLot.parkVehicle(car);
        
        assertTrue(result.isSuccessful());
        assertEquals(Integer.valueOf(1), result.getLevelNumber());
        assertNotNull(result.getElevatorRequestId());
        assertTrue(result.requiresElevator());
    }
    
    @Test
    @DisplayName("Should find vehicle location correctly")
    void testFindVehicleLocation() {
        Vehicle motorcycle = new Vehicle("BIKE-001", VehicleType.MOTORCYCLE);
        MultiLevelParkingResult parkResult = parkingLot.parkVehicle(motorcycle);
        assertTrue(parkResult.isSuccessful());
        
        Optional<VehicleLocation> location = parkingLot.findVehicleLocation("BIKE-001");
        assertTrue(location.isPresent());
        assertEquals("BIKE-001", location.get().getVehicleId());
        assertEquals(parkResult.getLevelNumber(), Integer.valueOf(location.get().getLevelNumber()));
    }
    
    @Test
    @DisplayName("Should remove vehicle and request elevator if needed")
    void testRemoveVehicleWithElevator() {
        // Park on elevated level
        fillGroundLevel();
        Vehicle car = new Vehicle("CAR-003", VehicleType.CAR);
        MultiLevelParkingResult parkResult = parkingLot.parkVehicle(car);
        assertTrue(parkResult.isSuccessful());
        
        // Remove vehicle
        MultiLevelParkingResult removeResult = parkingLot.removeVehicle("CAR-003");
        assertTrue(removeResult.isSuccessful());
        assertTrue(removeResult.requiresElevator());
        assertNotNull(removeResult.getElevatorRequestId());
    }
    
    @Test
    @DisplayName("Should handle vehicle type restrictions correctly")
    void testVehicleTypeRestrictions() {
        Vehicle van = new Vehicle("VAN-001", VehicleType.VAN);
        MultiLevelParkingResult result = parkingLot.parkVehicle(van);
        
        // Van should be restricted to ground level only
        assertTrue(result.isSuccessful());
        assertEquals(Integer.valueOf(0), result.getLevelNumber());
        assertEquals("Ground", result.getLevelName());
    }
    
    @Test
    @DisplayName("Should provide accurate system status")
    void testSystemStatus() {
        // Park some vehicles
        parkingLot.parkVehicle(new Vehicle("CAR-001", VehicleType.CAR));
        parkingLot.parkVehicle(new Vehicle("BIKE-001", VehicleType.MOTORCYCLE));
        
        MultiLevelLotStatus status = parkingLot.getLotStatus();
        
        assertNotNull(status);
        assertEquals("TEST-LOT", status.getLotId());
        assertEquals(2, status.getLevelCount());
        assertTrue(status.getOccupiedSpaces() > 0);
        assertTrue(status.isElevatorSystemOperational());
    }
    
    @Test
    @DisplayName("Should handle elevator maintenance mode")
    void testElevatorMaintenanceMode() {
        boolean result = parkingLot.setElevatorMaintenanceMode("MAIN", true);
        assertTrue(result);
        
        MultiLevelLotStatus status = parkingLot.getLotStatus();
        List<ElevatorStatus> elevatorStatuses = status.getElevatorStatuses();
        assertEquals(1, elevatorStatuses.size());
        assertTrue(elevatorStatuses.get(0).isMaintenanceMode());
    }
    
    @Test
    @DisplayName("Should process elevator operations correctly")
    void testElevatorOperations() {
        // Request elevator service
        ElevatorRequest request = parkingLot.requestUrgentElevator(
            "TEST-VEH", 0, 1, VehicleType.CAR
        );
        assertNotNull(request);
        
        // Process operations
        parkingLot.processElevatorOperations();
        
        // Check request status
        Optional<ElevatorRequest> status = parkingLot.getElevatorRequestStatus(request.getRequestId());
        assertTrue(status.isPresent());
    }
    
    @Test
    @DisplayName("Should cancel elevator requests successfully")
    void testCancelElevatorRequest() {
        ElevatorRequest request = parkingLot.requestElevatorForRetrieval(
            "TEST-VEH", 1, 0, VehicleType.CAR
        );
        assertNotNull(request);
        
        boolean cancelled = parkingLot.cancelElevatorRequest(request.getRequestId());
        assertTrue(cancelled);
    }
    
    @Test
    @DisplayName("Should get available spaces for vehicle type")
    void testGetAvailableSpacesForVehicleType() {
        int carSpaces = parkingLot.getAvailableSpacesForVehicleType(VehicleType.CAR);
        int motorcycleSpaces = parkingLot.getAvailableSpacesForVehicleType(VehicleType.MOTORCYCLE);
        int vanSpaces = parkingLot.getAvailableSpacesForVehicleType(VehicleType.VAN);
        
        assertTrue(carSpaces > 0);
        assertTrue(motorcycleSpaces >= carSpaces); // Motorcycles can use any space
        assertTrue(vanSpaces <= carSpaces); // Vans are more restricted
    }
    
    @Test
    @DisplayName("Should handle no available spaces gracefully")
    void testNoAvailableSpaces() {
        // Fill all spaces
        fillAllSpaces();
        
        Vehicle newCar = new Vehicle("OVERFLOW-CAR", VehicleType.CAR);
        MultiLevelParkingResult result = parkingLot.parkVehicle(newCar);
        
        assertFalse(result.isSuccessful());
        assertEquals("OVERFLOW-CAR", result.getVehicleId());
    }
    
    @Test
    @DisplayName("Should get levels for specific vehicle type")
    void testGetLevelsForVehicleType() {
        List<ParkingLevel> carLevels = parkingLot.getLevelsForVehicleType(VehicleType.CAR);
        List<ParkingLevel> vanLevels = parkingLot.getLevelsForVehicleType(VehicleType.VAN);
        
        assertEquals(2, carLevels.size()); // Both levels support cars
        assertEquals(1, vanLevels.size());  // Only ground level supports vans
    }
    
    // Helper methods
    
    private void fillGroundLevel() {
        // Fill all ground level spaces to force next vehicle to upper level
        for (int i = 0; i < 5; i++) { // Force more than ground level capacity
            Vehicle vehicle = new Vehicle("FILLER-" + i, VehicleType.CAR);
            parkingLot.parkVehicle(vehicle);
            // Stop if we've filled the ground level
            int availableOnGround = parkingLot.getLevelsForVehicleType(VehicleType.CAR).get(0).getAvailableSpacesForVehicleType(VehicleType.CAR);
            if (availableOnGround == 0) {
                break;
            }
        }
    }
    
    private void fillAllSpaces() {
        // Fill all available spaces across all levels
        for (int i = 0; i < 10; i++) { // More than total spaces
            Vehicle vehicle = new Vehicle("FILL-" + i, VehicleType.CAR);
            MultiLevelParkingResult result = parkingLot.parkVehicle(vehicle);
            if (!result.isSuccessful()) {
                break; // No more spaces available
            }
        }
    }
}