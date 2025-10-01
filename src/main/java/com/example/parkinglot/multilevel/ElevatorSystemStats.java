package com.example.parkinglot.multilevel;

import com.example.parkinglot.model.VehicleType;
import java.util.Map;

/**
 * System-wide statistics for the elevator system
 */
public class ElevatorSystemStats {
    private final int totalRequests;
    private final int totalElevators;
    private final int activeElevators;
    private final double averageWaitTime;
    private final int urgentRequests;
    private final Map<VehicleType, Integer> requestsByVehicleType;
    
    public ElevatorSystemStats(int totalRequests, int totalElevators, int activeElevators,
                              double averageWaitTime, int urgentRequests,
                              Map<VehicleType, Integer> requestsByVehicleType) {
        this.totalRequests = totalRequests;
        this.totalElevators = totalElevators;
        this.activeElevators = activeElevators;
        this.averageWaitTime = averageWaitTime;
        this.urgentRequests = urgentRequests;
        this.requestsByVehicleType = requestsByVehicleType;
    }
    
    public double getSystemEfficiency() {
        return totalElevators > 0 ? (double) activeElevators / totalElevators : 0.0;
    }
    
    public double getAverageWaitTimeMinutes() {
        return averageWaitTime / 60000.0; // Convert milliseconds to minutes
    }
    
    // Getters
    public int getTotalRequests() { return totalRequests; }
    public int getTotalElevators() { return totalElevators; }
    public int getActiveElevators() { return activeElevators; }
    public double getAverageWaitTime() { return averageWaitTime; }
    public int getUrgentRequests() { return urgentRequests; }
    public Map<VehicleType, Integer> getRequestsByVehicleType() { return requestsByVehicleType; }
    
    @Override
    public String toString() {
        return String.format(
            "Elevator System Stats: %d requests, %d/%d elevators active (%.1f%%), " +
            "Avg wait: %.1f min, Urgent: %d, By type: %s",
            totalRequests, activeElevators, totalElevators, getSystemEfficiency() * 100,
            getAverageWaitTimeMinutes(), urgentRequests, requestsByVehicleType
        );
    }
}