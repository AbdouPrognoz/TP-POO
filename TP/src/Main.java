import Farm.FarmSystem;
import Zone.*;
import Crops.*;
import Animals.*;

import java.time.LocalDate;
    // just a quick test
public class Main {
    public static void main(String[] args) {

        FarmSystem farm = new FarmSystem();

        // ===== 1) Manage zones =====
        farm.addZone(new CropZone("CZ1", "Crop Zone 1", "CROP"));
        farm.addZone(new LivestockZone("LZ1", "Livestock Zone 1", "LIVESTOCK"));
        farm.addZone(new AquacultureZone("AZ1", "Aqua Zone 1", "AQUACULTURE"));

        farm.editZone("CZ1", "Updated Crop Zone", null);
        farm.deactivateZone("AZ1");
        farm.reactivateZone("AZ1");
        farm.recordProduction("CZ1", 2500.0);

        farm.displayZonesOverview();

        // ===== 2) Manage crops =====
        farm.registerCrop("CZ1", new Cereals("C1",
                LocalDate.of(2026,1,10),
                LocalDate.of(2026,7,10),
                GrowthStage.SOWING,
                6.0, 7.5, 40, 70,
                C.WHEAT));

        farm.registerCrop("CZ1", new Vegetables("V1",
                LocalDate.of(2026,2,1),
                LocalDate.of(2026,6,1),
                GrowthStage.GERMINATION,
                5.5, 7.0, 45, 75,
                V.TOMATO));

        farm.updateCropStage("CZ1", "C1", GrowthStage.GROWTH);
        farm.displayCropStage("CZ1", "C1");
        farm.displayAllCropStages("CZ1");
        farm.cropStatusReport("CZ1");

        // ===== 3) Manage animals =====
        farm.registerAnimal("LZ1", new Land("A1", Poultry.CHICKEN, 1, 2.1, HealthStatus.HEALTHY));
        farm.registerAnimal("LZ1", new Land("A2", Ruminant.COW, 3, 250.0, HealthStatus.HEALTHY));
        farm.registerAnimal("AZ1", new Aqua("AQ1", AquaSpecies.FISH, 1, 0.8, HealthStatus.HEALTHY));

        farm.logIllness("LZ1", "A1", "Flu symptoms");
        farm.logWeightChange("LZ1", "A2", 260.0);

        farm.defineFeedingProgram("LZ1", "Corn Feed", 0.25);
        farm.displayFeedingSchedule("LZ1");
    }
}
