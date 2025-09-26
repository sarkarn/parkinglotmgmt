package com.example.parkinglot.strategy;

import com.example.parkinglot.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Unit tests for parking strategies using the Strategy Pattern
 */
public class ParkingStrategyTest {
    
    private List<List<ParkingSpace>> rows;
    
    @BeforeEach
    void setUp() {
        // Create test parking lot configuration
        // Row 1: REGULAR, REGULAR, COMPACT
        // Row 2: REGULAR, REGULAR, REGULAR  
        // Row 3: COMPACT, REGULAR, REGULAR
        rows = new ArrayList<>();
        
        // Row 1
        List<ParkingSpace> row1 = Arrays.asList(
            new ParkingSpace("R1-1", SpaceType.REGULAR),
            new ParkingSpace("R1-2", SpaceType.REGULAR),
            new ParkingSpace("R1-3", SpaceType.COMPACT)
        );
        rows.add(row1);
        
        // Row 2
        List<ParkingSpace> row2 = Arrays.asList(
            new ParkingSpace("R2-1", SpaceType.REGULAR),
            new ParkingSpace("R2-2", SpaceType.REGULAR),
            new ParkingSpace("R2-3", SpaceType.REGULAR)
        );
        rows.add(row2);
        
        // Row 3
        List<ParkingSpace> row3 = Arrays.asList(
            new ParkingSpace("R3-1", SpaceType.COMPACT),
            new ParkingSpace("R3-2", SpaceType.REGULAR),
            new ParkingSpace("R3-3", SpaceType.REGULAR)
        );
        rows.add(row3);
    }
    
    @Test
    void testMotorcycleParkingStrategy() {
        MotorcycleParkingStrategy strategy = new MotorcycleParkingStrategy();
        
        ParkingResult result = strategy.allocateSpaces("BIKE001", rows);
        assertTrue(result.isSuccess());
        assertEquals("R1-1", result.getAllocatedSpaces().get(0)); // First available space
        assertEquals(1, result.getAllocatedSpaces().size());
    }
    
    @Test
    void testMotorcycleParkingStrategyWithOccupiedSpaces() {
        // Occupy first regular space
        rows.get(0).get(0).occupy("OTHER_VEHICLE");
        
        MotorcycleParkingStrategy strategy = new MotorcycleParkingStrategy();
        ParkingResult result = strategy.allocateSpaces("BIKE001", rows);
        
        assertTrue(result.isSuccess());
        assertEquals("R1-2", result.getAllocatedSpaces().get(0)); // Next available space
    }
    
