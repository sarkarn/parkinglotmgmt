package com.example.parkinglot;

import com.example.parkinglot.model.*;
import com.example.parkinglot.strategy.*;
import java.util.*;

/**
 * Main class implementing the parking lot management system.
 * Supports parking motorcycles, cars, and vans with different space requirements.
 */
public class ParkingLot {
    private final List<List<ParkingSpace>> rows;
    private final Map<String, List<String>> vehicleToSpaces; // Vehicle ID -> List of space IDs
    private final Map<String, String> spaceToVehicle; // Space ID -> Vehicle ID
    
    /**
     * Initializes the parking lot with the given configuration.
     * @param rowConfigurations List of space type arrays, one for each row
     */
    public ParkingLot(List<SpaceType[]> rowConfigurations) {
        if (rowConfigurations == null || rowConfigurations.isEmpty()) {
            throw new IllegalArgumentException("Row configurations cannot be null or empty");
        }
        
        this.rows = new ArrayList<>();
        this.vehicleToSpaces = new HashMap<>();
        this.spaceToVehicle = new HashMap<>();
        
        initializeRows(rowConfigurations);
    }
    
    private void initializeRows(List<SpaceType[]> rowConfigurations) {
        for (int rowIndex = 0; rowIndex < rowConfigurations.size(); rowIndex++) {
            SpaceType[] rowConfig = rowConfigurations.get(rowIndex);
            if (rowConfig == null || rowConfig.length == 0) {
                throw new IllegalArgumentException("Row configuration cannot be null or empty");
            }
            
            List<ParkingSpace> row = new ArrayList<>();
            for (int spaceIndex = 0; spaceIndex < rowConfig.length; spaceIndex++) {
                String spaceId = String.format("R%d-%d", rowIndex + 1, spaceIndex + 1);
                row.add(new ParkingSpace(spaceId, rowConfig[spaceIndex]));
            }
            rows.add(row);
        }
    }
    
