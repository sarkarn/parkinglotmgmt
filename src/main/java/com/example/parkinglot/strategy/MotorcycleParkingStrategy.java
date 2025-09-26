package com.example.parkinglot.strategy;

import com.example.parkinglot.model.*;
import java.util.List;

/**
 * Parking strategy for motorcycles.
 * Motorcycles can park in any available space (compact or regular).
 */
public class MotorcycleParkingStrategy implements ParkingStrategy {
    
    @Override
    public ParkingResult allocateSpaces(String vehicleId, List<List<ParkingSpace>> rows) {
        // Motorcycles can park in any available space (compact or regular)
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                if (!space.isOccupied()) {
                    return ParkingResult.success(space.getIdentifier());
                }
            }
        }
        return ParkingResult.failure("No available space for motorcycle");
    }
}