    @Test
    void testMotorcycleParkingStrategyWhenFull() {
        // Occupy all spaces
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                space.occupy("OTHER_VEHICLE");
            }
        }
        
        MotorcycleParkingStrategy strategy = new MotorcycleParkingStrategy();
        ParkingResult result = strategy.allocateSpaces("BIKE001", rows);
        
        assertFalse(result.isSuccess());
        assertEquals("No available space for motorcycle", result.getMessage());
    }
    
    @Test
    void testCarParkingStrategy() {
        CarParkingStrategy strategy = new CarParkingStrategy();
        
        ParkingResult result = strategy.allocateSpaces("CAR001", rows);
        assertTrue(result.isSuccess());
        assertEquals("R1-1", result.getAllocatedSpaces().get(0)); // First regular space
        assertEquals(1, result.getAllocatedSpaces().size());
    }
    
    @Test
    void testCarParkingStrategySkipsCompactSpaces() {
        // Occupy all regular spaces except compact ones
        rows.get(0).get(0).occupy("OTHER1"); // R1-1 (regular)
        rows.get(0).get(1).occupy("OTHER2"); // R1-2 (regular)
        rows.get(1).get(0).occupy("OTHER3"); // R2-1 (regular)
        rows.get(1).get(1).occupy("OTHER4"); // R2-2 (regular)
        rows.get(1).get(2).occupy("OTHER5"); // R2-3 (regular)
        rows.get(2).get(1).occupy("OTHER6"); // R3-2 (regular)
        rows.get(2).get(2).occupy("OTHER7"); // R3-3 (regular)
        // R1-3 (compact) and R3-1 (compact) are still free
        
        CarParkingStrategy strategy = new CarParkingStrategy();
        ParkingResult result = strategy.allocateSpaces("CAR001", rows);
        
        assertFalse(result.isSuccess());
        assertEquals("No available regular space for car", result.getMessage());
    }
    
    @Test
    void testVanParkingStrategy() {
        VanParkingStrategy strategy = new VanParkingStrategy();
        
        ParkingResult result = strategy.allocateSpaces("VAN001", rows);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAllocatedSpaces().size());
        assertTrue(result.getAllocatedSpaces().contains("R1-1"));
        assertTrue(result.getAllocatedSpaces().contains("R1-2"));
    }
    
    @Test
    void testVanParkingStrategyFindsNextContiguousSpaces() {
        // Occupy R1-2 to break contiguous spaces in row 1
        rows.get(0).get(1).occupy("OTHER_CAR");
        
        VanParkingStrategy strategy = new VanParkingStrategy();
        ParkingResult result = strategy.allocateSpaces("VAN001", rows);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAllocatedSpaces().size());
        assertTrue(result.getAllocatedSpaces().contains("R2-1"));
        assertTrue(result.getAllocatedSpaces().contains("R2-2"));
    }
    
    @Test
    void testVanParkingStrategyRequiresBothRegularSpaces() {
        // Only leave R1-3 (compact) and R2-1 (regular) available as contiguous
        rows.get(0).get(0).occupy("OTHER1");
        rows.get(0).get(1).occupy("OTHER2");
        rows.get(1).get(1).occupy("OTHER3");
        rows.get(1).get(2).occupy("OTHER4");
        rows.get(2).get(0).occupy("OTHER5");
        rows.get(2).get(1).occupy("OTHER6");
        rows.get(2).get(2).occupy("OTHER7");
        
        VanParkingStrategy strategy = new VanParkingStrategy();
        ParkingResult result = strategy.allocateSpaces("VAN001", rows);
        
        assertFalse(result.isSuccess());
        assertEquals("No two contiguous regular spaces available for van", result.getMessage());
    }
    
    @Test
    void testParkingStrategyFactory() {
        // Test that factory returns correct strategies
        assertTrue(ParkingStrategyFactory.getStrategy(VehicleType.MOTORCYCLE) instanceof MotorcycleParkingStrategy);
        assertTrue(ParkingStrategyFactory.getStrategy(VehicleType.CAR) instanceof CarParkingStrategy);
        assertTrue(ParkingStrategyFactory.getStrategy(VehicleType.VAN) instanceof VanParkingStrategy);
    }
    
    @Test
    void testParkingStrategyFactoryCustomRegistration() {
        // Create a custom strategy for testing
        ParkingStrategy customStrategy = new ParkingStrategy() {
            @Override
            public ParkingResult allocateSpaces(String vehicleId, List<List<ParkingSpace>> rows) {
                return ParkingResult.success("CUSTOM_SPACE");
            }
        };
        
        // Register custom strategy
        ParkingStrategyFactory.registerStrategy(VehicleType.MOTORCYCLE, customStrategy);
        
        // Verify it was registered
        ParkingStrategy retrieved = ParkingStrategyFactory.getStrategy(VehicleType.MOTORCYCLE);
        ParkingResult result = retrieved.allocateSpaces("TEST", rows);
        assertEquals("CUSTOM_SPACE", result.getAllocatedSpaces().get(0));
        
        // Reset to original strategy
        ParkingStrategyFactory.registerStrategy(VehicleType.MOTORCYCLE, new MotorcycleParkingStrategy());
    }
    
    @Test
    void testParkingStrategyFactoryInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParkingStrategyFactory.registerStrategy(null, new MotorcycleParkingStrategy());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ParkingStrategyFactory.registerStrategy(VehicleType.CAR, null);
        });
    }
}