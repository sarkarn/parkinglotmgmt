package com.example.parkinglot.strategy;

import com.example.parkinglot.model.VehicleType;
import java.util.EnumMap;
import java.util.Map;

/**
 * Factory class for creating and managing parking strategies.
 * Uses the Strategy Pattern to provide different allocation algorithms for different vehicle types.
 */
public class ParkingStrategyFactory {
    
    private static final Map<VehicleType, ParkingStrategy> strategies = new EnumMap<>(VehicleType.class);
    
    static {
        // Initialize strategies for each vehicle type
        strategies.put(VehicleType.MOTORCYCLE, new MotorcycleParkingStrategy());
        strategies.put(VehicleType.CAR, new CarParkingStrategy());
        strategies.put(VehicleType.VAN, new VanParkingStrategy());
    }
    
    /**
     * Gets the appropriate parking strategy for the given vehicle type.
     * 
     * @param vehicleType The type of vehicle
     * @return The corresponding parking strategy
     * @throws IllegalArgumentException if vehicle type is not supported
     */
    public static ParkingStrategy getStrategy(VehicleType vehicleType) {
        ParkingStrategy strategy = strategies.get(vehicleType);
        if (strategy == null) {
            throw new IllegalArgumentException("No parking strategy available for vehicle type: " + vehicleType);
        }
        return strategy;
    }
    
    /**
     * Registers a custom parking strategy for a vehicle type.
     * This allows for runtime strategy modification if needed.
     * 
     * @param vehicleType The vehicle type
     * @param strategy The parking strategy to register
     */
    public static void registerStrategy(VehicleType vehicleType, ParkingStrategy strategy) {
        if (vehicleType == null || strategy == null) {
            throw new IllegalArgumentException("Vehicle type and strategy cannot be null");
        }
        strategies.put(vehicleType, strategy);
    }
}