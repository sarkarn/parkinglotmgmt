package com.example.parkinglot.multilevel;

/**
 * Result of finding an optimal parking level
 */
public class OptimalLevelResult {
    private final boolean successful;
    private final ParkingLevel selectedLevel;
    private final String reason;
    
    public OptimalLevelResult(boolean successful, ParkingLevel selectedLevel, String reason) {
        this.successful = successful;
        this.selectedLevel = selectedLevel;
        this.reason = reason;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public ParkingLevel getSelectedLevel() {
        return selectedLevel;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return String.format("OptimalLevelResult[success=%s, level=%s, reason=%s]",
                successful, 
                selectedLevel != null ? selectedLevel.getLevelName() : "None",
                reason);
    }
}