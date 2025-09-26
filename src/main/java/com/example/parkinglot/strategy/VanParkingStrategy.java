package com.example.parkinglot.strategy;

import com.example.parkinglot.model.*;
import java.util.List;

/**
 * Parking strategy for vans.
 * Vans require two contiguous regular spaces in the same row.
 */
public class VanParkingStrategy implements ParkingStrategy {
    
    @Override
    public ParkingResult allocateSpaces(String vehicleId, List<List<ParkingSpace>> rows) {
        // Vans need two contiguous regular spaces in the same row
        for (List<ParkingSpace> row : rows) {
            for (int i = 0; i < row.size() - 1; i++) {
                ParkingSpace space1 = row.get(i);
                ParkingSpace space2 = row.get(i + 1);
                
                if (!space1.isOccupied() && !space2.isOccupied() &&
                    space1.getType() == SpaceType.REGULAR && space2.getType() == SpaceType.REGULAR) {
                    
                    return ParkingResult.success(List.of(space1.getIdentifier(), space2.getIdentifier()));
                }
            }
        }
        return ParkingResult.failure("No two contiguous regular spaces available for van");
    }
}