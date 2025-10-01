package com.example.parkinglot.multilevel;

import java.util.List;

/**
 * Status information for an elevator
 */
public class ElevatorStatus {
    private final String elevatorId;
    private final int currentLevel;
    private final ElevatorState state;
    private final boolean maintenanceMode;
    private final int currentOccupants;
    private final int maxCapacity;
    private final int queueLength;
    private final List<Integer> servedLevels;
    private final boolean vanCompatible;
    private final long lastOperationTime;
    private final String currentRequestId;
    
    public ElevatorStatus(String elevatorId, int currentLevel, ElevatorState state,
                         boolean maintenanceMode, int currentOccupants, int maxCapacity,
                         int queueLength, List<Integer> servedLevels, boolean vanCompatible,
                         long lastOperationTime, String currentRequestId) {
        this.elevatorId = elevatorId;
        this.currentLevel = currentLevel;
        this.state = state;
        this.maintenanceMode = maintenanceMode;
        this.currentOccupants = currentOccupants;
        this.maxCapacity = maxCapacity;
        this.queueLength = queueLength;
        this.servedLevels = servedLevels;
        this.vanCompatible = vanCompatible;
        this.lastOperationTime = lastOperationTime;
        this.currentRequestId = currentRequestId;
    }
    
    public double getCapacityUsage() {
        return maxCapacity > 0 ? (double) currentOccupants / maxCapacity : 0.0;
    }
    
    public boolean isAvailable() {
        return !maintenanceMode && state != ElevatorState.MAINTENANCE && 
               state != ElevatorState.OUT_OF_SERVICE;
    }
    
    public long getIdleTime() {
        return state == ElevatorState.IDLE ? System.currentTimeMillis() - lastOperationTime : 0;
    }
    
    // Getters
    public String getElevatorId() { return elevatorId; }
    public int getCurrentLevel() { return currentLevel; }
    public ElevatorState getState() { return state; }
    public boolean isMaintenanceMode() { return maintenanceMode; }
    public int getCurrentOccupants() { return currentOccupants; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getQueueLength() { return queueLength; }
    public List<Integer> getServedLevels() { return servedLevels; }
    public boolean isVanCompatible() { return vanCompatible; }
    public long getLastOperationTime() { return lastOperationTime; }
    public String getCurrentRequestId() { return currentRequestId; }
    
    @Override
    public String toString() {
        return String.format(
            "Elevator[%s]: L%d, %s, %d/%d (%.1f%%), Queue: %d, Levels: %s, Van: %s",
            elevatorId, currentLevel, state, currentOccupants, maxCapacity,
            getCapacityUsage() * 100, queueLength, servedLevels, 
            vanCompatible ? "Yes" : "No"
        );
    }
}