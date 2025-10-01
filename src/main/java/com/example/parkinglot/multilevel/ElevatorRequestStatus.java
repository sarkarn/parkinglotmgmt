package com.example.parkinglot.multilevel;

/**
 * Status enumeration for elevator requests
 */
public enum ElevatorRequestStatus {
    /**
     * Request is waiting to be assigned to an elevator
     */
    WAITING("Waiting"),
    
    /**
     * Request has been assigned to an elevator
     */
    ASSIGNED("Assigned"),
    
    /**
     * Elevator is in transit to pick up or moving the vehicle
     */
    IN_TRANSIT("In Transit"),
    
    /**
     * Request has been completed successfully
     */
    COMPLETED("Completed"),
    
    /**
     * Request was cancelled
     */
    CANCELLED("Cancelled"),
    
    /**
     * Request failed due to technical issues
     */
    FAILED("Failed");
    
    private final String displayName;
    
    ElevatorRequestStatus(String displayName) {
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