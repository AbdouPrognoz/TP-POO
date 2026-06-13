package Sensors;

import Readings.Reading;
import Readings.ReadingLevel;
import Zone.Zone;
import java.io.Serializable;

public abstract class NumericSensor extends Sensor implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String unit;

    protected NumericSensor(String id, Zone zone, double minThreshold, double maxThreshold, String unit) {
        super(id, zone, minThreshold, maxThreshold);
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public Reading recordReading(String readingId, String timestamp, double value) {
        if (!isActive()) {
            return null;
        }
        ReadingLevel level = computeLevel(value);
        Reading reading = new Reading(readingId, timestamp, getId(), value, unit, null, level);
        addToHistory(reading);
        return reading;
    }
}
