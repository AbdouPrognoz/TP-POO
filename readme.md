
This is an OOP school project 

# TP-OOP Smart Farming

Smart Farming is a Java 17 object-oriented project developed as a practical assignment for ESI-Alger. It models a farm management system with zones, crops, animals, sensors, readings, alerts, and a JavaFX user interface.

The application is organized around three kinds of zones:

- Crop zones for cereals, vegetables, and fruits
- Livestock zones for land animals with biometric and GPS monitoring
- Aquaculture zones for aquatic species and water-quality monitoring

Each zone can be monitored through sensors, produce readings over time, and generate alerts when values go out of range. The project also includes production history, alert tracking, and a graphical desktop interface.

## Features

- Manage farm zones and their hosted entities
- Register crops, animals, and sensors
- Track production history by zone type
- Record numeric, GPS, environmental, soil, biometric, and water readings
- Automatically generate alerts from out-of-range readings
- Filter and manage alerts by severity, sensor, and period
- Display a JavaFX dashboard for farm data visualization

## Technologies

- Java 17
- Maven
- JavaFX 21
- Object-oriented design with packages for `Farm`, `Zone`, `Animals`, `Crops`, `Sensors`, `Readings`, `Alerts`, and `ui`

## Project Structure

```text
src/
├── Alerts/
├── Animals/
├── Crops/
├── Farm/
├── Readings/
├── Sensors/
├── Zone/
├── ui/
└── Main.java
```

## Running the Project

### With Maven

```bash
mvn clean javafx:run
```

This launches the JavaFX application defined in `ui.SmartFarmApp`.

### Compile Only

```bash
mvn clean compile
```

### Run the Console Demo

If you want to execute the console entry point instead of the JavaFX application, compile the project and run `Main` from the output directory:

```bash
find src -name '*.java' -print0 | xargs -0 javac -d out
java -cp out Main
```

## Notes

- The project uses a shared `src` source directory configured in `pom.xml`.
- JavaFX dependencies are already declared in the Maven configuration.
- `Main.java` contains a simple scenario that exercises the core farm services.

## Academic Context

This project was created for the ESI-Alger 2CP 2025/2026 OOP coursework.
