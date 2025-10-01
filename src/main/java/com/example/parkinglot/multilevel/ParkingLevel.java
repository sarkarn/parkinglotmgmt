package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.*;
import java.util.*;

/**
 * Represents a single level in a multi-level parking structure.
 */
public class ParkingLevel {
    private final int levelNumber;
    private final String levelName; // e.g., "Ground", "Level 1", "Level B1"
    private final LevelType levelType;
    private final List<List<ParkingSpace>> rows;
    private final Map<String, List<String>> vehicleToSpaces;
    private final Map<String, String> spaceToVehicle;
    private final boolean hasElevatorAccess;
    private final boolean hasStairAccess;
    private final Set<VehicleType> allowedVehicleTypes;
    private final int maxCapacity;
    
    public ParkingLevel(int levelNumber, String levelName, LevelType levelType,
                       List<SpaceType[]> rowConfigurations, boolean hasElevatorAccess,
                       boolean hasStairAccess, Set<VehicleType> allowedVehicleTypes) {
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.levelType = levelType;
        this.hasElevatorAccess = hasElevatorAccess;
        this.hasStairAccess = hasStairAccess;
        this.allowedVehicleTypes = new HashSet<>(allowedVehicleTypes);
        this.vehicleToSpaces = new HashMap<>();
        this.spaceToVehicle = new HashMap<>();
        this.rows = new ArrayList<>();
        
        initializeRows(rowConfigurations);
        this.maxCapacity = calculateMaxCapacity();
    }
    
    private void initializeRows(List<SpaceType[]> rowConfigurations) {
        for (int rowIndex = 0; rowIndex < rowConfigurations.size(); rowIndex++) {
            SpaceType[] rowConfig = rowConfigurations.get(rowIndex);
            List<ParkingSpace> row = new ArrayList<>();
            
            for (int spaceIndex = 0; spaceIndex < rowConfig.length; spaceIndex++) {
                String spaceId = String.format("L%d-R%d-%d", levelNumber, rowIndex + 1, spaceIndex + 1);
                row.add(new ParkingSpace(spaceId, rowConfig[spaceIndex]));
            }
            rows.add(row);
        }
    }
    
    private int calculateMaxCapacity() {
        return rows.stream()
                .mapToInt(List::size)
                .sum();
    }
    
    /**
     * Checks if this level can accommodate the given vehicle type
     */
    public boolean canAccommodateVehicleType(VehicleType vehicleType) {
        return allowedVehicleTypes.contains(vehicleType);
    }
    
