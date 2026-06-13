package ui.model;

import Alerts.Alert;
import Alerts.AlertStatus;
import Alerts.Severity;
import Animals.Aqua;
import Animals.AquaSpecies;
import Animals.Animal;
import Animals.HealthEvent;
import Animals.HealthEventType;
import Animals.HealthStatus;
import Animals.Land;
import Animals.Poultry;
import Animals.Ruminant;
import Crops.C;
import Crops.Cereals;
import Crops.Crops;
import Crops.F;
import Crops.Fruits;
import Crops.GrowthStage;
import Crops.V;
import Crops.Vegetables;
import Farm.FarmSystem;
import Readings.Coordinates;
import Readings.Reading;
import Readings.ReadingLevel;
import Sensors.BiometricMetric;
import Sensors.BiometricSensor;
import Sensors.EnvironmentalMetric;
import Sensors.EnvironmentalSensor;
import Sensors.GpsSensor;
import Sensors.Sensor;
import Sensors.SensorStatus;
import Sensors.SoilMetric;
import Sensors.SoilSensor;
import Sensors.WaterMetric;
import Sensors.WaterSensor;
import Zone.AquaProductionRecord;
import Zone.AquacultureZone;
import Zone.CropProductionRecord;
import Zone.CropZone;
import Zone.LivestockProductionRecord;
import Zone.LivestockZone;
import Zone.StatusZone;
import Zone.Zone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FarmDataStore {
    private final FarmSystem farmSystem = new FarmSystem();

    public FarmDataStore() {
        initializeSampleData();
    }

    public FarmSystem getFarmSystem() {
        return farmSystem;
    }

    public List<Zone> getZones() {
        return new ArrayList<>(farmSystem.getZonesView());
    }

    public List<Crops> getAllCrops() {
        List<Crops> crops = new ArrayList<>();
        for (Zone zone : getZones()) {
            if (zone instanceof CropZone cropZone) {
                crops.addAll(cropZone.getCrops());
            }
        }
        return crops;
    }

    public List<Animal> getAllAnimals() {
        List<Animal> animals = new ArrayList<>();
        for (Zone zone : getZones()) {
            if (zone instanceof LivestockZone livestockZone) {
                animals.addAll(livestockZone.getAnimals());
            } else if (zone instanceof AquacultureZone aquacultureZone) {
                animals.addAll(aquacultureZone.getSpecies());
            }
        }
        return animals;
    }

    public List<Sensor> getAllSensors() {
        List<Sensor> sensors = new ArrayList<>();
        for (Zone zone : getZones()) {
            sensors.addAll(zone.getSensors());
            if (zone instanceof LivestockZone livestockZone) {
                for (Animal animal : livestockZone.getAnimals()) {
                    sensors.addAll(animal.getSensors());
                }
            } else if (zone instanceof AquacultureZone aquacultureZone) {
                for (Animal animal : aquacultureZone.getSpecies()) {
                    sensors.addAll(animal.getSensors());
                }
            }
        }
        return sensors.stream()
                .filter(sensor -> sensor != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Sensor::getId, sensor -> sensor, (left, right) -> left),
                        map -> new ArrayList<>(map.values())
                ));
    }

    public List<Alert> getAlerts() {
        return farmSystem.getAlertsHistory().stream()
                .sorted(Comparator.comparing(Alert::getSeverity).reversed().thenComparing(Alert::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<Alert> getRecentAlerts(int limit) {
        return getAlerts().stream().limit(limit).collect(Collectors.toList());
    }

    public long countActiveZones() {
        return getZones().stream().filter(zone -> zone.getStatus() == StatusZone.ACTIVE).count();
    }

    public long countSuspendedZones() {
        return getZones().stream().filter(zone -> zone.getStatus() == StatusZone.SUSPENDED).count();
    }

    public long countCriticalAlerts() {
        return farmSystem.filterAlertsByLevel(Severity.CRITICAL).size();
    }

    public long countWarningAlerts() {
        return farmSystem.filterAlertsByLevel(Severity.WARNING).size();
    }

    public long countTotalSensors() {
        return getAllSensors().size();
    }

    public List<Reading> getReadingsForSensor(String sensorId) {
        return farmSystem.getReadingsBySensor(sensorId);
    }

    public List<Reading> getReadingsForSensorInPeriod(String sensorId, String start, String end) {
        return farmSystem.getReadingsBySensor(sensorId).stream()
                .filter(reading -> reading.getTimestamp().compareTo(start) >= 0 && reading.getTimestamp().compareTo(end) <= 0)
                .collect(Collectors.toList());
    }

    public List<Sensor> getSensorsForChart() {
        return getAllSensors().stream()
                .filter(sensor -> !sensor.getHistory().isEmpty())
                .collect(Collectors.toList());
    }

    public void acknowledgeAlert(String alertId) {
        farmSystem.acknowledgeAlert(alertId);
    }

    public void deleteAlert(String alertId) {
        farmSystem.deleteAlert(alertId);
    }

    public void suspendZone(String code) {
        farmSystem.deactivateZone(code);
    }

    public void reactivateZone(String code) {
        farmSystem.reactivateZone(code);
    }

    public void updateGrowthStage(String zoneCode, String cropId, GrowthStage stage) {
        farmSystem.updateCropStage(zoneCode, cropId, stage);
    }

    public void updateWeight(String zoneCode, String animalId, double newWeight) {
        farmSystem.logWeightChange(zoneCode, animalId, newWeight);
    }

    public void registerHealthEvent(String zoneCode, String animalId, HealthEventType type, String notes) {
        Animal animal = findAnimal(zoneCode, animalId);
        if (animal != null) {
            animal.addHealthEvent(new HealthEvent(LocalDate.now(), type, notes));
        }
    }

    public void configureSensorStatus(String sensorId, SensorStatus status) {
        farmSystem.changeSensorStatus(sensorId, status);
    }

    public void addDemoGpsReading() {
        farmSystem.addGpsReading("GPS-A1", "R-Demo", LocalDateTime.now().toString(), 20.0, 20.0);
    }

    public void addDemoNumericReading() {
        farmSystem.addNumericReading("ENV1", "R-Demo-ENV", LocalDateTime.now().toString(), 5.0);
    }

    public void addZone(Zone zone) {
        farmSystem.addZone(zone);
    }

    public void addCropToZone(String zoneCode, Crops crop) {
        farmSystem.assignCropToZone(zoneCode, crop);
    }

    public void addAnimalToZone(String zoneCode, Animal animal) {
        farmSystem.assignAnimalToZone(zoneCode, animal);
    }

    public void addSensorToFarm(Sensor sensor) {
        farmSystem.addSensor(sensor);
    }

    public List<Reading> getReadingsForZone(String zoneCode) {
        return farmSystem.getReadingsByZone(zoneCode);
    }

    public List<Reading> getReadingsForZoneInPeriod(String zoneCode, String start, String end) {
        return farmSystem.getReadingsByZoneAndPeriod(zoneCode, start, end);
    }

    public void seedDemoAlertData() {
        // already created by demo readings; method kept for UI refresh symmetry
    }

    public String formatAlertState(Alert alert) {
        return alert.getStatus().name();
    }

    public String formatSensorLatestReading(Sensor sensor) {
        List<Reading> history = sensor.getHistory();
        if (history.isEmpty()) {
            return "-";
        }
        Reading latest = history.get(history.size() - 1);
        if (latest.getCoordinates() != null) {
            return latest.getCoordinates().getLatitude() + ", " + latest.getCoordinates().getLongitude();
        }
        if (latest.getNumericValue() != null) {
            return latest.getNumericValue() + (latest.getUnit() != null ? " " + latest.getUnit() : "");
        }
        return "-";
    }

    private Animal findAnimal(String zoneCode, String animalId) {
        Zone zone = farmSystem.getZoneView(zoneCode);
        if (zone instanceof LivestockZone livestockZone) {
            return livestockZone.getAnimals().stream().filter(animal -> animal.getId().equals(animalId)).findFirst().orElse(null);
        }
        if (zone instanceof AquacultureZone aquacultureZone) {
            return aquacultureZone.getSpecies().stream().filter(animal -> animal.getId().equals(animalId)).findFirst().orElse(null);
        }
        return null;
    }

    private void initializeSampleData() {
        CropZone cropZone = new CropZone("CZ1", "Crop Zone 1", "CROP");
        AquacultureZone aquaZone = new AquacultureZone("AZ1", "Aqua Zone 1", "AQUACULTURE");
        LivestockZone livestockZone = new LivestockZone("LZ1", "Livestock Zone 1", "LIVESTOCK");
        livestockZone.setBoundary(new Coordinates(0, 0), 10.0);

        farmSystem.addZone(cropZone);
        farmSystem.addZone(aquaZone);
        farmSystem.addZone(livestockZone);

        Cereals wheat = new Cereals("W1", LocalDate.now().minusDays(12), LocalDate.now().plusMonths(6), GrowthStage.GROWTH, 6.0, 7.5, 40, 70, C.WHEAT);
        farmSystem.assignCropToZone("CZ1", wheat);
        farmSystem.recordProduction("CZ1", new CropProductionRecord(LocalDate.now().minusDays(1), "W1", 1000.0));

        Land cow = new Land("A1", Ruminant.COW, 5, 250.0, HealthStatus.HEALTHY);
        farmSystem.assignAnimalToZone("LZ1", cow);
        cow.addSensor(new GpsSensor("GPS-A1", livestockZone));
        cow.addSensor(new BiometricSensor("BIO-A1", livestockZone, 35.0, 40.0, "C", BiometricMetric.BODY_TEMPERATURE));
        farmSystem.recordProduction("LZ1", new LivestockProductionRecord(LocalDate.now().minusDays(2), "A1", 260.0));

        Aqua fish = new Aqua("AQ1", AquaSpecies.FISH, 1, 0.5, HealthStatus.HEALTHY);
        farmSystem.assignAnimalToZone("AZ1", fish);
        fish.addSensor(new WaterSensor("WAT1", aquaZone, 5.0, 9.0, "mg/L", WaterMetric.DISSOLVED_OXYGEN));
        farmSystem.recordProduction("AZ1", new AquaProductionRecord(LocalDate.now().minusDays(3), "FISH", 12.5));

        farmSystem.addSensor(new EnvironmentalSensor("ENV1", cropZone, 10.0, 30.0, "C", EnvironmentalMetric.TEMPERATURE));
        farmSystem.addSensor(new SoilSensor("SOIL1", cropZone, 6.0, 7.5, "%", SoilMetric.HUMIDITY));

        farmSystem.addNumericReading("ENV1", "R2", LocalDateTime.now().minusHours(2).toString(), 5.0);
        farmSystem.addGpsReading("GPS-A1", "R1", LocalDateTime.now().minusHours(1).toString(), 20.0, 20.0);
    }
}
