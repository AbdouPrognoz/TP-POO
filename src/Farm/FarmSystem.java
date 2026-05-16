package Farm;

import Alerts.*;
import Animals.*;
import Crops.*;
import Readings.*;
import Sensors.*;
import Zone.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class FarmSystem {

    private final Map<String, Zone> zones = new HashMap<>();
    private final Map<String, Sensor> sensors = new HashMap<>();
    private final List<Reading> readings = new ArrayList<>();
    private final List<Alert> alerts = new ArrayList<>();

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

    public void recordProduction(String zoneCode, double value) {
        getZone(zoneCode).setProduction(value);
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
        if (sensor.getZone() == null) {
            throw new IllegalArgumentException("Sensor must be attached to a zone.");
        }
        if (!zones.containsKey(sensor.getZone().getCode())) {
            throw new IllegalArgumentException("Sensor zone is not registered: " + sensor.getZone().getCode());
        }
        sensors.put(sensor.getId(), sensor);
    }

    public Sensor findSensorById(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            System.out.println("Sensor found ");
        }
        return sensor;
    }

    public void changeSensorStatus(String sensorId, SensorStatus status) {
        Sensor sensor = findSensorById(sensorId);
        if (sensor != null) {
            sensor.setStatus(status);
        }
    }

    public Reading addNumericReading(String sensorId, String readingId, String timestamp, double value) {
        Sensor sensor = findSensorById(sensorId);
        if (!(sensor instanceof NumericSensor numericSensor)) {
            return null;
        }

        Reading reading = numericSensor.recordReading(readingId, timestamp, value);
        if (reading != null) {
            readings.add(reading);
            Alert alert = createAlertIfNeeded(reading);
            if (alert != null) {
                alerts.add(alert);
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
        if (reading != null) {
            readings.add(reading);
        }
        return reading;
    }

    public List<Reading> getReadingsBySensor(String sensorId) {
        List<Reading> result = new ArrayList<>();
        for (Reading reading : readings) {
            if (reading.getSensorId().equals(sensorId)) {
                result.add(reading);
            }
        }
        return result;
    }

    public List<Reading> getReadingsByZone(String zoneCode) {
        List<Reading> result = new ArrayList<>();
        for (Sensor sensor : sensors.values()) {
            Zone zone = sensor.getZone();
            if (zone != null && zone.getCode().equals(zoneCode)) {
                result.addAll(sensor.getHistory());
            }
        }
        return result;
    }

    public List<Reading> getReadingsByZoneAndPeriod(String zoneCode, String startTimestamp, String endTimestamp) {
        List<Reading> result = new ArrayList<>();
        for (Sensor sensor : sensors.values()) {
            Zone zone = sensor.getZone();
            if (zone != null && zone.getCode().equals(zoneCode)) {
                result.addAll(sensor.getHistoryBetween(startTimestamp, endTimestamp));
            }
        }
        return result;
    }

    public List<Alert> getActiveAlertsSorted() {
        List<Alert> active = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.getStatus() == AlertStatus.ACTIVE) {
                active.add(alert);
            }
        }
        active.sort(Comparator.comparing(Alert::getSeverity).reversed());
        return active;
    }

    public void acknowledgeAlert(String alertId) {
        Alert alert = findAlertById(alertId);
        if (alert != null) {
            alert.acknowledge();
        }
    }

    public void deleteAlert(String alertId) {
        Alert alert = findAlertById(alertId);
        if (alert != null) {
            alert.delete();
        }
    }

    public List<Alert> getAlertsHistory() {
        return Collections.unmodifiableList(alerts);
    }

    public List<Alert> filterAlertsBySeverity(Severity severity) {
        List<Alert> result = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.getSeverity() == severity) {
                result.add(alert);
            }
        }
        return result;
    }

    public List<Alert> filterAlertsBySensor(String sensorId) {
        List<Alert> result = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.getSensorId().equals(sensorId)) {
                result.add(alert);
            }
        }
        return result;
    }

    public List<Alert> filterAlertsByPeriod(String startTimestamp, String endTimestamp) {
        List<Alert> result = new ArrayList<>();
        LocalDateTime start = parseTimestamp(startTimestamp);
        LocalDateTime end = parseTimestamp(endTimestamp);
        for (Alert alert : alerts) {
            LocalDateTime timestamp = parseTimestamp(alert.getTimestamp());
            if ((timestamp.isEqual(start) || timestamp.isAfter(start))
                    && (timestamp.isEqual(end) || timestamp.isBefore(end))) {
                result.add(alert);
            }
        }
        return result;
    }

    private Alert findAlertById(String alertId) {
        for (Alert alert : alerts) {
            if (alert.getId().equals(alertId)) {
                return alert;
            }
        }
        return null;
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

    private LocalDateTime parseTimestamp(String timestamp) {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}