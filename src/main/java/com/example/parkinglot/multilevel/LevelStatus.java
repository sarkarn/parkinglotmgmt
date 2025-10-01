package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.VehicleType;
import java.util.Set;

/**
 * Status information for a parking level
 */
public class LevelStatus {
    private final int levelNumber;
    private final String levelName;
    private final LevelType levelType;
    private final int totalSpaces;
    private final int availableSpaces;
    private final int occupiedSpaces;
    private final int totalCompactSpaces;
    private final int availableCompactSpaces;
    private final int occupiedCompactSpaces;
    private final int totalRegularSpaces;
    private final int availableRegularSpaces;
    private final int occupiedRegularSpaces;
    private final double occupancyRate;
    private final boolean isFull;
    private final boolean isEmpty;
    private final boolean hasElevatorAccess;
    private final boolean hasStairAccess;
    private final Set<VehicleType> allowedVehicleTypes;
    
    public LevelStatus(int levelNumber, String levelName, LevelType levelType,
                      int totalSpaces, int availableSpaces, int occupiedSpaces,
                      int totalCompactSpaces, int availableCompactSpaces, int occupiedCompactSpaces,
                      int totalRegularSpaces, int availableRegularSpaces, int occupiedRegularSpaces,
                      double occupancyRate, boolean isFull, boolean isEmpty,
                      boolean hasElevatorAccess, boolean hasStairAccess,
                      Set<VehicleType> allowedVehicleTypes) {
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.levelType = levelType;
        this.totalSpaces = totalSpaces;
        this.availableSpaces = availableSpaces;
        this.occupiedSpaces = occupiedSpaces;
        this.totalCompactSpaces = totalCompactSpaces;
        this.availableCompactSpaces = availableCompactSpaces;
        this.occupiedCompactSpaces = occupiedCompactSpaces;
        this.totalRegularSpaces = totalRegularSpaces;
        this.availableRegularSpaces = availableRegularSpaces;
        this.occupiedRegularSpaces = occupiedRegularSpaces;
        this.occupancyRate = occupancyRate;
        this.isFull = isFull;
        this.isEmpty = isEmpty;
        this.hasElevatorAccess = hasElevatorAccess;
        this.hasStairAccess = hasStairAccess;
        this.allowedVehicleTypes = allowedVehicleTypes;
    }
    
    // Getters
    public int getLevelNumber() { return levelNumber; }
    public String getLevelName() { return levelName; }
    public LevelType getLevelType() { return levelType; }
    public int getTotalSpaces() { return totalSpaces; }
    public int getAvailableSpaces() { return availableSpaces; }
    public int getOccupiedSpaces() { return occupiedSpaces; }
    public int getTotalCompactSpaces() { return totalCompactSpaces; }
    public int getAvailableCompactSpaces() { return availableCompactSpaces; }
    public int getOccupiedCompactSpaces() { return occupiedCompactSpaces; }
    public int getTotalRegularSpaces() { return totalRegularSpaces; }
    public int getAvailableRegularSpaces() { return availableRegularSpaces; }
    public int getOccupiedRegularSpaces() { return occupiedRegularSpaces; }
    public double getOccupancyRate() { return occupancyRate; }
    public boolean isFull() { return isFull; }
    public boolean isEmpty() { return isEmpty; }
    public boolean hasElevatorAccess() { return hasElevatorAccess; }
    public boolean hasStairAccess() { return hasStairAccess; }
    public Set<VehicleType> getAllowedVehicleTypes() { return allowedVehicleTypes; }
    
    @Override
    public String toString() {
        return String.format(
            "Level %d (%s) - %s: %d/%d spaces (%.1f%% occupied) | " +
            "Compact: %d/%d | Regular: %d/%d | " +
            "Access: %s%s | Vehicles: %s",
            levelNumber, levelName, levelType.getDisplayName(),
            occupiedSpaces, totalSpaces, occupancyRate * 100,
            occupiedCompactSpaces, totalCompactSpaces,
            occupiedRegularSpaces, totalRegularSpaces,
            hasElevatorAccess ? "Elevator" : "",
            hasStairAccess ? (hasElevatorAccess ? "+Stairs" : "Stairs") : "",
            allowedVehicleTypes
        );
    }
}