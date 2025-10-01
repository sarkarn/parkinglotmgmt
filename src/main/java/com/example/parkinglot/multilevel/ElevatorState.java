package com.example.parkinglot.multilevel;

/**
 * Enumeration representing different states of an elevator
 */
public enum ElevatorState {
    /**
     * Elevator is idle and available for requests
     */
    IDLE("Idle"),
    
    /**
     * Elevator is moving between levels
     */
    MOVING("Moving"),
    
    /**
     * Elevator is loading or unloading vehicles
     */
    LOADING("Loading"),
    
    /**
     * Elevator is in maintenance mode
     */
    MAINTENANCE("Maintenance"),
    
    /**
     * Elevator is out of service due to failure
     */
    OUT_OF_SERVICE("Out of Service");
    
    private final String displayName;
    
    ElevatorState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}