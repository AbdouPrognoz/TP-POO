package Farm;

import Zone.*;
import Crops.*;

import java.util.*;

public class FarmSystem {

    private Map<String, Zone> zones = new HashMap<>();

    //  1: Manage zones

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

    public void deactivateZone(String code) {
        getZone(code).suspend();
    }

    public void reactivateZone(String code) {
        getZone(code).reactivate();
    }



    public void assignCropToZone(String zoneCode, Crops crop) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cz)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        cz.addCrop(crop);
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

    // 2 : Manage crops

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

    public void registerCrop(String zoneCode, Crops crop) {
        assignCropToZone(zoneCode, crop);
    }

    public void updateCropStage(String zoneCode, String cropId, GrowthStage stage) {
        Crops crop = findCrop(zoneCode, cropId);
        crop.setGrowthStage(stage);
    }

    public void displayCropStage(String zoneCode, String cropId) {
        Crops crop = findCrop(zoneCode, cropId);
        System.out.println("Crop " + cropId + " stage: " + crop.getGrowthStage());
    }

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

    // 3 : Manage animals


}