    /**
     * Checks if this level has appropriate access for the vehicle type
     */
    public boolean hasAppropriateAccess(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return hasStairAccess || hasElevatorAccess;
            case CAR:
            case VAN:
                return hasElevatorAccess; // Cars and vans need elevator/ramp access
            default:
                return false;
        }
    }
    
    /**
     * Gets available spaces count for specific vehicle type
     */
    public int getAvailableSpacesForVehicleType(VehicleType vehicleType) {
        if (!canAccommodateVehicleType(vehicleType)) {
            return 0;
        }
        
        return (int) rows.stream()
                .flatMap(List::stream)
                .filter(space -> !space.isOccupied())
                .filter(space -> isSpaceSuitableForVehicle(space, vehicleType))
                .count();
    }
    
    private boolean isSpaceSuitableForVehicle(ParkingSpace space, VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return true; // Motorcycles can use any space
            case CAR:
                return space.getType() == SpaceType.REGULAR || space.getType() == SpaceType.COMPACT; // Cars can use regular or compact spaces
            case VAN:
                return space.getType() == SpaceType.REGULAR; // Vans need regular spaces only
            default:
                return false;
        }
    }
    
    /**
     * Calculates the priority score for this level for a given vehicle type
     * Lower score = higher priority
     */
    public int getLevelPriorityScore(VehicleType vehicleType) {
        if (!canAccommodateVehicleType(vehicleType) || !hasAppropriateAccess(vehicleType)) {
            return Integer.MAX_VALUE; // Cannot accommodate
        }
        
        int baseScore = 0;
        
        // Level type preferences
        switch (levelType) {
            case GROUND:
                baseScore = 1; // Highest priority for convenience
                break;
            case UNDERGROUND:
                baseScore = 3; // Lower priority due to accessibility
                break;
            case ELEVATED:
                baseScore = 2; // Medium priority
                break;
        }
        
        // Vehicle type specific adjustments
        switch (vehicleType) {
            case MOTORCYCLE:
                if (levelType == LevelType.ELEVATED) {
                    baseScore -= 1; // Motorcycles prefer elevated levels (security)
                }
                break;
            case CAR:
                // Cars have neutral preference adjustments
                break;
            case VAN:
                if (levelType == LevelType.GROUND) {
                    baseScore -= 1; // Vans prefer ground level (easier access)
                }
                break;
        }
        
        // Occupancy factor - prefer levels with more available space
        double occupancyRate = (double) getOccupiedSpaces() / maxCapacity;
        int occupancyPenalty = (int) (occupancyRate * 5); // 0-5 penalty points
        
        return baseScore + occupancyPenalty;
    }
    
    // Getters and utility methods
    public int getLevelNumber() { return levelNumber; }
    public String getLevelName() { return levelName; }
    public LevelType getLevelType() { return levelType; }
    public List<List<ParkingSpace>> getRows() { return rows; }
    public boolean hasElevatorAccess() { return hasElevatorAccess; }
    public boolean hasStairAccess() { return hasStairAccess; }
    public Set<VehicleType> getAllowedVehicleTypes() { return new HashSet<>(allowedVehicleTypes); }
    public int getMaxCapacity() { return maxCapacity; }
    
    public Map<String, List<String>> getVehicleToSpaces() {
        return new HashMap<>(vehicleToSpaces);
    }
    
    public Map<String, String> getSpaceToVehicle() {
        return new HashMap<>(spaceToVehicle);
    }
    
    public int getTotalSpaces() {
        return rows.stream().mapToInt(List::size).sum();
    }
    
    public int getOccupiedSpaces() {
        return (int) rows.stream()
                .flatMap(List::stream)
                .filter(ParkingSpace::isOccupied)
                .count();
    }
    
    public int getAvailableSpaces() {
        return getTotalSpaces() - getOccupiedSpaces();
    }
    
    public boolean isFull() {
        return getAvailableSpaces() == 0;
    }
    
    public boolean isEmpty() {
        return getOccupiedSpaces() == 0;
    }
    
    /**
     * Updates internal mappings when a vehicle is parked
     */
    public void addVehicleMapping(String vehicleId, List<String> spaceIds) {
        vehicleToSpaces.put(vehicleId, new ArrayList<>(spaceIds));
        for (String spaceId : spaceIds) {
            spaceToVehicle.put(spaceId, vehicleId);
        }
    }
    
    /**
     * Updates internal mappings when a vehicle is removed
     */
    public void removeVehicleMapping(String vehicleId) {
        List<String> spaceIds = vehicleToSpaces.remove(vehicleId);
        if (spaceIds != null) {
            for (String spaceId : spaceIds) {
                spaceToVehicle.remove(spaceId);
            }
        }
    }
    
    /**
     * Gets spaces occupied by a specific vehicle on this level
     */
    public List<String> getVehicleSpaces(String vehicleId) {
        List<String> spaces = vehicleToSpaces.get(vehicleId);
        return spaces != null ? new ArrayList<>(spaces) : Collections.emptyList();
    }
    
    /**
     * Finds a parking space by its ID on this level
     */
    public ParkingSpace findSpaceById(String spaceId) {
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                if (space.getIdentifier().equals(spaceId)) {
                    return space;
                }
            }
        }
        return null; // Space not found on this level
    }
    
    /**
     * Gets level status information
     */
    public LevelStatus getLevelStatus() {
        int totalSpaces = getTotalSpaces();
        int occupiedSpaces = getOccupiedSpaces();
        int availableSpaces = getAvailableSpaces();
        
        int totalCompactSpaces = 0;
        int occupiedCompactSpaces = 0;
        int totalRegularSpaces = 0;
        int occupiedRegularSpaces = 0;
        
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                if (space.getType() == SpaceType.COMPACT) {
                    totalCompactSpaces++;
                    if (space.isOccupied()) {
                        occupiedCompactSpaces++;
                    }
                } else {
                    totalRegularSpaces++;
                    if (space.isOccupied()) {
                        occupiedRegularSpaces++;
                    }
                }
            }
        }
        
        double occupancyRate = totalSpaces > 0 ? (double) occupiedSpaces / totalSpaces : 0.0;
        
        return new LevelStatus(
            levelNumber, levelName, levelType,
            totalSpaces, availableSpaces, occupiedSpaces,
            totalCompactSpaces, totalCompactSpaces - occupiedCompactSpaces, occupiedCompactSpaces,
            totalRegularSpaces, totalRegularSpaces - occupiedRegularSpaces, occupiedRegularSpaces,
            occupancyRate, isFull(), isEmpty(),
            hasElevatorAccess, hasStairAccess,
            new HashSet<>(allowedVehicleTypes)
        );
    }
    
    @Override
    public String toString() {
        return String.format("Level %d (%s): %d/%d spaces occupied [%s]",
                levelNumber, levelName, getOccupiedSpaces(), getTotalSpaces(), levelType);
    }
}