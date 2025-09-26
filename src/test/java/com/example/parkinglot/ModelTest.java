package com.example.parkinglot;

import com.example.parkinglot.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Unit tests for the model classes
 */
public class ModelTest {
    
    @Test
    public void testVehicleCreation() {
        Vehicle car = new Vehicle("CAR001", VehicleType.CAR);
        assertEquals("CAR001", car.getId());
        assertEquals(VehicleType.CAR, car.getType());
    }
    
    @Test
    public void testVehicleInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle(null, VehicleType.CAR);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("", VehicleType.CAR);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("CAR001", null);
        });
    }
    
    @Test
    public void testVehicleEquality() {
        Vehicle car1 = new Vehicle("CAR001", VehicleType.CAR);
        Vehicle car2 = new Vehicle("CAR001", VehicleType.CAR);
        Vehicle car3 = new Vehicle("CAR002", VehicleType.CAR);
        
        assertEquals(car1, car2);
        assertNotEquals(car1, car3);
        assertEquals(car1.hashCode(), car2.hashCode());
    }
    
    @Test
    public void testParkingSpaceOccupancy() {
        ParkingSpace space = new ParkingSpace("R1-1", SpaceType.REGULAR);
        assertFalse(space.isOccupied());
        assertNull(space.getOccupiedBy());
        
        space.occupy("CAR001");
        assertTrue(space.isOccupied());
        assertEquals("CAR001", space.getOccupiedBy());
        
        space.vacate();
        assertFalse(space.isOccupied());
        assertNull(space.getOccupiedBy());
    }
    
    @Test
    public void testParkingSpaceDoubleOccupancy() {
        ParkingSpace space = new ParkingSpace("R1-1", SpaceType.REGULAR);
        space.occupy("CAR001");
        
        assertThrows(IllegalStateException.class, () -> {
            space.occupy("CAR002");
        });
    }
    
    @Test
    public void testParkingResultSuccess() {
        ParkingResult result = ParkingResult.success("R1-1");
        assertTrue(result.isSuccess());
        assertEquals("Vehicle parked successfully", result.getMessage());
        assertEquals(1, result.getAllocatedSpaces().size());
        assertEquals("R1-1", result.getAllocatedSpaces().get(0));
    }
    
    @Test
    public void testParkingResultFailure() {
        ParkingResult result = ParkingResult.failure("No space available");
        assertFalse(result.isSuccess());
        assertEquals("No space available", result.getMessage());
        assertTrue(result.getAllocatedSpaces().isEmpty());
    }
    
    @Test
    public void testParkingResultAlreadyParked() {
        List<String> existingSpaces = Arrays.asList("R1-1", "R1-2");
        ParkingResult result = ParkingResult.alreadyParked(existingSpaces);
        assertTrue(result.isSuccess());
        assertEquals("Vehicle is already parked", result.getMessage());
        assertEquals(2, result.getAllocatedSpaces().size());
    }
    
    @Test
    public void testLotStatusCreation() {
        LotStatus status = new LotStatus(10, 3, 7, 8, 2, 6, 2, 1, 1, 0, false, false, false, false);
        
        assertEquals(10, status.getTotalSpaces());
        assertEquals(3, status.getTotalCompactSpaces());
        assertEquals(7, status.getTotalRegularSpaces());
        assertEquals(8, status.getAvailableSpaces());
        assertEquals(2, status.getOccupiedSpaces());
        assertFalse(status.isFull());
        assertFalse(status.isEmpty());
    }
}