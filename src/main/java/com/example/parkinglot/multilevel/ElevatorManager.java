package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.VehicleType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages elevator operations and optimization for multi-level parking
 */
public class ElevatorManager {
    private final List<Elevator> elevators;
    private final Map<Integer, Set<Integer>> levelConnections; // level -> connected levels
    private final AtomicInteger requestCounter;
    private final Map<String, ElevatorRequest> activeRequests;
    
    public ElevatorManager() {
        this.elevators = new ArrayList<>();
        this.levelConnections = new ConcurrentHashMap<>();
        this.requestCounter = new AtomicInteger(0);
        this.activeRequests = new ConcurrentHashMap<>();
    }
    
    /**
     * Adds an elevator to the system
     */
    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
        // Connect all levels this elevator serves
        List<Integer> servedLevels = elevator.getServedLevels();
        for (int level : servedLevels) {
            levelConnections.computeIfAbsent(level, k -> new HashSet<>()).addAll(servedLevels);
        }
    }
    
    /**
     * Checks if a path exists between two levels
     */
    public boolean canReachLevel(int fromLevel, int toLevel) {
        if (fromLevel == toLevel) {
            return true;
        }
        
        Set<Integer> reachableLevels = levelConnections.get(fromLevel);
        return reachableLevels != null && reachableLevels.contains(toLevel);
    }
    
    /**
     * Finds the optimal elevator for a request
     */
    public Optional<Elevator> findOptimalElevator(int fromLevel, int toLevel, 
                                                VehicleType vehicleType, boolean isUrgent) {
        return elevators.stream()
                .filter(elevator -> elevator.canServeRequest(fromLevel, toLevel, vehicleType))
                .filter(elevator -> !elevator.isMaintenanceMode())
                .min(Comparator.comparingInt(elevator -> 
                    calculateElevatorScore(elevator, fromLevel, toLevel, vehicleType, isUrgent)));
    }
    
    private int calculateElevatorScore(Elevator elevator, int fromLevel, int toLevel, 
                                     VehicleType vehicleType, boolean isUrgent) {
        int score = 0;
        
        // Distance factor (current position)
        int currentLevel = elevator.getCurrentLevel();
        int distanceToFrom = Math.abs(currentLevel - fromLevel);
        score += distanceToFrom * 2;
        
        // Travel distance
        int travelDistance = Math.abs(toLevel - fromLevel);
        score += travelDistance;
        
        // Load factor
        if (elevator.getCapacityUsage() > 0.8) {
            score += 10; // Penalty for near-full elevators
        }
        
        // Vehicle type preference
        if (vehicleType == VehicleType.VAN && !elevator.isVanCompatible()) {
            score += 20; // High penalty for incompatible vehicles
        }
        
        // Queue length
        score += elevator.getQueueLength() * 3;
        
        // Urgency bonus
        if (isUrgent && elevator.getQueueLength() == 0) {
            score -= 5; // Prefer empty elevators for urgent requests
        }
        
        return score;
    }
    
    /**
     * Requests elevator service
     */
    public ElevatorRequest requestElevator(int fromLevel, int toLevel, VehicleType vehicleType, 
                                         String vehicleId, boolean isUrgent) {
        String requestId = "REQ-" + requestCounter.incrementAndGet();
        
        Optional<Elevator> optimalElevator = findOptimalElevator(fromLevel, toLevel, vehicleType, isUrgent);
        
        if (optimalElevator.isPresent()) {
            Elevator elevator = optimalElevator.get();
            ElevatorRequest request = new ElevatorRequest(
                requestId, fromLevel, toLevel, vehicleType, vehicleId, 
                elevator.getElevatorId(), System.currentTimeMillis(), isUrgent
            );
            
            elevator.addRequest(request);
            activeRequests.put(requestId, request);
            return request;
        } else {
            // No available elevator - create a waiting request
            ElevatorRequest request = new ElevatorRequest(
                requestId, fromLevel, toLevel, vehicleType, vehicleId, 
                null, System.currentTimeMillis(), isUrgent
            );
            activeRequests.put(requestId, request);
            return request;
        }
    }
    
    /**
     * Cancels an elevator request
     */
    public boolean cancelRequest(String requestId) {
        ElevatorRequest request = activeRequests.remove(requestId);
        if (request != null && request.getAssignedElevatorId() != null) {
            findElevatorById(request.getAssignedElevatorId())
                .ifPresent(elevator -> elevator.removeRequest(requestId));
            return true;
        }
        return false;
    }
    
    /**
     * Gets the status of a request
     */
    public Optional<ElevatorRequest> getRequestStatus(String requestId) {
        return Optional.ofNullable(activeRequests.get(requestId));
    }
    
    /**
     * Gets current elevator statuses
     */
    public List<ElevatorStatus> getElevatorStatuses() {
        return elevators.stream()
                .map(Elevator::getStatus)
                .sorted(Comparator.comparing(ElevatorStatus::getElevatorId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Finds an elevator by ID
     */
    public Optional<Elevator> findElevatorById(String elevatorId) {
        return elevators.stream()
                .filter(elevator -> elevator.getElevatorId().equals(elevatorId))
                .findFirst();
    }
    
    /**
     * Gets system-wide elevator statistics
     */
    public ElevatorSystemStats getSystemStats() {
        int totalRequests = activeRequests.size();
        int totalElevators = elevators.size();
        int activeElevators = (int) elevators.stream()
                .filter(elevator -> !elevator.isMaintenanceMode())
                .count();
        
        double averageWaitTime = activeRequests.values().stream()
                .filter(request -> request.getAssignedElevatorId() != null)
                .mapToLong(request -> System.currentTimeMillis() - request.getRequestTime())
                .average()
                .orElse(0.0);
        
        int urgentRequests = (int) activeRequests.values().stream()
                .filter(ElevatorRequest::isUrgent)
                .count();
        
        Map<VehicleType, Integer> requestsByVehicleType = new EnumMap<>(VehicleType.class);
        for (ElevatorRequest request : activeRequests.values()) {
            requestsByVehicleType.merge(request.getVehicleType(), 1, Integer::sum);
        }
        
        return new ElevatorSystemStats(
            totalRequests, totalElevators, activeElevators,
            averageWaitTime, urgentRequests, requestsByVehicleType
        );
    }
    
    /**
     * Processes elevator movements and updates (should be called periodically)
     */
    public void processElevatorOperations() {
        for (Elevator elevator : elevators) {
            if (!elevator.isMaintenanceMode()) {
                elevator.processNextRequest();
            }
        }
        
        // Reassign waiting requests to available elevators
        reassignWaitingRequests();
    }
    
    private void reassignWaitingRequests() {
        List<ElevatorRequest> waitingRequests = activeRequests.values().stream()
                .filter(request -> request.getAssignedElevatorId() == null)
                .sorted(Comparator.comparing(ElevatorRequest::isUrgent).reversed()
                        .thenComparing(ElevatorRequest::getRequestTime))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        for (ElevatorRequest request : waitingRequests) {
            Optional<Elevator> availableElevator = findOptimalElevator(
                request.getFromLevel(), request.getToLevel(),
                request.getVehicleType(), request.isUrgent()
            );
            
            if (availableElevator.isPresent()) {
                Elevator elevator = availableElevator.get();
                request.assignElevator(elevator.getElevatorId());
                elevator.addRequest(request);
            }
        }
    }
    
    /**
     * Sets an elevator to maintenance mode
     */
    public boolean setElevatorMaintenanceMode(String elevatorId, boolean maintenanceMode) {
        return findElevatorById(elevatorId)
                .map(elevator -> {
                    elevator.setMaintenanceMode(maintenanceMode);
                    if (maintenanceMode) {
                        // Reassign requests from this elevator
                        List<ElevatorRequest> requests = elevator.clearAllRequests();
                        for (ElevatorRequest request : requests) {
                            request.assignElevator(null);
                        }
                    }
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Gets connected levels for a given level
     */
    public Set<Integer> getConnectedLevels(int level) {
        return new HashSet<>(levelConnections.getOrDefault(level, Collections.emptySet()));
    }
    
    /**
     * Checks if the elevator system is operational
     */
    public boolean isSystemOperational() {
        return elevators.stream().anyMatch(elevator -> !elevator.isMaintenanceMode());
    }
}