package com.example.parkinglot;

import com.example.parkinglot.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Comprehensive unit tests for the ParkingLot class
 */
public class ParkingLotTest {
    
    private ParkingLot parkingLot;
    
    @BeforeEach
    void setUp() {
        // Create a test parking lot configuration
        // Row 1: REGULAR, REGULAR, COMPACT
        // Row 2: REGULAR, REGULAR, REGULAR  
        // Row 3: COMPACT, REGULAR, REGULAR
        List<SpaceType[]> config = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.COMPACT},
            new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.REGULAR},
            new SpaceType[]{SpaceType.COMPACT, SpaceType.REGULAR, SpaceType.REGULAR}
        );
        parkingLot = new ParkingLot(config);
    }
    
    @Test
    void testParkingLotInitialization() {
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(9, status.getTotalSpaces());
        assertEquals(2, status.getTotalCompactSpaces());
        assertEquals(7, status.getTotalRegularSpaces());
        assertEquals(9, status.getAvailableSpaces());
        assertEquals(0, status.getOccupiedSpaces());
        assertTrue(status.isEmpty());
        assertFalse(status.isFull());
    }
    
    @Test
    void testParkMotorcycle() {
        ParkingResult result = parkingLot.parkVehicle("BIKE001", VehicleType.MOTORCYCLE);
        assertTrue(result.isSuccess());
        assertEquals("R1-1", result.getAllocatedSpaces().get(0)); // First available space
        
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(8, status.getAvailableSpaces());
        assertEquals(1, status.getOccupiedSpaces());
    }
    
    @Test
    void testParkCar() {
        ParkingResult result = parkingLot.parkVehicle("CAR001", VehicleType.CAR);
        assertTrue(result.isSuccess());
        assertEquals("R1-1", result.getAllocatedSpaces().get(0)); // First regular space
        
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(8, status.getAvailableSpaces());
        assertEquals(1, status.getOccupiedRegularSpaces());
    }
    
    @Test
    void testParkVan() {
        ParkingResult result = parkingLot.parkVehicle("VAN001", VehicleType.VAN);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAllocatedSpaces().size());
        assertTrue(result.getAllocatedSpaces().contains("R1-1"));
        assertTrue(result.getAllocatedSpaces().contains("R1-2"));
        
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(7, status.getAvailableSpaces());
        assertEquals(2, status.getOccupiedSpaces());
        assertEquals(2, status.getVanOccupiedSpaces());
    }
    
    @Test
    void testCarCannotParkInCompactSpace() {
        // Fill all regular spaces first
        parkingLot.parkVehicle("CAR1", VehicleType.CAR);
        parkingLot.parkVehicle("CAR2", VehicleType.CAR);
        parkingLot.parkVehicle("CAR3", VehicleType.CAR);
        parkingLot.parkVehicle("CAR4", VehicleType.CAR);
        parkingLot.parkVehicle("CAR5", VehicleType.CAR);
        parkingLot.parkVehicle("CAR6", VehicleType.CAR);
        parkingLot.parkVehicle("CAR7", VehicleType.CAR);
        
        // Try to park another car (should fail - only compact spaces left)
        ParkingResult result = parkingLot.parkVehicle("CAR8", VehicleType.CAR);
        assertFalse(result.isSuccess());
        assertEquals("No available regular space for car", result.getMessage());
    }
    
    @Test
    void testVanRequiresTwoContiguousRegularSpaces() {
        // Park a car in R1-2 to break contiguous spaces in row 1
        parkingLot.parkVehicle("CAR1", VehicleType.CAR); // Takes R1-1
        parkingLot.parkVehicle("CAR2", VehicleType.CAR); // Takes R1-2
        
        // Van should still be able to park in row 2 (R2-1, R2-2)
        ParkingResult result = parkingLot.parkVehicle("VAN001", VehicleType.VAN);
        assertTrue(result.isSuccess());
        assertTrue(result.getAllocatedSpaces().contains("R2-1"));
        assertTrue(result.getAllocatedSpaces().contains("R2-2"));
    }
    
    @Test
    void testVanCannotParkWithoutTwoContiguousRegularSpaces() {
        // Fill spaces strategically to prevent van parking
        parkingLot.parkVehicle("CAR1", VehicleType.CAR);  // R1-1
        parkingLot.parkVehicle("BIKE1", VehicleType.MOTORCYCLE); // R1-2
        parkingLot.parkVehicle("CAR2", VehicleType.CAR);  // R2-1
        parkingLot.parkVehicle("BIKE2", VehicleType.MOTORCYCLE); // R2-2
        parkingLot.parkVehicle("CAR3", VehicleType.CAR);  // R2-3
        parkingLot.parkVehicle("CAR4", VehicleType.CAR);  // R3-2
        parkingLot.parkVehicle("CAR5", VehicleType.CAR);  // R3-3
        
        ParkingResult result = parkingLot.parkVehicle("VAN001", VehicleType.VAN);
        assertFalse(result.isSuccess());
        assertEquals("No two contiguous regular spaces available for van", result.getMessage());
    }
    
    @Test
    void testDuplicateVehicleParkingReturnsExistingAllocation() {
        ParkingResult result1 = parkingLot.parkVehicle("CAR001", VehicleType.CAR);
        assertTrue(result1.isSuccess());
        String firstSpace = result1.getAllocatedSpaces().get(0);
        
        // Try to park the same vehicle again
        ParkingResult result2 = parkingLot.parkVehicle("CAR001", VehicleType.CAR);
        assertTrue(result2.isSuccess());
        assertEquals("Vehicle is already parked", result2.getMessage());
        assertEquals(firstSpace, result2.getAllocatedSpaces().get(0));
        
        // Verify only one space is occupied
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(1, status.getOccupiedSpaces());
    }
    
    @Test
    void testRemoveVehicle() {
        parkingLot.parkVehicle("CAR001", VehicleType.CAR);
        assertTrue(parkingLot.removeVehicle("CAR001"));
        
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(0, status.getOccupiedSpaces());
        assertEquals(9, status.getAvailableSpaces());
    }
    
    @Test
    void testRemoveVanFreesAllSpaces() {
        parkingLot.parkVehicle("VAN001", VehicleType.VAN);
        LotStatus beforeRemoval = parkingLot.getLotStatus();
        assertEquals(2, beforeRemoval.getOccupiedSpaces());
        assertEquals(2, beforeRemoval.getVanOccupiedSpaces());
        
        assertTrue(parkingLot.removeVehicle("VAN001"));
        
        LotStatus afterRemoval = parkingLot.getLotStatus();
        assertEquals(0, afterRemoval.getOccupiedSpaces());
        assertEquals(0, afterRemoval.getVanOccupiedSpaces());
    }
    
    @Test
    void testRemoveNonExistentVehicle() {
        assertFalse(parkingLot.removeVehicle("NONEXISTENT"));
        assertFalse(parkingLot.removeVehicle(null));
        assertFalse(parkingLot.removeVehicle(""));
    }
    
    @Test
    void testFullLotScenario() {
        // Fill all spaces
        parkingLot.parkVehicle("CAR1", VehicleType.CAR);     // R1-1
        parkingLot.parkVehicle("CAR2", VehicleType.CAR);     // R1-2
        parkingLot.parkVehicle("BIKE1", VehicleType.MOTORCYCLE); // R1-3 (compact)
        parkingLot.parkVehicle("CAR3", VehicleType.CAR);     // R2-1
        parkingLot.parkVehicle("CAR4", VehicleType.CAR);     // R2-2
        parkingLot.parkVehicle("CAR5", VehicleType.CAR);     // R2-3
        parkingLot.parkVehicle("BIKE2", VehicleType.MOTORCYCLE); // R3-1 (compact)
        parkingLot.parkVehicle("CAR6", VehicleType.CAR);     // R3-2
        parkingLot.parkVehicle("CAR7", VehicleType.CAR);     // R3-3
        
        LotStatus status = parkingLot.getLotStatus();
        assertTrue(status.isFull());
        assertEquals(0, status.getAvailableSpaces());
        assertEquals(9, status.getOccupiedSpaces());
        
        // Try to park another vehicle
        ParkingResult result = parkingLot.parkVehicle("CAR8", VehicleType.CAR);
        assertFalse(result.isSuccess());
    }
    
    @Test
    void testInvalidInputs() {
        // Null vehicle ID
        ParkingResult result1 = parkingLot.parkVehicle(null, VehicleType.CAR);
        assertFalse(result1.isSuccess());
        
        // Empty vehicle ID
        ParkingResult result2 = parkingLot.parkVehicle("", VehicleType.CAR);
        assertFalse(result2.isSuccess());
        
        // Null vehicle type
        ParkingResult result3 = parkingLot.parkVehicle("CAR001", null);
        assertFalse(result3.isSuccess());
    }
    
    @Test
    void testGetVehicleSpaces() {
        parkingLot.parkVehicle("VAN001", VehicleType.VAN);
        parkingLot.parkVehicle("CAR001", VehicleType.CAR);
        
        List<String> vanSpaces = parkingLot.getVehicleSpaces("VAN001");
        assertEquals(2, vanSpaces.size());
        
        List<String> carSpaces = parkingLot.getVehicleSpaces("CAR001");
        assertEquals(1, carSpaces.size());
        
        List<String> nonExistentSpaces = parkingLot.getVehicleSpaces("NONEXISTENT");
        assertTrue(nonExistentSpaces.isEmpty());
    }
    
    @Test
    void testRowSummaries() {
        parkingLot.parkVehicle("VAN001", VehicleType.VAN); // Occupies R1-1, R1-2
        parkingLot.parkVehicle("CAR001", VehicleType.CAR); // Occupies R2-1
        
        List<String> summaries = parkingLot.getRowSummaries();
        assertEquals(3, summaries.size());
        assertEquals("Row 1: 2 occupied, 1 available", summaries.get(0));
        assertEquals("Row 2: 1 occupied, 2 available", summaries.get(1));
        assertEquals("Row 3: 0 occupied, 3 available", summaries.get(2));
    }
    
    @Test
    void testSpaceTypeSpecificOccupancy() {
        parkingLot.parkVehicle("BIKE1", VehicleType.MOTORCYCLE); // R1-1 (regular)
        parkingLot.parkVehicle("BIKE2", VehicleType.MOTORCYCLE); // R1-2 (regular)
        
        LotStatus status = parkingLot.getLotStatus();
        assertEquals(2, status.getOccupiedRegularSpaces());
        assertEquals(0, status.getOccupiedCompactSpaces());
        assertFalse(status.isAllRegularOccupied());
        assertFalse(status.isAllCompactOccupied());
        
        // Fill remaining regular spaces
        parkingLot.parkVehicle("CAR1", VehicleType.CAR);
        parkingLot.parkVehicle("CAR2", VehicleType.CAR);
        parkingLot.parkVehicle("CAR3", VehicleType.CAR);
        parkingLot.parkVehicle("CAR4", VehicleType.CAR);
        parkingLot.parkVehicle("CAR5", VehicleType.CAR);
        
        status = parkingLot.getLotStatus();
        assertTrue(status.isAllRegularOccupied());
        assertFalse(status.isAllCompactOccupied());
    }
}

/**
 * Test class for invalid parking lot configurations
 */
class ParkingLotConfigurationTest {
    
    @Test
    void testNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ParkingLot(null);
        });
    }
    
    @Test
    void testEmptyConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ParkingLot(Collections.emptyList());
        });
    }
    
    @Test
    void testNullRowConfiguration() {
        List<SpaceType[]> config = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR},
            null // Invalid row
        );
        
        assertThrows(IllegalArgumentException.class, () -> {
            new ParkingLot(config);
        });
    }
    
    @Test
    void testEmptyRowConfiguration() {
        List<SpaceType[]> config = Arrays.asList(
            new SpaceType[]{SpaceType.REGULAR},
            new SpaceType[]{} // Invalid empty row
        );
        
        assertThrows(IllegalArgumentException.class, () -> {
            new ParkingLot(config);
        });
    }
}