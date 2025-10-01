package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.VehicleType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy for finding optimal parking levels in a multi-level system
 */
public class MultiLevelAllocationStrategy {
    
    /**
     * Finds the optimal level for parking a vehicle
     */
    public OptimalLevelResult findOptimalLevel(Collection<ParkingLevel> levels, VehicleType vehicleType) {
        List<ParkingLevel> suitableLevels = levels.stream()
                .filter(level -> level.canAccommodateVehicleType(vehicleType))
                .filter(level -> level.hasAppropriateAccess(vehicleType))
                .filter(level -> level.getAvailableSpacesForVehicleType(vehicleType) > 0)
                .collect(Collectors.toList());
        
        if (suitableLevels.isEmpty()) {
            return new OptimalLevelResult(false, null, "No suitable levels available for " + vehicleType);
        }
        
        // Sort levels by priority score (lower is better)
        ParkingLevel optimalLevel = suitableLevels.stream()
                .min(Comparator.comparingInt(level -> level.getLevelPriorityScore(vehicleType)))
                .orElse(null);
        
        if (optimalLevel != null) {
            String reason = String.format("Selected %s (Level %d) - Priority Score: %d, Available: %d spaces",
                    optimalLevel.getLevelName(), optimalLevel.getLevelNumber(),
                    optimalLevel.getLevelPriorityScore(vehicleType),
                    optimalLevel.getAvailableSpacesForVehicleType(vehicleType));
            
            return new OptimalLevelResult(true, optimalLevel, reason);
        }
        
        return new OptimalLevelResult(false, null, "No optimal level found");
    }
    
    /**
     * Finds multiple levels that can accommodate a large vehicle or multiple vehicles
     */
    public List<ParkingLevel> findMultipleLevels(Collection<ParkingLevel> levels, 
                                               VehicleType vehicleType, int requiredSpaces) {
        return levels.stream()
                .filter(level -> level.canAccommodateVehicleType(vehicleType))
                .filter(level -> level.hasAppropriateAccess(vehicleType))
                .filter(level -> level.getAvailableSpacesForVehicleType(vehicleType) >= requiredSpaces)
                .sorted(Comparator.comparingInt(level -> level.getLevelPriorityScore(vehicleType)))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets level utilization statistics for optimization
     */
    public LevelUtilizationStats getLevelUtilizationStats(Collection<ParkingLevel> levels) {
        if (levels.isEmpty()) {
            return new LevelUtilizationStats(0, 0, 0.0, 0.0, null, null);
        }
        
        int totalSpaces = levels.stream().mapToInt(ParkingLevel::getTotalSpaces).sum();
        int occupiedSpaces = levels.stream().mapToInt(ParkingLevel::getOccupiedSpaces).sum();
        double averageOccupancy = levels.stream()
                .mapToDouble(level -> (double) level.getOccupiedSpaces() / level.getTotalSpaces())
                .average()
                .orElse(0.0);
        
        ParkingLevel mostOccupied = levels.stream()
                .max(Comparator.comparingDouble(level -> (double) level.getOccupiedSpaces() / level.getTotalSpaces()))
                .orElse(null);
        
        ParkingLevel leastOccupied = levels.stream()
                .min(Comparator.comparingDouble(level -> (double) level.getOccupiedSpaces() / level.getTotalSpaces()))
                .orElse(null);
        
        double overallOccupancy = totalSpaces > 0 ? (double) occupiedSpaces / totalSpaces : 0.0;
        
        return new LevelUtilizationStats(totalSpaces, occupiedSpaces, overallOccupancy, 
                                       averageOccupancy, mostOccupied, leastOccupied);
    }
}