package com.example.parkinglot.model;

import java.util.*;

/**
 * Represents the result of a parking operation.
 */
public class ParkingResult {
    private final boolean success;
    private final String message;
    private final List<String> allocatedSpaces;
    
    private ParkingResult(boolean success, String message, List<String> allocatedSpaces) {
        this.success = success;
        this.message = message;
        this.allocatedSpaces = allocatedSpaces != null ? 
            Collections.unmodifiableList(new ArrayList<>(allocatedSpaces)) : 
            Collections.emptyList();
    }
    
    public static ParkingResult success(List<String> allocatedSpaces) {
        return new ParkingResult(true, "Vehicle parked successfully", allocatedSpaces);
    }
    
    public static ParkingResult success(String allocatedSpace) {
        return new ParkingResult(true, "Vehicle parked successfully", List.of(allocatedSpace));
    }
    
    public static ParkingResult alreadyParked(List<String> existingSpaces) {
        return new ParkingResult(true, "Vehicle is already parked", existingSpaces);
    }
    
    public static ParkingResult failure(String reason) {
        return new ParkingResult(false, reason, Collections.emptyList());
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<String> getAllocatedSpaces() {
        return allocatedSpaces;
    }
    
    @Override
    public String toString() {
        return String.format("ParkingResult{success=%s, message='%s', spaces=%s}", 
                success, message, allocatedSpaces);
    }
}