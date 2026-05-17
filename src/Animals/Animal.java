package Animals;

import Sensors.Sensor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Animal {

    private String id;
    private String species;
    private int age;
    private double weight;
    private HealthStatus healthStatus;

    private List<HealthEvent> healthEvents = new ArrayList<>();
    private final List<Sensor> sensors = new ArrayList<>();

    public Animal(String id, String species, int age, double weight, HealthStatus healthStatus) {
        this.id = id;
        this.species = species;
        this.age = age;
        this.weight = weight;
        this.healthStatus = healthStatus;
    }

    public String getId() { return id; }
    public String getSpecies() { return species; }
    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public HealthStatus getHealthStatus() { return healthStatus; }

    public void setAge(int age) { this.age = age; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setHealthStatus(HealthStatus status) { this.healthStatus = status; }

    public void addHealthEvent(HealthEvent event) { healthEvents.add(event); }
    public List<HealthEvent> getHealthEvents() { return healthEvents; }

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    public List<Sensor> getSensors() {
        return Collections.unmodifiableList(sensors);
    }

    public abstract String getCategory();
}