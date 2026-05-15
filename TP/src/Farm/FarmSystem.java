package Farm;

import Zone.*;
import Crops.*;
import Animals.*;

import java.time.LocalDate;
import java.util.*;

public class FarmSystem {

    private Map<String, Zone> zones = new HashMap<>();

    // ===== 1) Manage zones =====

    private Zone getZone(String code) {
        Zone zone = zones.get(code);
        if (zone == null) throw new NoSuchElementException("Zone not found: " + code);
        return zone;
    }

    public void addZone(Zone zone) {
        if (zones.containsKey(zone.getCode())) {
            throw new IllegalArgumentException("Zone already exists: " + zone.getCode());
        }
        zones.put(zone.getCode(), zone);
    }

    public void editZone(String code, String newName, String newType) {
        Zone zone = getZone(code);
        if (newName != null) zone.setName(newName);
        if (newType != null) zone.setType(newType);
    }

    public void deactivateZone(String code) { getZone(code).suspend(); }
    public void reactivateZone(String code) { getZone(code).reactivate(); }

    // assign crops or animals to zones
    public void assignCropToZone(String zoneCode, Crops crop) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cz)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        cz.addCrop(crop);
    }

    public void assignAnimalToZone(String zoneCode, Animal animal) {
        Zone zone = getZone(zoneCode);

        if (zone instanceof LivestockZone lz && animal instanceof Land) {
            lz.addAnimal((Land) animal);
        } else if (zone instanceof AquacultureZone az && animal instanceof Aqua) {
            az.addSpecies((Aqua) animal);
        } else {
            throw new IllegalArgumentException("Animal type does not match zone.");
        }
    }

    public void displayZonesOverview() {
        for (Zone z : zones.values()) {
            System.out.println(
                    z.getCode() + " | " + z.getName() +
                            " | status=" + z.getStatus() +
                            " | hosted=" + z.getHostedCount()
            );
        }
    }

    public void recordProduction(String zoneCode, double value) {
        getZone(zoneCode).setProduction(value);
    }

    // ===== 2) Manage crops =====

    private Crops findCrop(String zoneCode, String cropId) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cz)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        return cz.getCrops().stream()
                .filter(c -> c.getId().equals(cropId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Crop not found: " + cropId));
    }

    // register crop
    public void registerCrop(String zoneCode, Crops crop) {
        assignCropToZone(zoneCode, crop);
    }

    // update growth stage
    public void updateCropStage(String zoneCode, String cropId, GrowthStage stage) {
        Crops crop = findCrop(zoneCode, cropId);
        crop.setGrowthStage(stage);
    }

    // display growth stage for a crop
    public void displayCropStage(String zoneCode, String cropId) {
        Crops crop = findCrop(zoneCode, cropId);
        System.out.println("Crop " + cropId + " stage: " + crop.getGrowthStage());
    }

    // display growth stage for all crops in a zone
    public void displayAllCropStages(String zoneCode) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cz)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        System.out.println("Growth stages in zone " + zoneCode + ":");
        for (Crops c : cz.getCrops()) {
            System.out.println("- " + c.getId() + " : " + c.getGrowthStage());
        }
    }

    // crop status report
    public void cropStatusReport(String zoneCode) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cz)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }

        System.out.println("Crop status report for zone " + zoneCode + ":");
        for (Crops c : cz.getCrops()) {
            System.out.println("- " + c.getId()
                    + " | family=" + c.getFamily()
                    + " | stage=" + c.getGrowthStage()
                    + " | planting=" + c.getPlantingDate()
                    + " | harvest=" + c.getHarvestDate()
                    + " | pH=[" + c.getMinPH() + "," + c.getMaxPH() + "]"
                    + " | moisture=[" + c.getMinMoisture() + "," + c.getMaxMoisture() + "]");
        }
    }

    // ===== 3) Manage animals =====

    // register animal
    public void registerAnimal(String zoneCode, Animal animal) {
        assignAnimalToZone(zoneCode, animal);
    }

    private Animal findAnimal(String zoneCode, String animalId) {
        Zone zone = getZone(zoneCode);

        if (zone instanceof LivestockZone lz) {
            return lz.getAnimals().stream()
                    .filter(a -> a.getId().equals(animalId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Animal not found: " + animalId));
        }

        if (zone instanceof AquacultureZone az) {
            return az.getSpecies().stream()
                    .filter(a -> a.getId().equals(animalId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Animal not found: " + animalId));
        }

        throw new IllegalArgumentException("Zone is not animal zone.");
    }

    // log illness
    public void logIllness(String zoneCode, String animalId, String notes) {
        Animal animal = findAnimal(zoneCode, animalId);
        animal.setHealthStatus(HealthStatus.SICK);
        animal.addHealthEvent(new HealthEvent(LocalDate.now(), HealthEventType.ILLNESS, notes));
    }

    // log weight change
    public void logWeightChange(String zoneCode, String animalId, double newWeight) {
        Animal animal = findAnimal(zoneCode, animalId);
        double old = animal.getWeight();
        animal.setWeight(newWeight);
        animal.addHealthEvent(new HealthEvent(LocalDate.now(), HealthEventType.WEIGHT_CHANGE,
                "Weight " + old + " -> " + newWeight));
    }

    // feeding schedules per zone
    public void defineFeedingProgram(String zoneCode, String feedType, double quantityPerMeal) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof LivestockZone lz)) {
            throw new IllegalArgumentException("Zone is not livestock zone.");
        }
        lz.setFeedingProgram(new FeedingProgram(feedType, quantityPerMeal));
    }

    public void displayFeedingSchedule(String zoneCode) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof LivestockZone lz)) {
            throw new IllegalArgumentException("Zone is not livestock zone.");
        }
        System.out.println("Feeding program for " + zoneCode + ": " + lz.getFeedingProgram());
    }
}
