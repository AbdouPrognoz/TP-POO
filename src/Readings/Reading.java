package Readings;

public class Reading {
    private final String id;
    private final String timestamp;
    private final String sensorId;
    private final Double numericValue;
    private final String unit;
    private final Coordinates coordinates;
    private final ReadingLevel level;

    public Reading(String id,
                   String timestamp,
                   String sensorId,
                   Double numericValue,
                   String unit,
                   Coordinates coordinates,
                   ReadingLevel level) {
        this.id = id;
        this.timestamp = timestamp;
        this.sensorId = sensorId;
        this.numericValue = numericValue;
        this.unit = unit;
        this.coordinates = coordinates;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Double getNumericValue() {
        return numericValue;
    }

    public String getUnit() {
        return unit;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public ReadingLevel getLevel() {
        return level;
    }
}
