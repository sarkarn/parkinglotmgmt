package com.example.parkinglot.multilevel;

import java.util.List;

/**
 * Represents the location of a vehicle in the multi-level parking system
 */
public class VehicleLocation {
    private final String vehicleId;
    private final int levelNumber;
    private final String levelName;
    private final List<String> spaceIds;
    private final LevelType levelType;
    
    public VehicleLocation(String vehicleId, int levelNumber, String levelName,
                          List<String> spaceIds, LevelType levelType) {
        this.vehicleId = vehicleId;
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.spaceIds = spaceIds;
        this.levelType = levelType;
    }
    
    public boolean isGroundLevel() {
        return levelNumber == 0;
    }
    
    public boolean requiresElevator() {
        return !isGroundLevel();
    }
    
    // Getters
    public String getVehicleId() { return vehicleId; }
    public int getLevelNumber() { return levelNumber; }
    public String getLevelName() { return levelName; }
    public List<String> getSpaceIds() { return spaceIds; }
    public LevelType getLevelType() { return levelType; }
    
    @Override
    public String toString() {
        return String.format("Vehicle[%s] @ %s (Level %d): %s - %s",
                vehicleId, levelName, levelNumber, 
                String.join(", ", spaceIds), levelType);
    }
}