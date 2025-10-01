package com.example.parkinglot.multilevel;

/**
 * Statistics about level utilization across the multi-level parking system
 */
public class LevelUtilizationStats {
    private final int totalSpaces;
    private final int occupiedSpaces;
    private final double overallOccupancy;
    private final double averageOccupancy;
    private final ParkingLevel mostOccupiedLevel;
    private final ParkingLevel leastOccupiedLevel;
    
    public LevelUtilizationStats(int totalSpaces, int occupiedSpaces, double overallOccupancy,
                               double averageOccupancy, ParkingLevel mostOccupiedLevel,
                               ParkingLevel leastOccupiedLevel) {
        this.totalSpaces = totalSpaces;
        this.occupiedSpaces = occupiedSpaces;
        this.overallOccupancy = overallOccupancy;
        this.averageOccupancy = averageOccupancy;
        this.mostOccupiedLevel = mostOccupiedLevel;
        this.leastOccupiedLevel = leastOccupiedLevel;
    }
    
    public int getAvailableSpaces() {
        return totalSpaces - occupiedSpaces;
    }
    
    public double getOccupancyVariance() {
        if (mostOccupiedLevel == null || leastOccupiedLevel == null) {
            return 0.0;
        }
        
        double mostOccupancyRate = (double) mostOccupiedLevel.getOccupiedSpaces() / mostOccupiedLevel.getTotalSpaces();
        double leastOccupancyRate = (double) leastOccupiedLevel.getOccupiedSpaces() / leastOccupiedLevel.getTotalSpaces();
        
        return mostOccupancyRate - leastOccupancyRate;
    }
    
    // Getters
    public int getTotalSpaces() { return totalSpaces; }
    public int getOccupiedSpaces() { return occupiedSpaces; }
    public double getOverallOccupancy() { return overallOccupancy; }
    public double getAverageOccupancy() { return averageOccupancy; }
    public ParkingLevel getMostOccupiedLevel() { return mostOccupiedLevel; }
    public ParkingLevel getLeastOccupiedLevel() { return leastOccupiedLevel; }
    
    @Override
    public String toString() {
        return String.format(
            "LevelUtilizationStats: %d/%d spaces (%.1f%% overall, %.1f%% average), " +
            "Variance: %.1f%%, Most: %s, Least: %s",
            occupiedSpaces, totalSpaces, overallOccupancy * 100, averageOccupancy * 100,
            getOccupancyVariance() * 100,
            mostOccupiedLevel != null ? mostOccupiedLevel.getLevelName() : "N/A",
            leastOccupiedLevel != null ? leastOccupiedLevel.getLevelName() : "N/A"
        );
    }
}