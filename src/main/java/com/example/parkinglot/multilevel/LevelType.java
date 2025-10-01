package com.example.parkinglot.multilevel;

/**
 * Enumeration representing different types of parking levels
 */
public enum LevelType {
    /**
     * Ground level parking - typically most convenient and accessible
     */
    GROUND("Ground Level"),
    
    /**
     * Elevated/above-ground level parking
     */
    ELEVATED("Elevated Level"),
    
    /**
     * Underground/basement level parking
     */
    UNDERGROUND("Underground Level");
    
    private final String displayName;
    
    LevelType(String displayName) {
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