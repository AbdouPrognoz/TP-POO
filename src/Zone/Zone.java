package Zone;

import Sensors.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Zone {

    private String code;
    private String name;
    private String type;
    private StatusZone status = StatusZone.ACTIVE;
    private double production = 0;
    private final List<Sensor> sensors = new ArrayList<>();

    public Zone(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public StatusZone getStatus() { return status; }
    public double getProduction() { return production; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }

    public void suspend() { this.status = StatusZone.SUSPENDED; }
    public void reactivate() { this.status = StatusZone.ACTIVE; }

    public double productionLevel() { return production; }
    public void setProduction(double production) { this.production = production; }

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