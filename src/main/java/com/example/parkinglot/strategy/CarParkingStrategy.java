package com.example.parkinglot.strategy;

import com.example.parkinglot.model.*;
import java.util.List;

/**
 * Parking strategy for cars.
 * Cars can only park in regular spaces.
 */
public class CarParkingStrategy implements ParkingStrategy {
    
    @Override
    public ParkingResult allocateSpaces(String vehicleId, List<List<ParkingSpace>> rows) {
        // Cars can park in both regular and compact spaces
        for (List<ParkingSpace> row : rows) {
            for (ParkingSpace space : row) {
                if (!space.isOccupied() && 
                    (space.getType() == SpaceType.REGULAR || space.getType() == SpaceType.COMPACT)) {
                    space.occupy(vehicleId);
                    return ParkingResult.success(space.getIdentifier());
                }
            }
        }
        return ParkingResult.failure("No available space for car");
    }
}