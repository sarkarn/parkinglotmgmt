package com.example.parkinglot.model;

/**
 * Represents the status and statistics of the parking lot.
 */
public class LotStatus {
    private final int totalSpaces;
    private final int totalCompactSpaces;
    private final int totalRegularSpaces;
    private final int availableSpaces;
    private final int availableCompactSpaces;
    private final int availableRegularSpaces;
    private final int occupiedSpaces;
    private final int occupiedCompactSpaces;
    private final int occupiedRegularSpaces;
    private final int vanOccupiedSpaces;
    private final boolean isFull;
    private final boolean isEmpty;
    private final boolean allCompactOccupied;
    private final boolean allRegularOccupied;
    
    public LotStatus(int totalSpaces, int totalCompactSpaces, int totalRegularSpaces,
                     int availableSpaces, int availableCompactSpaces, int availableRegularSpaces,
                     int occupiedSpaces, int occupiedCompactSpaces, int occupiedRegularSpaces,
                     int vanOccupiedSpaces, boolean isFull, boolean isEmpty,
                     boolean allCompactOccupied, boolean allRegularOccupied) {
        this.totalSpaces = totalSpaces;
        this.totalCompactSpaces = totalCompactSpaces;
        this.totalRegularSpaces = totalRegularSpaces;
        this.availableSpaces = availableSpaces;
        this.availableCompactSpaces = availableCompactSpaces;
        this.availableRegularSpaces = availableRegularSpaces;
        this.occupiedSpaces = occupiedSpaces;
        this.occupiedCompactSpaces = occupiedCompactSpaces;
        this.occupiedRegularSpaces = occupiedRegularSpaces;
        this.vanOccupiedSpaces = vanOccupiedSpaces;
        this.isFull = isFull;
        this.isEmpty = isEmpty;
        this.allCompactOccupied = allCompactOccupied;
        this.allRegularOccupied = allRegularOccupied;
    }
    
    // Getters
    public int getTotalSpaces() { return totalSpaces; }
    public int getTotalCompactSpaces() { return totalCompactSpaces; }
    public int getTotalRegularSpaces() { return totalRegularSpaces; }
    public int getAvailableSpaces() { return availableSpaces; }
    public int getAvailableCompactSpaces() { return availableCompactSpaces; }
    public int getAvailableRegularSpaces() { return availableRegularSpaces; }
    public int getOccupiedSpaces() { return occupiedSpaces; }
    public int getOccupiedCompactSpaces() { return occupiedCompactSpaces; }
    public int getOccupiedRegularSpaces() { return occupiedRegularSpaces; }
    public int getVanOccupiedSpaces() { return vanOccupiedSpaces; }
    public boolean isFull() { return isFull; }
    public boolean isEmpty() { return isEmpty; }
    public boolean isAllCompactOccupied() { return allCompactOccupied; }
    public boolean isAllRegularOccupied() { return allRegularOccupied; }
    
    @Override
    public String toString() {
        return String.format(
            "LotStatus{total=%d, available=%d, occupied=%d, " +
            "compact(total=%d, available=%d, occupied=%d), " +
            "regular(total=%d, available=%d, occupied=%d), " +
            "vanSpaces=%d, full=%s, empty=%s}",
            totalSpaces, availableSpaces, occupiedSpaces,
            totalCompactSpaces, availableCompactSpaces, occupiedCompactSpaces,
            totalRegularSpaces, availableRegularSpaces, occupiedRegularSpaces,
            vanOccupiedSpaces, isFull, isEmpty
        );
    }
}