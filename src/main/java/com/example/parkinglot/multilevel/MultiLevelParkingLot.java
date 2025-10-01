package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.*;
import com.example.parkinglot.strategy.*;
import com.example.parkinglot.reservation.ReservationManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Multi-level parking lot system with elevator management
 */
public class MultiLevelParkingLot {
    private final String lotId;
    private final Map<Integer, ParkingLevel> levels;
    private final ElevatorManager elevatorManager;
    private final ReservationManager reservationManager;
    private final MultiLevelAllocationStrategy allocationStrategy;
    
    public MultiLevelParkingLot(String lotId) {
        this.lotId = lotId;
        this.levels = new TreeMap<>(); // Sorted by level number
        this.elevatorManager = new ElevatorManager();
        // Note: ReservationManager will be initialized when needed with a ParkingLot reference
        this.reservationManager = null; // Placeholder for now
        this.allocationStrategy = new MultiLevelAllocationStrategy();
    }
    
    /**
     * Adds a level to the parking lot
     */
    public void addLevel(ParkingLevel level) {
        levels.put(level.getLevelNumber(), level);
    }
    
    /**
     * Adds an elevator to the system
     */
    public void addElevator(Elevator elevator) {
        elevatorManager.addElevator(elevator);
    }
    
    /**
     * Parks a vehicle using multi-level allocation
     */
    public MultiLevelParkingResult parkVehicle(Vehicle vehicle) {
        // Find optimal level for the vehicle
        OptimalLevelResult levelResult = allocationStrategy.findOptimalLevel(
            levels.values(), vehicle.getType()
        );
        
        if (!levelResult.isSuccessful()) {
            return MultiLevelParkingResult.failure("No suitable level found for " + vehicle.getType(), 
                                                  vehicle.getId());
        }
        
        ParkingLevel selectedLevel = levelResult.getSelectedLevel();
        
        // Use appropriate strategy to find spaces on the selected level
        ParkingStrategy strategy = ParkingStrategyFactory.getStrategy(vehicle.getType());
        ParkingResult result = strategy.allocateSpaces(vehicle.getId(), selectedLevel.getRows());
        
        if (result.isSuccess()) {
            // Update level mappings
            selectedLevel.addVehicleMapping(vehicle.getId(), result.getAllocatedSpaces());
            
            // Request elevator if needed (not ground level)
            if (selectedLevel.getLevelNumber() != 0 && 
                selectedLevel.hasAppropriateAccess(vehicle.getType())) {
                ElevatorRequest elevatorRequest = requestElevatorForParking(vehicle, 0, selectedLevel.getLevelNumber());
                return MultiLevelParkingResult.successWithElevator(
                    result.getAllocatedSpaces(), vehicle.getId(),
                    selectedLevel.getLevelNumber(), selectedLevel.getLevelName(),
                    elevatorRequest.getRequestId()
                );
            }
            
            return MultiLevelParkingResult.success(
                result.getAllocatedSpaces(), vehicle.getId(),
                selectedLevel.getLevelNumber(), selectedLevel.getLevelName()
            );
        }
        
        return MultiLevelParkingResult.failure(result.getMessage(), vehicle.getId());
    }
    
    /**
     * Removes a vehicle from the parking lot
     */
    public MultiLevelParkingResult removeVehicle(String vehicleId) {
        // Find which level contains the vehicle
        for (ParkingLevel level : levels.values()) {
            List<String> vehicleSpaces = level.getVehicleSpaces(vehicleId);
            if (!vehicleSpaces.isEmpty()) {
                return removeVehicleFromLevel(vehicleId, level, vehicleSpaces);
            }
        }
        
        return MultiLevelParkingResult.failure("Vehicle not found: " + vehicleId, vehicleId);
    }
    
    private MultiLevelParkingResult removeVehicleFromLevel(String vehicleId, ParkingLevel level, 
                                               List<String> spaceIds) {
        boolean success = true;
        List<String> freedSpaces = new ArrayList<>();
        
        for (String spaceId : spaceIds) {
            ParkingSpace space = level.findSpaceById(spaceId);
            if (space != null && space.isOccupied()) {
                space.vacate();
                freedSpaces.add(spaceId);
            } else {
                success = false;
            }
        }
        
        if (success) {
            level.removeVehicleMapping(vehicleId);
            
            // Request elevator if needed (not ground level)
            if (level.getLevelNumber() != 0) {
                // Determine vehicle type from space type (simplified)
                VehicleType vehicleType = inferVehicleTypeFromSpaces(level, spaceIds);
                ElevatorRequest elevatorRequest = requestElevatorForRetrieval(vehicleId, level.getLevelNumber(), 0, vehicleType);
                return new MultiLevelParkingResult(true, 
                    String.format("Vehicle removed from %s, freed spaces: %s", 
                        level.getLevelName(), String.join(", ", freedSpaces)),
                    freedSpaces, vehicleId, level.getLevelNumber(), level.getLevelName(),
                    elevatorRequest.getRequestId());
            }
            
            return new MultiLevelParkingResult(true, 
                String.format("Vehicle removed from %s, freed spaces: %s", 
                    level.getLevelName(), String.join(", ", freedSpaces)),
                freedSpaces, vehicleId, level.getLevelNumber(), level.getLevelName(), null);
        } else {
            return MultiLevelParkingResult.failure("Error removing vehicle from some spaces", vehicleId);
        }
    }
    
