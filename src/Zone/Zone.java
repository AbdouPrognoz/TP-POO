package Zone;

import Sensors.Sensor;
import Sensors.SensorStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Zone {

    private String code;
    private String name;
    private String type;
    private StatusZone status = StatusZone.ACTIVE;
    private final List<ProductionRecord> productionHistory = new ArrayList<>();
    private final List<Sensor> sensors = new ArrayList<>();
    private final Set<String> zoneSuspendedSensorIds = new HashSet<>();

    public Zone(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public StatusZone getStatus() { return status; }
    public List<ProductionRecord> getProductionHistory() { return Collections.unmodifiableList(productionHistory); }

    public void addProductionRecord(ProductionRecord record) {
        productionHistory.add(record);
    }

    public void suspend() {
        this.status = StatusZone.SUSPENDED;
        zoneSuspendedSensorIds.clear();  // we will refill this set
        for (Sensor sensor : sensors) {
            if (sensor.getStatus() == SensorStatus.ACTIVE) {
                sensor.setStatus(SensorStatus.SUSPENDED);
                zoneSuspendedSensorIds.add(sensor.getId());
            }
        }
    }

    public void reactivate() {
        this.status = StatusZone.ACTIVE;
        for (String sensorId : zoneSuspendedSensorIds) {  // only reactivate sensors that were suspended by suspension of the zone 
            Sensor sensor = findSensorById(sensorId);
            if (sensor != null && sensor.getStatus() == SensorStatus.SUSPENDED) {
                sensor.setStatus(SensorStatus.ACTIVE);
            }
        }
        zoneSuspendedSensorIds.clear();
    }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }

    public void addSensor(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor cannot be null.");
        }
        if (sensor.getZone() != this) {
            throw new IllegalArgumentException("Sensor zone mismatch: " + sensor.getId());
        }
        for (Sensor existing : sensors) {
            if (existing.getId().equals(sensor.getId())) {
                throw new IllegalArgumentException("Sensor already exists: " + sensor.getId());
            }
        }
        if (status == StatusZone.SUSPENDED && sensor.getStatus() == SensorStatus.ACTIVE) {
            sensor.setStatus(SensorStatus.SUSPENDED);
            zoneSuspendedSensorIds.add(sensor.getId());
        }
        sensors.add(sensor);
    }

    public List<Sensor> getSensors() {
        return Collections.unmodifiableList(sensors);
    }

    public Sensor findSensorById(String sensorId) {
        for (Sensor sensor : sensors) {
            if (sensor.getId().equals(sensorId)) {
                return sensor;
            }
        }
        return null;
    }

    public abstract int getHostedCount();
}