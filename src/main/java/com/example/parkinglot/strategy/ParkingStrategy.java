package com.example.parkinglot.strategy;

import com.example.parkinglot.model.ParkingSpace;
import com.example.parkinglot.model.ParkingResult;
import java.util.List;

/**
 * Strategy interface for different vehicle parking allocation strategies.
 */
public interface ParkingStrategy {
    /**
     * Attempts to find and allocate suitable parking space(s) for a vehicle.
     * 
     * @param vehicleId The unique identifier of the vehicle to park
     * @param rows The parking lot rows containing spaces
     * @return ParkingResult indicating success/failure and allocated spaces
     */
    ParkingResult allocateSpaces(String vehicleId, List<List<ParkingSpace>> rows);
}