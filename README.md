# Parking Lot Management System

A comprehensive object-oriented parking lot management system built with Java 17 and Maven. This system handles different vehicle types (motorcycles, cars, vans) with specific parking constraints and provides detailed status reporting.

## Features

- **Multi-vehicle support**: Motorcycles, cars, and vans with different parking rules
- **Flexible lot configuration**: Support for compact and regular spaces arranged in rows
- **Deterministic allocation**: Consistent space assignment using row-major order
- **Comprehensive status reporting**: Detailed statistics and per-row summaries
- **Edge case handling**: Graceful handling of full lots, duplicate parking, and invalid inputs
- **Robust testing**: Comprehensive unit test suite covering all scenarios

## Parking Rules

1. **Motorcycles**: Can park in any available space (compact or regular)
2. **Cars**: Can only park in regular spaces
3. **Vans**: Require two contiguous regular spaces in the same row

## Project Structure

```
parking-lot-management/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── parkinglot/
│   │                   ├── App.java              # Demo application
│   │                   ├── ParkingLot.java       # Core parking lot logic
│   │                   └── model/
│   │                       ├── SpaceType.java    # Parking space types enum
│   │                       ├── VehicleType.java  # Vehicle types enum
│   │                       ├── ParkingSpace.java # Individual parking space
│   │                       ├── Vehicle.java      # Vehicle representation
│   │                       ├── ParkingResult.java # Operation result wrapper
│   │                       └── LotStatus.java    # Status reporting class
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── parkinglot/
│                       ├── AppTest.java          # Model unit tests
│                       └── ParkingLotTest.java   # Core functionality tests
├── pom.xml
└── README.md
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

To build the project, run:

```bash
mvn clean compile
```

## Running Tests

To run the comprehensive test suite:

```bash
mvn test
```

## Running the Application

To run the demonstration application:

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.parkinglot.App"
```

## Usage Example

```java
import com.example.parkinglot.ParkingLot;
import com.example.parkinglot.model.*;
import java.util.*;

// Create parking lot configuration
List<SpaceType[]> config = Arrays.asList(
    new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.COMPACT},
    new SpaceType[]{SpaceType.REGULAR, SpaceType.REGULAR, SpaceType.REGULAR}
);

// Initialize parking lot
ParkingLot parkingLot = new ParkingLot(config);

// Park vehicles
ParkingResult result = parkingLot.parkVehicle("CAR001", VehicleType.CAR);
if (result.isSuccess()) {
    System.out.println("Parked in spaces: " + result.getAllocatedSpaces());
}

// Get status
LotStatus status = parkingLot.getLotStatus();
System.out.println("Available spaces: " + status.getAvailableSpaces());

// Remove vehicle
boolean removed = parkingLot.removeVehicle("CAR001");
```

## API Documentation

### Core Methods

#### `ParkingLot(List<SpaceType[]> rowConfigurations)`
Initializes the parking lot with the given configuration. Each array represents a row with space types.

#### `ParkingResult parkVehicle(String vehicleId, VehicleType vehicleType)`
Attempts to park a vehicle. Returns a `ParkingResult` with success status and allocated spaces.

#### `boolean removeVehicle(String vehicleId)`
Removes a vehicle from the parking lot. Returns `true` if successful, `false` if vehicle not found.

#### `LotStatus getLotStatus()`
Returns comprehensive status information including total/available/occupied spaces by type.

#### `List<String> getVehicleSpaces(String vehicleId)`
Returns the list of space identifiers currently occupied by a specific vehicle.

#### `List<String> getRowSummaries()`
Returns per-row summary of occupied and available spots.

## Design Decisions

### Object-Oriented Design
- **Separation of Concerns**: Clear separation between domain models, business logic, and presentation
- **Encapsulation**: Private fields with controlled access through public methods
- **Immutability**: Result objects and status reports are immutable for thread safety

### Deterministic Allocation
- Uses row-major order (row 1 to N, then space 1 to M within each row)
- Ensures consistent results across multiple runs with the same state

### Error Handling
- Graceful handling of invalid inputs (null, empty strings)
- Meaningful error messages for different failure scenarios
- No runtime exceptions for normal operation failures

### Space Identification
- Unique space identifiers using format "R{row}-{space}" (e.g., "R1-1", "R2-3")
- Human-readable and easy to reference

### Van Parking Strategy
- Searches for first available pair of contiguous regular spaces
- Both spaces must be in the same row as per requirements

## Testing Strategy

The test suite covers:

- **Happy Path Scenarios**: Normal parking and removal operations
- **Edge Cases**: Full lot, no suitable spaces, invalid inputs
- **Business Rules**: Vehicle-specific parking constraints
- **Duplicate Operations**: Parking same vehicle multiple times
- **Data Integrity**: Proper space allocation and deallocation
- **Status Reporting**: Accurate statistics and summaries

## Assumptions Made

1. **Space Identifiers**: Using "R{row}-{space}" format (1-indexed)
2. **Case Sensitivity**: Vehicle IDs are case-sensitive
3. **Whitespace Handling**: Vehicle IDs are trimmed of leading/trailing whitespace
4. **Van Definition**: Vans always require exactly 2 spaces
5. **Thread Safety**: Single-threaded usage assumed (no concurrent modifications)

## Potential Improvements

### Short Term
- Add vehicle size validation for spaces
- Implement reservation system for future parking
- Add parking duration tracking and fees calculation

### Long Term
- Database persistence layer
- RESTful API for remote access
- Real-time monitoring dashboard
- Multi-lot management support
- Automated space optimization algorithms

### Alternative Designs Considered

1. **Event-Driven Architecture**: Using events for parking operations
   - Pros: Better auditability and extensibility
   - Cons: Added complexity for current requirements

2. **Strategy Pattern for Allocation**: Different allocation strategies per vehicle type
   - Pros: More flexible allocation rules
   - Cons: Over-engineering for current simple rules

3. **Observer Pattern for Status Updates**: Real-time status notifications
   - Pros: Better for monitoring systems
   - Cons: Not needed for current synchronous operations

## Dependencies

- **JUnit 5**: Testing framework for comprehensive unit and integration tests
- **Java 17**: Modern Java features for cleaner, more maintainable code

---

*This project was developed as part of an interview assignment demonstrating object-oriented design principles, comprehensive testing, and clean code practices. All GitHub Copilot prompts used during development are documented in [COPILOT_PROMPTS.md](COPILOT_PROMPTS.md).*
