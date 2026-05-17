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

        // 1. Gérer les zones et les entités
        System.out.println("--- 1. Gérer les zones ---");
        CropZone cropZone = new CropZone("CZ1", "Zone Cultures", "CROP");
        LivestockZone livestockZone = new LivestockZone("LZ1", "Zone Élevage", "LIVESTOCK");
        livestockZone.setBoundary(new Coordinates(0, 0), 10.0);
        AquacultureZone aquaZone = new AquacultureZone("AZ1", "Zone Aquaculture", "AQUACULTURE");
        
        farm.addZone(cropZone);
        farm.addZone(livestockZone);
        farm.addZone(aquaZone);
        
        farm.editZone("CZ1", "Zone Cultures Mise à Jour", "CROP");
        farm.deactivateZone("AZ1");
        farm.reactivateZone("AZ1");
        
        // 2. Gérer les cultures
        System.out.println("\n--- 2. Gérer les cultures ---");
        Cereals wheat = new Cereals("W1", LocalDate.now(), LocalDate.now().plusMonths(6), GrowthStage.SOWING, 6.0, 7.5, 40, 70, C.WHEAT);
        farm.registerCrop("CZ1", wheat);
        farm.updateCropStage("CZ1", "W1", GrowthStage.GROWTH);
        farm.cropStatusReport("CZ1");
        farm.recordProduction(cropZone, new CropProductionRecord(LocalDate.now(), "W1", 1000.0));

        // 3. Gérer les animaux
        System.out.println("\n--- 3. Gérer les animaux ---");
        Land cow = new Land("A1", Ruminant.COW, 5, 250.0, HealthStatus.HEALTHY);
        farm.registerAnimal("LZ1", cow);
        farm.logIllness("LZ1", "A1", "Fièvre");
        farm.logWeightChange("LZ1", "A1", 260.0);
        farm.defineFeedingProgram("LZ1", "Fourrage", 5.0);
        
        Aqua fish = new Aqua("AQ1", AquaSpecies.FISH, 1, 0.5, HealthStatus.HEALTHY);
        farm.registerAnimal("AZ1", fish);

        // 4. Sensors & Alerts (Requirements: sensors on entities, boundary alerts)
        System.out.println("\n--- 4. Capteurs et Alertes ---");
        GpsSensor gps = new GpsSensor("GPS-A1", null);
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
    }
}
