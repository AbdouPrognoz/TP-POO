package Sensors;

import Readings.Coordinates;
import Readings.Reading;
import Readings.ReadingLevel;
import Zone.LivestockZone;
import Zone.Zone;
import java.io.Serializable;

public class GpsSensor extends Sensor implements Serializable {
    private static final long serialVersionUID = 1L;
    public GpsSensor(String id, Zone zone) {
        super(id, zone, 0.0, 0.0);
    }

    public Reading recordReading(String readingId, String timestamp, double latitude, double longitude) {
        if (!isActive()) {
            return null;
        }
        Coordinates coords = new Coordinates(latitude, longitude);
        ReadingLevel level = isOutsideZone(latitude, longitude) ? ReadingLevel.CRITICAL : ReadingLevel.NORMAL;
        Reading reading = new Reading(readingId, timestamp, getId(), null, null, coords, level);
        addToHistory(reading);
        return reading;
    }

    public boolean isOutsideZone(double latitude, double longitude) {
        if (!(getZone() instanceof LivestockZone livestockZone)) {
            return false;
        }
        return livestockZone.isOutside(new Coordinates(latitude, longitude));
    }
}
