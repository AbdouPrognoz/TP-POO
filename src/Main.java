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

        // 1. ZONES
        System.out.println("--- 1. ZONE MANAGEMENT ---");

        CropZone cropZone = new CropZone("CZ1", "Crop Zone 1", "CROP");
        AquacultureZone aquaZone = new AquacultureZone("AZ1", "Aqua Zone 1", "AQUACULTURE");
        LivestockZone livestockZone = new LivestockZone("LZ1", "Livestock Zone 1", "LIVESTOCK");
        livestockZone.setBoundary(new Coordinates(0, 0), 10.0);

        farm.addZone(cropZone);
        farm.addZone(livestockZone);
        farm.addZone(aquaZone);

        farm.editZone("CZ1", "Updated Crop Zone 1", "CROP");
        farm.deactivateZone("AZ1");
        farm.reactivateZone("AZ1");

        // 2. CROPS
        System.out.println("\n--- 2. CROPS MANAGEMENTS ---");

        Cereals wheat = new Cereals("W1", LocalDate.now(), LocalDate.now().plusMonths(6), GrowthStage.SOWING, 6.0, 7.5, 40, 70, C.WHEAT);
        farm.registerCrop("CZ1", wheat);

        farm.updateCropStage("CZ1", "W1", GrowthStage.GROWTH);

        farm.displayCropStage("CZ1", "W1");

          // farm.displayAllCropStages("CZ1");

        farm.cropStatusReport("CZ1");

        farm.recordProduction("CZ1", new CropProductionRecord(LocalDate.now(), "W1", 1000.0));

        farm.displayProductionHistory("CZ1");
          //farm.displayAllProductionHistories();

        // 3. ANIMALS
        System.out.println("\n--- 3. ANIMALS MANAGEMENT ---");

        Land cow = new Land("A1", Ruminant.COW, 5, 250.0, HealthStatus.HEALTHY);
        farm.registerAnimal("LZ1", cow);

        farm.logIllness("LZ1", "A1", "Fever");
        farm.logWeightChange("LZ1", "A1", 260.0);

        farm.defineFeedingProgram("LZ1", "Corn Feed", 0.25);
        farm.displayFeedingSchedule("LZ1");


        Aqua fish = new Aqua("AQ1", AquaSpecies.FISH, 1, 0.5, HealthStatus.HEALTHY);
        farm.registerAnimal("AZ1", fish);

        farm.displayZonesOverview();


        // 4. Sensors & Alerts (Requirements: sensors on entities, boundary alerts)

        System.out.println("\n--- 4. ALERTS & SENSORS ---");
        GpsSensor gps = new GpsSensor("GPS-A1", livestockZone);
        cow.addSensor(gps);
        
        // Test Boundary (Center 0,0 Rad 10)
        farm.addGpsReading("GPS-A1", "R1", "2026-05-17T10:00", 20.0, 20.0); // Outside
        
        EnvironmentalSensor temp = new EnvironmentalSensor("ENV1", cropZone, 10.0, 30.0, "C", EnvironmentalMetric.TEMPERATURE);
        farm.addSensor(temp);
        farm.addNumericReading("ENV1", "R2", "2026-05-17T11:00", 5.0); // Trigger Warning/Critical
        
        System.out.println("Alerts list (Count: " + farm.getAlertsHistory().size() + "):");
        for(Alert a : farm.getAlertsHistory()) {
            System.out.println("- " + a.getMessage() + " [" + a.getSeverity() + "]");
        }

        System.out.println("Active alerts sorted:");
        for (Alert a : farm.getActiveAlertsSorted()) {
          System.out.println("- " + a.getMessage() + " [" + a.getSeverity() + "] zone=" + a.getZoneCode() + " type=" + a.getSensorType());
        }

        System.out.println("Alerts in zone LZ1: " + farm.filterAlertsByZone("LZ1").size());
        System.out.println("Alerts from GpsSensor: " + farm.filterAlertsBySensorType("GpsSensor").size());
        System.out.println("Critical alerts: " + farm.filterAlertsByLevel(Severity.CRITICAL).size());
    }
}