    private VehicleType inferVehicleTypeFromSpaces(ParkingLevel level, List<String> spaceIds) {
        // Simple heuristic based on number of spaces used
        if (spaceIds.size() > 1) {
            return VehicleType.VAN; // Vans typically use multiple spaces
        }
        
        // Check space type
        ParkingSpace space = level.findSpaceById(spaceIds.get(0));
        if (space != null && space.getType() == SpaceType.COMPACT) {
            return VehicleType.MOTORCYCLE;
        }
        
        return VehicleType.CAR; // Default assumption
    }
    
    /**
     * Requests elevator for parking (going up/down from ground level)
     */
    public ElevatorRequest requestElevatorForParking(Vehicle vehicle, int fromLevel, int toLevel) {
        return elevatorManager.requestElevator(fromLevel, toLevel, vehicle.getType(), 
                                             vehicle.getId(), false);
    }
    
    /**
     * Requests elevator for vehicle retrieval
     */
    public ElevatorRequest requestElevatorForRetrieval(String vehicleId, int fromLevel, 
                                                     int toLevel, VehicleType vehicleType) {
        return elevatorManager.requestElevator(fromLevel, toLevel, vehicleType, 
                                             vehicleId, false);
    }
    
    /**
     * Requests urgent elevator service
     */
    public ElevatorRequest requestUrgentElevator(String vehicleId, int fromLevel, 
                                               int toLevel, VehicleType vehicleType) {
        return elevatorManager.requestElevator(fromLevel, toLevel, vehicleType, 
                                             vehicleId, true);
    }
    
    /**
     * Gets comprehensive status of the multi-level parking lot
     */
    public MultiLevelLotStatus getLotStatus() {
        List<LevelStatus> levelStatuses = levels.values().stream()
                .map(ParkingLevel::getLevelStatus)
                .collect(Collectors.toList());
        
        List<ElevatorStatus> elevatorStatuses = elevatorManager.getElevatorStatuses();
        ElevatorSystemStats elevatorStats = elevatorManager.getSystemStats();
        
        int totalSpaces = levelStatuses.stream().mapToInt(LevelStatus::getTotalSpaces).sum();
        int occupiedSpaces = levelStatuses.stream().mapToInt(LevelStatus::getOccupiedSpaces).sum();
        int availableSpaces = totalSpaces - occupiedSpaces;
        double overallOccupancyRate = totalSpaces > 0 ? (double) occupiedSpaces / totalSpaces : 0.0;
        
        return new MultiLevelLotStatus(
            lotId, levelStatuses, elevatorStatuses, elevatorStats,
            totalSpaces, availableSpaces, occupiedSpaces, overallOccupancyRate,
            levels.size(), elevatorManager.isSystemOperational()
        );
    }
    
    /**
     * Gets status for a specific level
     */
    public Optional<LevelStatus> getLevelStatus(int levelNumber) {
        ParkingLevel level = levels.get(levelNumber);
        return level != null ? Optional.of(level.getLevelStatus()) : Optional.empty();
    }
    
    /**
     * Gets available spaces for a vehicle type across all levels
     */
    public int getAvailableSpacesForVehicleType(VehicleType vehicleType) {
        return levels.values().stream()
                .mapToInt(level -> level.getAvailableSpacesForVehicleType(vehicleType))
                .sum();
    }
    
    /**
     * Finds a vehicle's location
     */
    public Optional<VehicleLocation> findVehicleLocation(String vehicleId) {
        for (ParkingLevel level : levels.values()) {
            List<String> vehicleSpaces = level.getVehicleSpaces(vehicleId);
            if (!vehicleSpaces.isEmpty()) {
                return Optional.of(new VehicleLocation(
                    vehicleId, level.getLevelNumber(), level.getLevelName(),
                    vehicleSpaces, level.getLevelType()
                ));
            }
        }
        return Optional.empty();
    }
    
    /**
     * Gets all levels
     */
    public List<ParkingLevel> getAllLevels() {
        return new ArrayList<>(levels.values());
    }
    
    /**
     * Gets levels that can accommodate a specific vehicle type
     */
    public List<ParkingLevel> getLevelsForVehicleType(VehicleType vehicleType) {
        return levels.values().stream()
                .filter(level -> level.canAccommodateVehicleType(vehicleType))
                .filter(level -> level.hasAppropriateAccess(vehicleType))
                .collect(Collectors.toList());
    }
    
    /**
     * Processes elevator operations (should be called periodically)
     */
    public void processElevatorOperations() {
        elevatorManager.processElevatorOperations();
    }
    
    /**
     * Sets elevator maintenance mode
     */
    public boolean setElevatorMaintenanceMode(String elevatorId, boolean maintenanceMode) {
        return elevatorManager.setElevatorMaintenanceMode(elevatorId, maintenanceMode);
    }
    
    /**
     * Gets elevator request status
     */
    public Optional<ElevatorRequest> getElevatorRequestStatus(String requestId) {
        return elevatorManager.getRequestStatus(requestId);
    }
    
    /**
     * Cancels an elevator request
     */
    public boolean cancelElevatorRequest(String requestId) {
        return elevatorManager.cancelRequest(requestId);
    }
    
    // Integration with reservation system
    public ReservationManager getReservationManager() {
        return reservationManager;
    }
    
    // Getters
    public String getLotId() { return lotId; }
    public ElevatorManager getElevatorManager() { return elevatorManager; }
    public int getLevelCount() { return levels.size(); }
    
    @Override
    public String toString() {
        return String.format("MultiLevelParkingLot[%s]: %d levels, %d total spaces, %.1f%% occupied",
                lotId, levels.size(), 
                levels.values().stream().mapToInt(ParkingLevel::getTotalSpaces).sum(),
                getLotStatus().getOverallOccupancyRate() * 100);
    }
}