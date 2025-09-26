package com.example.parkinglot.model;

/**
 * Represents a parking space with a unique identifier, type, and occupancy status.
 */
public class ParkingSpace {
    private final String identifier;
    private final SpaceType type;
    private String occupiedBy; // Vehicle identifier that occupies this space
    
    public ParkingSpace(String identifier, SpaceType type) {
        this.identifier = identifier;
        this.type = type;
        this.occupiedBy = null;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public SpaceType getType() {
        return type;
    }
    
    public boolean isOccupied() {
        return occupiedBy != null;
    }
    
    public String getOccupiedBy() {
        return occupiedBy;
    }
    
    public void occupy(String vehicleId) {
        if (isOccupied()) {
            throw new IllegalStateException("Space " + identifier + " is already occupied");
        }
        this.occupiedBy = vehicleId;
    }
    
    public void vacate() {
        this.occupiedBy = null;
    }
    
    @Override
    public String toString() {
        return String.format("ParkingSpace{id='%s', type=%s, occupied=%s}", 
                identifier, type, isOccupied() ? occupiedBy : "false");
    }
}