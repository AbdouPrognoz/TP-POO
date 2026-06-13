package Readings;
import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;
    private final double latitude;
    private final double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
