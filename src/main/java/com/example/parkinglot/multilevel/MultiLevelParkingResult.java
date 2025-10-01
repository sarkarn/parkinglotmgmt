package com.example.parkinglot.multilevel;

import java.util.Collections;
import java.util.List;

/**
 * Result of multi-level parking operations with extended information
 */
public class MultiLevelParkingResult {
    private final boolean success;
    private final String message;
    private final List<String> assignedSpaces;
    private final String vehicleId;
    private final Integer levelNumber;
    private final String levelName;
    private final String elevatorRequestId;
    
    public MultiLevelParkingResult(boolean success, String message, List<String> assignedSpaces, 
                                 String vehicleId, Integer levelNumber, String levelName, 
                                 String elevatorRequestId) {
        this.success = success;
        this.message = message;
        this.assignedSpaces = assignedSpaces != null ? assignedSpaces : Collections.emptyList();
        this.vehicleId = vehicleId;
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.elevatorRequestId = elevatorRequestId;
    }
    
    public MultiLevelParkingResult(boolean success, String message, List<String> assignedSpaces, String vehicleId) {
        this(success, message, assignedSpaces, vehicleId, null, null, null);
    }
    
    public static MultiLevelParkingResult success(List<String> assignedSpaces, String vehicleId, 
                                                int levelNumber, String levelName) {
        return new MultiLevelParkingResult(true, "Vehicle parked successfully", assignedSpaces, 
                                         vehicleId, levelNumber, levelName, null);
    }
    
    public static MultiLevelParkingResult successWithElevator(List<String> assignedSpaces, String vehicleId,
                                                            int levelNumber, String levelName, 
                                                            String elevatorRequestId) {
        return new MultiLevelParkingResult(true, "Vehicle parked successfully with elevator request", 
                                         assignedSpaces, vehicleId, levelNumber, levelName, elevatorRequestId);
    }
    
    public static MultiLevelParkingResult failure(String message, String vehicleId) {
        return new MultiLevelParkingResult(false, message, Collections.emptyList(), vehicleId);
    }
    
    // Getters
    public boolean isSuccessful() { return success; }
    public String getMessage() { return message; }
    public List<String> getAssignedSpaces() { return assignedSpaces; }
    public String getVehicleId() { return vehicleId; }
    public Integer getLevelNumber() { return levelNumber; }
    public String getLevelName() { return levelName; }
    public String getElevatorRequestId() { return elevatorRequestId; }
    
    public boolean requiresElevator() {
        return elevatorRequestId != null;
    }
    
    @Override
    public String toString() {
        return String.format("MultiLevelParkingResult{success=%s, message='%s', spaces=%s, vehicle=%s, level=%s}",
                success, message, assignedSpaces, vehicleId, levelName);
    }
}