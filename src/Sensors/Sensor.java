package Sensors;

import Readings.Reading;
import Readings.ReadingLevel;
import Zone.Zone;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Sensor {
    private final String id;
    private SensorStatus status;
    private final double minThreshold;
    private final double maxThreshold;
    private final Zone zone;
    private final List<Reading> history;

    protected Sensor(String id, Zone zone, double minThreshold, double maxThreshold) {
        this.id = id;
        this.zone = zone;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.status = SensorStatus.ACTIVE;
        this.history = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Zone getZone() {
        return zone;
    }

    public SensorStatus getStatus() {
        return status;
    }

    public void setStatus(SensorStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return status == SensorStatus.ACTIVE;
    }

    public double getMinThreshold() {
        return minThreshold;
    }

    public double getMaxThreshold() {
        return maxThreshold;
    }

    public ReadingLevel computeLevel(double value) {
        if (value < minThreshold || value > maxThreshold) {
            double span = Math.max(Math.abs(maxThreshold - minThreshold), 1.0);
            double overshoot = value < minThreshold ? minThreshold - value : value - maxThreshold;
            if (overshoot >= span * 0.5) {
                return ReadingLevel.CRITICAL;
            }
            return ReadingLevel.WARNING;
        }
        return ReadingLevel.NORMAL;
    }

    protected void addToHistory(Reading reading) {
        history.add(reading);
    }

    public List<Reading> getHistory() {
        return Collections.unmodifiableList(history);
    }

    // TODO: filtering by date range depends on timestamp format.
    public List<Reading> getHistoryBetween(String startTimestamp, String endTimestamp) {
        List<Reading> result = new ArrayList<>();
        LocalDateTime start = parseTimestamp(startTimestamp);
        LocalDateTime end = parseTimestamp(endTimestamp);
        for (Reading reading : history) {
            LocalDateTime timestamp = parseTimestamp(reading.getTimestamp());
            if ((timestamp.isEqual(start) || timestamp.isAfter(start))
                    && (timestamp.isEqual(end) || timestamp.isBefore(end))) {
                result.add(reading);
            }
        }
        return result;
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
