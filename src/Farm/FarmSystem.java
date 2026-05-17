package Farm;

import Alerts.*;
import Animals.*;
import Crops.*;
import Readings.*;
import Sensors.*;
import Zone.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class FarmSystem {

    private final Map<String, Zone> zones = new HashMap<>();
    private final AlertRepository alertRepository = new AlertRepository();

    private Zone getZone(String code) {
        Zone zone = zones.get(code);
        if (zone == null) {
            throw new NoSuchElementException("Zone not found: " + code);
        }
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
        if (newName != null) {
            zone.setName(newName);
        }
        if (newType != null) {
            zone.setType(newType);
        }
    }

    public void deactivateZone(String code) {
        getZone(code).suspend();
    }

    public void reactivateZone(String code) {
        getZone(code).reactivate();
    }

    public void assignCropToZone(String zoneCode, Crops crop) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cropZone)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        cropZone.addCrop(crop);
    }

    public void assignAnimalToZone(String zoneCode, Animal animal) {
        Zone zone = getZone(zoneCode);

        if (zone instanceof LivestockZone livestockZone && animal instanceof Land landAnimal) {
            livestockZone.addAnimal(landAnimal);
        } else if (zone instanceof AquacultureZone aquacultureZone && animal instanceof Aqua aquaAnimal) {
            aquacultureZone.addSpecies(aquaAnimal);
        } else {
            throw new IllegalArgumentException("Animal type does not match zone.");
        }
    }

    public void displayZonesOverview() {
        for (Zone zone : zones.values()) {
            System.out.println(
                    zone.getCode() + " | " + zone.getName() +
                            " | status=" + zone.getStatus() +
                            " | hosted=" + zone.getHostedCount()
            );
        }
    }

    public void recordProduction(Zone zone, ProductionRecord record) {
        zone.addProductionRecord(record);
    }

    public void registerCrop(String zoneCode, Crops crop) {
        assignCropToZone(zoneCode, crop);
    }

    private Crops findCrop(String zoneCode, String cropId) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cropZone)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        return cropZone.getCrops().stream()
                .filter(crop -> crop.getId().equals(cropId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Crop not found: " + cropId));
    }

    public void updateCropStage(String zoneCode, String cropId, GrowthStage stage) {
        Crops crop = findCrop(zoneCode, cropId);
        crop.setGrowthStage(stage);
    }

    public void displayCropStage(String zoneCode, String cropId) {
        Crops crop = findCrop(zoneCode, cropId);
        System.out.println("Crop " + cropId + " stage: " + crop.getGrowthStage());
    }

    public void displayAllCropStages(String zoneCode) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cropZone)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }
        System.out.println("Growth stages in zone " + zoneCode + ":");
        for (Crops crop : cropZone.getCrops()) {
            System.out.println("- " + crop.getId() + " : " + crop.getGrowthStage());
        }
    }

    public void cropStatusReport(String zoneCode) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof CropZone cropZone)) {
            throw new IllegalArgumentException("Zone is not a crop zone.");
        }

        System.out.println("Crop status report for zone " + zoneCode + ":");
        for (Crops crop : cropZone.getCrops()) {
            System.out.println("- " + crop.getId()
                    + " | family=" + crop.getFamily()
                    + " | stage=" + crop.getGrowthStage()
                    + " | planting=" + crop.getPlantingDate()
                    + " | harvest=" + crop.getHarvestDate()
                    + " | pH=[" + crop.getMinPH() + "," + crop.getMaxPH() + "]"
                    + " | moisture=[" + crop.getMinMoisture() + "," + crop.getMaxMoisture() + "]");
        }
    }

    public void registerAnimal(String zoneCode, Animal animal) {
        assignAnimalToZone(zoneCode, animal);
    }

    private Animal findAnimal(String zoneCode, String animalId) {
        Zone zone = getZone(zoneCode);

        if (zone instanceof LivestockZone livestockZone) {
            return livestockZone.getAnimals().stream()
                    .filter(animal -> animal.getId().equals(animalId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Animal not found: " + animalId));
        }

        if (zone instanceof AquacultureZone aquacultureZone) {
            return aquacultureZone.getSpecies().stream()
                    .filter(animal -> animal.getId().equals(animalId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Animal not found: " + animalId));
        }

        throw new IllegalArgumentException("Zone is not animal zone.");
    }

    public void logIllness(String zoneCode, String animalId, String notes) {
        Animal animal = findAnimal(zoneCode, animalId);
        animal.setHealthStatus(HealthStatus.SICK);
        animal.addHealthEvent(new HealthEvent(java.time.LocalDate.now(), HealthEventType.ILLNESS, notes));
    }

    public void logWeightChange(String zoneCode, String animalId, double newWeight) {
        Animal animal = findAnimal(zoneCode, animalId);
        double oldWeight = animal.getWeight();
        animal.setWeight(newWeight);
        animal.addHealthEvent(new HealthEvent(java.time.LocalDate.now(), HealthEventType.WEIGHT_CHANGE,
                "Weight " + oldWeight + " -> " + newWeight));
    }

    public void defineFeedingProgram(String zoneCode, String feedType, double quantityPerMeal) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof LivestockZone livestockZone)) {
            throw new IllegalArgumentException("Zone is not livestock zone.");
        }
        livestockZone.setFeedingProgram(new FeedingProgram(feedType, quantityPerMeal));
    }

    public void displayFeedingSchedule(String zoneCode) {
        Zone zone = getZone(zoneCode);
        if (!(zone instanceof LivestockZone livestockZone)) {
            throw new IllegalArgumentException("Zone is not livestock zone.");
        }
        System.out.println("Feeding program for " + zoneCode + ": " + livestockZone.getFeedingProgram());
    }

    public void addSensor(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor cannot be null.");
        }
        if (sensor.getZone() != null) {
            Zone zone = getZone(sensor.getZone().getCode());
            zone.addSensor(sensor);
        } else if (sensor instanceof BiometricSensor || sensor instanceof GpsSensor) {
            // Logic to find animal associated with this sensor needs to be implemented or handled.
            // For now, we assume the sensor is added to the system via its associated animal.
            // This part might need further refinement based on how animal sensors are registered.
        } else {
            throw new IllegalArgumentException("Sensor must be attached to a zone or animal.");
        }
    }

    public Sensor findSensorById(String sensorId) {
        for (Zone zone : zones.values()) {
            Sensor sensor = zone.findSensorById(sensorId);
            if (sensor != null) {
                System.out.println("Sensor found ");
                return sensor;
            }
        }
        return null;
    }

    public void changeSensorStatus(String sensorId, SensorStatus status) {
        Sensor sensor = findSensorById(sensorId);
        if (sensor == null) {
            return;
        }
        Zone zone = sensor.getZone();
        if (zone != null && zone.getStatus() == StatusZone.SUSPENDED) {
            System.out.println("Sensor status cannot change because the zone is suspended.");
            return;
        }
        sensor.setStatus(status);
    }

    public Reading addNumericReading(String sensorId, String readingId, String timestamp, double value) {
        Sensor sensor = findSensorById(sensorId);
        if (!(sensor instanceof NumericSensor numericSensor)) {
            return null;
        }

        Reading reading = numericSensor.recordReading(readingId, timestamp, value);
        if (reading != null) {
            Alert alert = createAlertIfNeeded(reading);
            if (alert != null) {
                alertRepository.add(alert);
            }
        }
        return reading;
    }

    public Reading addGpsReading(String sensorId, String readingId, String timestamp, double latitude, double longitude) {
        Sensor sensor = findSensorById(sensorId);
        if (!(sensor instanceof GpsSensor gpsSensor)) {
            return null;
        }

        Reading reading = gpsSensor.recordReading(readingId, timestamp, latitude, longitude);
        
        // Find animal associated with the sensor
        for (Zone zone : zones.values()) {
            if (zone instanceof LivestockZone lz) {
                for (Land a : lz.getAnimals()) {
                    boolean found = false;
                    for (Sensor s : a.getSensors()) {
                        if (s.getId().equals(sensor.getId())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        if (lz.isOutside(new Coordinates(latitude, longitude))) {
                            alertRepository.add(new Alert("A-" + readingId, timestamp, sensorId, readingId, Severity.CRITICAL, AlertStatus.ACTIVE, "Animal " + a.getId() + " is out of bounds"));
                        }
                        break;
                    }
                }
            }
        }
        return reading;
    }

    public List<Reading> getReadingsBySensor(String sensorId) {
        Sensor sensor = findSensorById(sensorId);
        if (sensor == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(sensor.getHistory());
    }

    public List<Reading> getReadingsByZone(String zoneCode) {
        List<Reading> result = new ArrayList<>();
        Zone zone = getZone(zoneCode);
        for (Sensor sensor : zone.getSensors()) {
            result.addAll(sensor.getHistory());
        }
        return result;
    }

    public List<Reading> getReadingsByZoneAndPeriod(String zoneCode, String startTimestamp, String endTimestamp) {
        List<Reading> result = new ArrayList<>();
        Zone zone = getZone(zoneCode);
        for (Sensor sensor : zone.getSensors()) {
            result.addAll(sensor.getHistoryBetween(startTimestamp, endTimestamp));
        }
        return result;
    }

    public List<Alert> getActiveAlertsSorted() {
        return alertRepository.getActiveSorted();
    }

    public void acknowledgeAlert(String alertId) {
        alertRepository.acknowledge(alertId);
    }

    public void deleteAlert(String alertId) {
        alertRepository.delete(alertId);
    }

    public List<Alert> getAlertsHistory() {
        return alertRepository.getHistory();
    }

    public List<Alert> filterAlertsBySeverity(Severity severity) {
        return alertRepository.filterBySeverity(severity);
    }

    public List<Alert> filterAlertsBySensor(String sensorId) {
        return alertRepository.filterBySensor(sensorId);
    }

    public List<Alert> filterAlertsByPeriod(String startTimestamp, String endTimestamp) {
        return alertRepository.filterByPeriod(startTimestamp, endTimestamp);
    }

    private Alert createAlertIfNeeded(Reading reading) {
        if (reading.getLevel() == ReadingLevel.NORMAL) {
            return null;
        }

        Severity severity = reading.getLevel() == ReadingLevel.CRITICAL ? Severity.CRITICAL : Severity.WARNING;
        return new Alert(
                "A-" + reading.getId(),
                reading.getTimestamp(),
                reading.getSensorId(),
                reading.getId(),
                severity,
                AlertStatus.ACTIVE,
                "Reading out of range"
        );
    }

    
}