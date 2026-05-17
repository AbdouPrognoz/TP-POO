import Alerts.*;
import Animals.*;
import Crops.*;
import Farm.FarmSystem;
import Readings.*;
import Sensors.*;
import Zone.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        FarmSystem farm = new FarmSystem();

        // 1. Setup Zones
        CropZone cropZone = new CropZone("CZ1", "Wheat Field", "CROP");
        LivestockZone livestockZone = new LivestockZone("LZ1", "Pasture", "LIVESTOCK");
        // Set boundary: Center (0,0) with radius 10
        livestockZone.setBoundary(new Coordinates(0, 0), 10.0);

        farm.addZone(cropZone);
        farm.addZone(livestockZone);

        // 2. Test Polymorphic Production History
        System.out.println("--- Testing Production History ---");
        farm.recordProduction(cropZone, new CropProductionRecord(LocalDate.now(), "C1", 500.5));
        farm.recordProduction(livestockZone, new LivestockProductionRecord(LocalDate.now(), "A1", 250.0));
        
        System.out.println("Crop Zone History: " + cropZone.getProductionHistory().get(0).getDetails());
        System.out.println("Livestock Zone History: " + livestockZone.getProductionHistory().get(0).getDetails());

        // 3. Test Sensor Reassignment and Animal Tracking
        System.out.println("\n--- Testing Boundary Alerts ---");
        Land cow = new Land("A1", Ruminant.COW, 5, 250.0, HealthStatus.HEALTHY);
        farm.registerAnimal("LZ1", cow);

        GpsSensor gps = new GpsSensor("GPS-1", null); // Zone null, as it's attached to animal
        cow.addSensor(gps);
        System.out.println("Cow sensors count: " + cow.getSensors().size());

        // Test valid position (5, 5) - distance 7.07 < 10
        System.out.println("Recording valid position...");
        farm.addGpsReading("GPS-1", "R-1", "2026-05-20T10:00", 5.0, 5.0);
        
        // Test invalid position (15, 15) - distance 21.2 > 10
        System.out.println("Recording out-of-bounds position (15,15)...");
        farm.addGpsReading("GPS-1", "R-2", "2026-05-20T11:00", 15.0, 15.0);

        // 4. Verify Alerts
        System.out.println("\n--- Verifying Alerts ---");
        System.out.println("Total alerts generated: " + farm.getAlertsHistory().size());
        for (Alert alert : farm.getAlertsHistory()) {
            System.out.println("Alert: " + alert.getMessage() + " | Severity: " + alert.getSeverity());
        }
    }
}