    /**
     * Attempts to park a vehicle in the parking lot using the Strategy Pattern.
     * @param vehicleId Unique identifier for the vehicle
     * @param vehicleType Type of vehicle (MOTORCYCLE, CAR, VAN)
     * @return ParkingResult indicating success/failure and allocated spaces
     */
    public ParkingResult parkVehicle(String vehicleId, VehicleType vehicleType) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return ParkingResult.failure("Vehicle ID cannot be null or empty");
        }
        if (vehicleType == null) {
            return ParkingResult.failure("Vehicle type cannot be null");
        }
        
        vehicleId = vehicleId.trim();
        
        // Check if vehicle is already parked
        if (vehicleToSpaces.containsKey(vehicleId)) {
            return ParkingResult.alreadyParked(vehicleToSpaces.get(vehicleId));
        }
        
        // Use Strategy Pattern to get the appropriate allocation strategy
        try {
            ParkingStrategy strategy = ParkingStrategyFactory.getStrategy(vehicleType);
            ParkingResult result = strategy.allocateSpaces(vehicleId, rows);
            
            // If allocation was successful, occupy the spaces
            if (result.isSuccess()) {
                for (String spaceId : result.getAllocatedSpaces()) {
                    ParkingSpace space = findSpaceById(spaceId);
                    occupySpace(space, vehicleId);
                }
            }
            
            return result;
        } catch (IllegalArgumentException e) {
            return ParkingResult.failure("Unsupported vehicle type: " + vehicleType);
        }
    }
    
    private void occupySpace(ParkingSpace space, String vehicleId) {
        space.occupy(vehicleId);
        spaceToVehicle.put(space.getIdentifier(), vehicleId);
        vehicleToSpaces.computeIfAbsent(vehicleId, k -> new ArrayList<>()).add(space.getIdentifier());
    }
    
    /**
     * Removes a vehicle from the parking lot.
     * @param vehicleId Unique identifier of the vehicle to remove
     * @return true if vehicle was found and removed, false otherwise
     */
    public boolean removeVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return false;
        }
        
        vehicleId = vehicleId.trim();
        List<String> spaceIds = vehicleToSpaces.remove(vehicleId);
        
        if (spaceIds == null) {
            return false; // Vehicle not found
        }
        
        // Free all spaces occupied by this vehicle
        for (String spaceId : spaceIds) {
            spaceToVehicle.remove(spaceId);
            findSpaceById(spaceId).vacate();
        }
        
        return true;
    }
    
    private ParkingSpace findSpaceById(String spaceId) {
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                if (space.getIdentifier().equals(spaceId)) {
                    return space;
                }
            }
        }
        throw new IllegalStateException("Space not found: " + spaceId);
    }
    
    /**
     * Gets the current status of the parking lot.
     * @return LotStatus object containing detailed statistics
     */
    public LotStatus getLotStatus() {
        int totalSpaces = 0;
        int totalCompactSpaces = 0;
        int totalRegularSpaces = 0;
        int availableSpaces = 0;
        int availableCompactSpaces = 0;
        int availableRegularSpaces = 0;
        int occupiedSpaces = 0;
        int occupiedCompactSpaces = 0;
        int occupiedRegularSpaces = 0;
        
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                totalSpaces++;
                
                if (space.getType() == SpaceType.COMPACT) {
                    totalCompactSpaces++;
                    if (!space.isOccupied()) {
                        availableCompactSpaces++;
                    } else {
                        occupiedCompactSpaces++;
                    }
                } else {
                    totalRegularSpaces++;
                    if (!space.isOccupied()) {
                        availableRegularSpaces++;
                    } else {
                        occupiedRegularSpaces++;
                    }
                }
                
                if (!space.isOccupied()) {
                    availableSpaces++;
                } else {
                    occupiedSpaces++;
                }
            }
        }
        
        int vanOccupiedSpaces = countVanOccupiedSpaces();
        boolean isFull = availableSpaces == 0;
        boolean isEmpty = occupiedSpaces == 0;
        boolean allCompactOccupied = totalCompactSpaces > 0 && availableCompactSpaces == 0;
        boolean allRegularOccupied = totalRegularSpaces > 0 && availableRegularSpaces == 0;
        
        return new LotStatus(totalSpaces, totalCompactSpaces, totalRegularSpaces,
                           availableSpaces, availableCompactSpaces, availableRegularSpaces,
                           occupiedSpaces, occupiedCompactSpaces, occupiedRegularSpaces,
                           vanOccupiedSpaces, isFull, isEmpty, allCompactOccupied, allRegularOccupied);
    }
    
    private int countVanOccupiedSpaces() {
        int count = 0;
        for (List<String> spaceIds : vehicleToSpaces.values()) {
            if (spaceIds.size() == 2) { // Vans occupy exactly 2 spaces
                count += 2;
            }
        }
        return count;
    }
    
    /**
     * Gets the spaces currently occupied by a specific vehicle.
     * @param vehicleId The vehicle identifier
     * @return List of space identifiers, or empty list if vehicle not found
     */
    public List<String> getVehicleSpaces(String vehicleId) {
        if (vehicleId == null) {
            return Collections.emptyList();
        }
        List<String> spaces = vehicleToSpaces.get(vehicleId.trim());
        return spaces != null ? new ArrayList<>(spaces) : Collections.emptyList();
    }
    
    /**
     * Gets a summary of occupied and available spots per row.
     * @return List of row summaries
     */
    public List<String> getRowSummaries() {
        List<String> summaries = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            List<ParkingSpace> row = rows.get(i);
            long occupied = row.stream().mapToLong(space -> space.isOccupied() ? 1 : 0).sum();
            long available = row.size() - occupied;
            summaries.add(String.format("Row %d: %d occupied, %d available", 
                                      i + 1, occupied, available));
        }
        return summaries;
    }
}