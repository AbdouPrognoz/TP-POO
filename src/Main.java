import Alerts.*;
import Animals.*;
import Crops.*;
import Farm.FarmSystem;
import Sensors.*;
import Zone.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        FarmSystem farm = new FarmSystem();

        CropZone cropZone = new CropZone("CZ1", "Crop Zone 1", "CROP");
        LivestockZone livestockZone = new LivestockZone("LZ1", "Livestock Zone 1", "LIVESTOCK");
        AquacultureZone aquacultureZone = new AquacultureZone("AZ1", "Aqua Zone 1", "AQUACULTURE");

        farm.addZone(cropZone);
        farm.addZone(livestockZone);
        farm.addZone(aquacultureZone);

        farm.editZone("CZ1", "Updated Crop Zone", null);
        farm.deactivateZone("AZ1");
        farm.reactivateZone("AZ1");
        farm.recordProduction("CZ1", 2500.0);

        farm.registerCrop("CZ1", new Cereals("C1",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 7, 10),
                GrowthStage.SOWING,
                6.0, 7.5, 40, 70,
                C.WHEAT));

        farm.registerCrop("CZ1", new Vegetables("V1",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 1),
                GrowthStage.GERMINATION,
                5.5, 7.0, 45, 75,
                V.TOMATO));

        farm.updateCropStage("CZ1", "C1", GrowthStage.GROWTH);
        farm.displayCropStage("CZ1", "C1");
        farm.displayAllCropStages("CZ1");
        farm.cropStatusReport("CZ1");

        farm.registerAnimal("LZ1", new Land("A1", Poultry.CHICKEN, 1, 2.1, HealthStatus.HEALTHY));
        farm.registerAnimal("LZ1", new Land("A2", Ruminant.COW, 3, 250.0, HealthStatus.HEALTHY));
        farm.registerAnimal("AZ1", new Aqua("AQ1", AquaSpecies.FISH, 1, 0.8, HealthStatus.HEALTHY));

        farm.logIllness("LZ1", "A1", "Flu symptoms");
        farm.logWeightChange("LZ1", "A2", 260.0);

        farm.defineFeedingProgram("LZ1", "Corn Feed", 0.25);
        farm.displayFeedingSchedule("LZ1");

        EnvironmentalSensor tempSensor = new EnvironmentalSensor(
                "ENV-1",
                cropZone,
                10.0,
                30.0,
                "C",
                EnvironmentalMetric.TEMPERATURE
        );
        SoilSensor soilSensor = new SoilSensor(
                "SOIL-1",
                cropZone,
                6.0,
                8.0,
                "pH",
                SoilMetric.PH
        );
        GpsSensor gpsSensor = new GpsSensor("GPS-1", cropZone);

        farm.addSensor(tempSensor);
        farm.addSensor(soilSensor);
        farm.addSensor(gpsSensor);

        farm.addNumericReading("ENV-1", "R-1", "2026-05-10T10:00", 20.0);
        farm.addNumericReading("ENV-1", "R-2", "2026-05-10T11:00", 35.0);
        farm.addNumericReading("ENV-1", "R-3", "2026-05-10T12:00", 50.0);
        farm.addNumericReading("SOIL-1", "R-4", "2026-05-10T12:05", 7.0);
        farm.addGpsReading("GPS-1", "R-5", "2026-05-10T12:30", 36.75, 3.06);

        System.out.println("Readings by ENV-1: " + farm.getReadingsBySensor("ENV-1").size());
        System.out.println("Readings by CZ1: " + farm.getReadingsByZone("CZ1").size());
        System.out.println("Alerts: " + farm.getAlertsHistory().size());
        System.out.println("Critical alerts: " + farm.filterAlertsBySeverity(Severity.CRITICAL).size());
        System.out.println("Active alerts sorted: " + farm.getActiveAlertsSorted().size());
    }
}