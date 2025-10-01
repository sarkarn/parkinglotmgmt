package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.VehicleType;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents an elevator in the multi-level parking system
 */
public class Elevator {
    private final String elevatorId;
    private final List<Integer> servedLevels;
    private final int maxCapacity; // max number of vehicles
    private final boolean vanCompatible; // can accommodate vans
    private final Queue<ElevatorRequest> requestQueue;
    private final Set<String> currentOccupants; // vehicle IDs currently in elevator
    
    private int currentLevel;
    private ElevatorState state;
    private boolean maintenanceMode;
    private long lastOperationTime;
    private ElevatorRequest currentRequest;
    
    public Elevator(String elevatorId, List<Integer> servedLevels, int maxCapacity, 
                   boolean vanCompatible, int initialLevel) {
        this.elevatorId = elevatorId;
        this.servedLevels = new ArrayList<>(servedLevels);
        this.maxCapacity = maxCapacity;
        this.vanCompatible = vanCompatible;
        this.currentLevel = initialLevel;
        this.state = ElevatorState.IDLE;
        this.maintenanceMode = false;
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.currentOccupants = new HashSet<>();
        this.lastOperationTime = System.currentTimeMillis();
    }
    
    /**
     * Checks if this elevator can serve a request
     */
    public boolean canServeRequest(int fromLevel, int toLevel, VehicleType vehicleType) {
        if (maintenanceMode) {
            return false;
        }
        
        // Check if elevator serves both levels
        if (!servedLevels.contains(fromLevel) || !servedLevels.contains(toLevel)) {
            return false;
        }
        
        // Check vehicle compatibility
        if (vehicleType == VehicleType.VAN && !vanCompatible) {
            return false;
        }
        
        // Check capacity
        if (currentOccupants.size() >= maxCapacity) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Adds a request to the elevator queue
     */
    public void addRequest(ElevatorRequest request) {
        if (canServeRequest(request.getFromLevel(), request.getToLevel(), request.getVehicleType())) {
            requestQueue.offer(request);
            request.updateStatus(ElevatorRequestStatus.ASSIGNED);
            
            if (state == ElevatorState.IDLE) {
                state = ElevatorState.MOVING;
            }
        }
    }
    
    /**
     * Removes a request from the queue
     */
    public boolean removeRequest(String requestId) {
        return requestQueue.removeIf(request -> request.getRequestId().equals(requestId));
    }
    
    /**
     * Clears all requests (used during maintenance)
     */
    public List<ElevatorRequest> clearAllRequests() {
        List<ElevatorRequest> requests = new ArrayList<>(requestQueue);
        requestQueue.clear();
        currentRequest = null;
        state = ElevatorState.IDLE;
        return requests;
    }
    
    /**
     * Processes the next request in the queue
     */
    public void processNextRequest() {
        if (maintenanceMode || state == ElevatorState.MAINTENANCE) {
            return;
        }
        
        if (currentRequest == null && !requestQueue.isEmpty()) {
            currentRequest = requestQueue.poll();
            if (currentRequest != null) {
                currentRequest.updateStatus(ElevatorRequestStatus.IN_TRANSIT);
                state = ElevatorState.MOVING;
            }
        }
        
        if (currentRequest != null) {
            processCurrentRequest();
        } else if (state != ElevatorState.IDLE) {
            state = ElevatorState.IDLE;
        }
        
        lastOperationTime = System.currentTimeMillis();
    }
    
    private void processCurrentRequest() {
        // Simulate elevator movement - in real implementation, this would involve
        // actual hardware control and timing
        
        if (currentLevel != currentRequest.getFromLevel()) {
            // Move to pickup level
            moveToLevel(currentRequest.getFromLevel());
        } else if (!currentOccupants.contains(currentRequest.getVehicleId())) {
            // Pick up vehicle
            currentOccupants.add(currentRequest.getVehicleId());
            state = ElevatorState.LOADING;
        } else if (currentLevel != currentRequest.getToLevel()) {
            // Move to destination level
            moveToLevel(currentRequest.getToLevel());
        } else {
            // Unload vehicle
            currentOccupants.remove(currentRequest.getVehicleId());
            currentRequest.updateStatus(ElevatorRequestStatus.COMPLETED);
            currentRequest = null;
            state = requestQueue.isEmpty() ? ElevatorState.IDLE : ElevatorState.MOVING;
        }
    }
    
    private void moveToLevel(int targetLevel) {
        if (currentLevel < targetLevel) {
            currentLevel++;
        } else if (currentLevel > targetLevel) {
            currentLevel--;
        }
        
        state = ElevatorState.MOVING;
    }
    
    /**
     * Gets current capacity usage as a percentage
     */
    public double getCapacityUsage() {
        return (double) currentOccupants.size() / maxCapacity;
    }
    
    /**
     * Gets current queue length
     */
    public int getQueueLength() {
        return requestQueue.size() + (currentRequest != null ? 1 : 0);
    }
    
    /**
     * Gets elevator status
     */
    public ElevatorStatus getStatus() {
        return new ElevatorStatus(
            elevatorId, currentLevel, state, maintenanceMode,
            currentOccupants.size(), maxCapacity, getQueueLength(),
            servedLevels, vanCompatible, lastOperationTime,
            currentRequest != null ? currentRequest.getRequestId() : null
        );
    }
    
    // Getters and Setters
    public String getElevatorId() { return elevatorId; }
    public List<Integer> getServedLevels() { return new ArrayList<>(servedLevels); }
    public int getCurrentLevel() { return currentLevel; }
    public ElevatorState getState() { return state; }
    public boolean isMaintenanceMode() { return maintenanceMode; }
    public boolean isVanCompatible() { return vanCompatible; }
    public int getMaxCapacity() { return maxCapacity; }
    public Set<String> getCurrentOccupants() { return new HashSet<>(currentOccupants); }
    
    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
        if (maintenanceMode) {
            state = ElevatorState.MAINTENANCE;
        } else if (state == ElevatorState.MAINTENANCE) {
            state = ElevatorState.IDLE;
        }
    }
    
    public void setCurrentLevel(int level) {
        if (servedLevels.contains(level)) {
            this.currentLevel = level;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Elevator[%s]: Level %d, %s, %d/%d occupants, %d queued",
                elevatorId, currentLevel, state, currentOccupants.size(), maxCapacity, getQueueLength());
    }
}