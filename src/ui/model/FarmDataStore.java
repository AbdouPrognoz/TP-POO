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
        Map<String, Zone> loadedZones = Persistence.PersistenceService.loadZones();
        List<Alert> loadedAlerts = Persistence.PersistenceService.loadAlerts();
        
        farmSystem.loadZones(loadedZones);
        farmSystem.loadAlerts(loadedAlerts);

        // If it's the first time running and no data exists, load some mock data
        if (loadedZones.isEmpty()) {
            System.out.println("No persisted data found. Initializing sample data...");
            initializeSampleDataIfEmpty();
        } else {
            System.out.println("Loaded " + loadedZones.size() + " zones from persistence.");
        }
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

    public void updateHealthStatus(String zoneCode, String animalId, HealthStatus newStatus) {
        Animal animal = findAnimal(zoneCode, animalId);
        if (animal != null) {
            HealthStatus old = animal.getHealthStatus();
            animal.setHealthStatus(newStatus);
            animal.addHealthEvent(new HealthEvent(LocalDate.now(),
                    HealthEventType.HEALTH_STATUS_CHANGE,
                    "État de santé : " + old.name() + " → " + newStatus.name()));
        }
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

    public void addManualNumericReading(String sensorId, double value) {
        String readingId = "R-" + System.currentTimeMillis() % 100000;
        farmSystem.addNumericReading(sensorId, readingId, LocalDateTime.now().toString(), value);
    }

    public void addManualGpsReading(String sensorId, double lat, double lon) {
        String readingId = "R-" + System.currentTimeMillis() % 100000;
        farmSystem.addGpsReading(sensorId, readingId, LocalDateTime.now().toString(), lat, lon);
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

    public void updateSensorThresholds(String sensorId, double min, double max) {
        Sensor sensor = farmSystem.findSensorById(sensorId);
        if (sensor != null) {
            sensor.setMinThreshold(min);
            sensor.setMaxThreshold(max);
        }
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

    private void initializeSampleDataIfEmpty() {
        // --- 1. Crop Zones ---
        CropZone cropZone1 = new CropZone("CZ1", "Champ de Blé Nord", "CROP");
        CropZone cropZone2 = new CropZone("CZ2", "Serre Tomates", "CROP");
        farmSystem.addZone(cropZone1);
        farmSystem.addZone(cropZone2);

        Cereals wheat = new Cereals("W1", LocalDate.now().minusDays(30), LocalDate.now().plusMonths(5), GrowthStage.GROWTH, 6.0, 7.5, 40, 70, C.WHEAT);
        Vegetables tomato = new Vegetables("T1", LocalDate.now().minusDays(15), LocalDate.now().plusMonths(2), GrowthStage.GERMINATION, 5.5, 6.8, 50, 80, V.TOMATO);
        farmSystem.assignCropToZone("CZ1", wheat);
        farmSystem.assignCropToZone("CZ2", tomato);
        
        farmSystem.recordProduction("CZ1", new CropProductionRecord(LocalDate.now().minusDays(1), "W1", 1200.0));
        farmSystem.recordProduction("CZ2", new CropProductionRecord(LocalDate.now().minusDays(5), "T1", 450.0));

        farmSystem.addSensor(new EnvironmentalSensor("ENV1", cropZone1, 10.0, 30.0, "C", EnvironmentalMetric.TEMPERATURE));
        farmSystem.addSensor(new SoilSensor("SOIL1", cropZone2, 5.0, 7.0, "%", SoilMetric.HUMIDITY));

        // --- 2. Aquaculture Zones ---
        AquacultureZone aquaZone1 = new AquacultureZone("AZ1", "Bassin Poissons", "AQUACULTURE");
        AquacultureZone aquaZone2 = new AquacultureZone("AZ2", "Bassin Crevettes", "AQUACULTURE");
        farmSystem.addZone(aquaZone1);
        farmSystem.addZone(aquaZone2);

        Aqua fish = new Aqua("AQ1", AquaSpecies.FISH, 1, 0.5, HealthStatus.HEALTHY);
        Aqua shrimp = new Aqua("AQ2", AquaSpecies.SHRIMP, 1, 0.05, HealthStatus.HEALTHY);
        farmSystem.assignAnimalToZone("AZ1", fish);
        farmSystem.assignAnimalToZone("AZ2", shrimp);
        
        farmSystem.recordProduction("AZ1", new AquaProductionRecord(LocalDate.now().minusDays(2), "FISH", 15.0));

        fish.addSensor(new WaterSensor("WAT1", aquaZone1, 5.0, 9.0, "mg/L", WaterMetric.DISSOLVED_OXYGEN));
        shrimp.addSensor(new WaterSensor("WAT2", aquaZone2, 6.5, 8.5, "pH", WaterMetric.PH));

        // --- 3. Livestock Zones ---
        LivestockZone livestockZone1 = new LivestockZone("LZ1", "Pâturage Vaches", "LIVESTOCK");
        livestockZone1.setBoundary(new Coordinates(36.75, 3.05), 50.0);
        LivestockZone livestockZone2 = new LivestockZone("LZ2", "Poulailler", "LIVESTOCK");
        livestockZone2.setBoundary(new Coordinates(36.76, 3.06), 20.0);
        farmSystem.addZone(livestockZone1);
        farmSystem.addZone(livestockZone2);

        Land cow = new Land("A1", Ruminant.COW, 5, 250.0, HealthStatus.HEALTHY);
        Land chicken = new Land("A2", Poultry.CHICKEN, 1, 2.5, HealthStatus.HEALTHY);
        farmSystem.assignAnimalToZone("LZ1", cow);
        farmSystem.assignAnimalToZone("LZ2", chicken);
        
        farmSystem.recordProduction("LZ1", new LivestockProductionRecord(LocalDate.now().minusDays(3), "A1", 300.0));
        farmSystem.recordProduction("LZ2", new LivestockProductionRecord(LocalDate.now().minusDays(1), "A2", 50.0)); // e.g. eggs

        cow.addSensor(new GpsSensor("GPS-A1", livestockZone1));
        cow.addSensor(new BiometricSensor("BIO-A1", livestockZone1, 35.0, 40.0, "C"));
        chicken.addSensor(new BiometricSensor("BIO-A2", livestockZone2, 38.0, 42.0, "C"));

        // --- Mock Readings ---
        for (int i = 0; i < 20; i++) {
            LocalDateTime dt = LocalDateTime.now().minusDays(20 - i).plusHours(i * 2);
            // Intentionally large variances to trigger Warning and Critical alerts
            farmSystem.addNumericReading("ENV1", "R-ENV-" + i, dt.toString(), 15.0 + Math.sin(i) * 20.0);
            farmSystem.addNumericReading("SOIL1", "R-SOIL-" + i, dt.toString(), 6.0 + Math.cos(i) * 3.0);
            farmSystem.addNumericReading("WAT1", "R-WAT-" + i, dt.toString(), 7.0 + Math.sin(i) * 4.0);
            farmSystem.addNumericReading("WAT2", "R-WAT2-" + i, dt.toString(), 7.5 + Math.cos(i) * 2.0);
            farmSystem.addNumericReading("BIO-A1", "R-BIO-" + i, dt.toString(), 37.5 + Math.cos(i) * 5.0);
            farmSystem.addNumericReading("BIO-A2", "R-BIO2-" + i, dt.toString(), 40.0 + Math.sin(i) * 4.0);
            
            // To trigger GPS alerts (LivestockZone radius is 50)
            double gpsX = 36.75 + (i * 5.0);
            double gpsY = 3.05 + (i * 5.0);
            farmSystem.addGpsReading("GPS-A1", "R-GPS-" + i, dt.toString(), gpsX, gpsY);
        }
    }
}
