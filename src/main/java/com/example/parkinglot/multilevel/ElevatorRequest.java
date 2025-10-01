package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.VehicleType;

/**
 * Represents an elevator request for vehicle movement between levels
 */
public class ElevatorRequest {
    private final String requestId;
    private final int fromLevel;
    private final int toLevel;
    private final VehicleType vehicleType;
    private final String vehicleId;
    private final long requestTime;
    private final boolean isUrgent;
    private String assignedElevatorId;
    private ElevatorRequestStatus status;
    private long completionTime;
    
    public ElevatorRequest(String requestId, int fromLevel, int toLevel, 
                          VehicleType vehicleType, String vehicleId,
                          String assignedElevatorId, long requestTime, boolean isUrgent) {
        this.requestId = requestId;
        this.fromLevel = fromLevel;
        this.toLevel = toLevel;
        this.vehicleType = vehicleType;
        this.vehicleId = vehicleId;
        this.assignedElevatorId = assignedElevatorId;
        this.requestTime = requestTime;
        this.isUrgent = isUrgent;
        this.status = assignedElevatorId != null ? ElevatorRequestStatus.ASSIGNED : ElevatorRequestStatus.WAITING;
        this.completionTime = 0;
    }
    
    public void assignElevator(String elevatorId) {
        this.assignedElevatorId = elevatorId;
        this.status = elevatorId != null ? ElevatorRequestStatus.ASSIGNED : ElevatorRequestStatus.WAITING;
    }
    
    public void updateStatus(ElevatorRequestStatus newStatus) {
        this.status = newStatus;
        if (newStatus == ElevatorRequestStatus.COMPLETED || newStatus == ElevatorRequestStatus.CANCELLED) {
            this.completionTime = System.currentTimeMillis();
        }
    }
    
    public long getWaitTime() {
        long endTime = completionTime > 0 ? completionTime : System.currentTimeMillis();
        return endTime - requestTime;
    }
    
    // Getters
    public String getRequestId() { return requestId; }
    public int getFromLevel() { return fromLevel; }
    public int getToLevel() { return toLevel; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getVehicleId() { return vehicleId; }
    public String getAssignedElevatorId() { return assignedElevatorId; }
    public long getRequestTime() { return requestTime; }
    public boolean isUrgent() { return isUrgent; }
    public ElevatorRequestStatus getStatus() { return status; }
    public long getCompletionTime() { return completionTime; }
    
    @Override
    public String toString() {
        return String.format("Request[%s]: %s from L%d to L%d via %s (%s) - %s",
                requestId, vehicleType, fromLevel, toLevel,
                assignedElevatorId != null ? assignedElevatorId : "UNASSIGNED",
                isUrgent ? "URGENT" : "NORMAL", status);
    }
}