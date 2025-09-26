package com.example.parkinglot.model;

/**
 * Represents a vehicle with a unique identifier and type.
 */
public class Vehicle {
    private final String id;
    private final VehicleType type;
    
    public Vehicle(String id, VehicleType type) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        this.id = id.trim();
        this.type = type;
    }
    
    public String getId() {
        return id;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vehicle vehicle = (Vehicle) obj;
        return id.equals(vehicle.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Vehicle{id='%s', type=%s}", id, type);
    }
}