package Sensors;

import Zone.Zone;
import java.io.Serializable;

public class BiometricSensor extends NumericSensor implements Serializable {
    private static final long serialVersionUID = 1L;

    public BiometricSensor(String id,
                           Zone zone,
                           double minThreshold,
                           double maxThreshold,
                           String unit) {
        super(id, zone, minThreshold, maxThreshold, unit);
    }
}
