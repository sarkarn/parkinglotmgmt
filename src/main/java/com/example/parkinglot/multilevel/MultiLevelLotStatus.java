package com.example.parkinglot.multilevel;

import java.util.List;

/**
 * Overall status of the multi-level parking lot system
 */
public class MultiLevelLotStatus {
    private final String lotId;
    private final List<LevelStatus> levelStatuses;
    private final List<ElevatorStatus> elevatorStatuses;
    private final ElevatorSystemStats elevatorStats;
    private final int totalSpaces;
    private final int availableSpaces;
    private final int occupiedSpaces;
    private final double overallOccupancyRate;
    private final int levelCount;
    private final boolean elevatorSystemOperational;
    
    public MultiLevelLotStatus(String lotId, List<LevelStatus> levelStatuses,
                             List<ElevatorStatus> elevatorStatuses, ElevatorSystemStats elevatorStats,
                             int totalSpaces, int availableSpaces, int occupiedSpaces,
                             double overallOccupancyRate, int levelCount, boolean elevatorSystemOperational) {
        this.lotId = lotId;
        this.levelStatuses = levelStatuses;
        this.elevatorStatuses = elevatorStatuses;
        this.elevatorStats = elevatorStats;
        this.totalSpaces = totalSpaces;
        this.availableSpaces = availableSpaces;
        this.occupiedSpaces = occupiedSpaces;
        this.overallOccupancyRate = overallOccupancyRate;
        this.levelCount = levelCount;
        this.elevatorSystemOperational = elevatorSystemOperational;
    }
    
    public boolean isFull() {
        return availableSpaces == 0;
    }
    
    public boolean isEmpty() {
        return occupiedSpaces == 0;
    }
    
    public double getCapacityUtilization() {
        return overallOccupancyRate;
    }
    
    // Getters
    public String getLotId() { return lotId; }
    public List<LevelStatus> getLevelStatuses() { return levelStatuses; }
    public List<ElevatorStatus> getElevatorStatuses() { return elevatorStatuses; }
    public ElevatorSystemStats getElevatorStats() { return elevatorStats; }
    public int getTotalSpaces() { return totalSpaces; }
    public int getAvailableSpaces() { return availableSpaces; }
    public int getOccupiedSpaces() { return occupiedSpaces; }
    public double getOverallOccupancyRate() { return overallOccupancyRate; }
    public int getLevelCount() { return levelCount; }
    public boolean isElevatorSystemOperational() { return elevatorSystemOperational; }
    
    @Override
    public String toString() {
        return String.format(
            "MultiLevelLot[%s]: %d levels, %d/%d spaces (%.1f%% occupied), " +
            "Elevators: %s, System: %s",
            lotId, levelCount, occupiedSpaces, totalSpaces, overallOccupancyRate * 100,
            elevatorSystemOperational ? "Operational" : "Issues",
            isFull() ? "FULL" : isEmpty() ? "EMPTY" : "Available"
        );
    }
}