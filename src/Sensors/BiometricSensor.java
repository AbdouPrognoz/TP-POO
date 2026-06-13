package Sensors;

import Zone.Zone;

public class BiometricSensor extends NumericSensor {

    public BiometricSensor(String id,
                           Zone zone,
                           double minThreshold,
                           double maxThreshold,
                           String unit) {
        super(id, zone, minThreshold, maxThreshold, unit);
    }
}
