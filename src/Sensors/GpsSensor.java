package Sensors;

import Readings.Coordinates;
import Readings.Reading;
import Readings.ReadingLevel;
import Zone.Zone;

public class GpsSensor extends Sensor {
    public GpsSensor(String id, Zone zone) {
        super(id, zone, 0.0, 0.0);
    }

    public Reading recordReading(String readingId, String timestamp, double latitude, double longitude) {
        if (!isActive()) {
            return null;
        }
        Coordinates coords = new Coordinates(latitude, longitude);
        Reading reading = new Reading(readingId, timestamp, getId(), null, null, coords, ReadingLevel.NORMAL);
        addToHistory(reading);
        return reading;
    }

    public boolean isOutsideZone(double latitude, double longitude) {
        return false;
    }
}
