package Sensors;

import Zone.Zone;

public class BiometricSensor extends NumericSensor {
    private final BiometricMetric metric;

    public BiometricSensor(String id,
                           Zone zone,
                           double minThreshold,
                           double maxThreshold,
                           String unit,
                           BiometricMetric metric) {
        super(id, zone, minThreshold, maxThreshold, unit);
        this.metric = metric;
    }

    public BiometricMetric getMetric() {
        return metric;
    }
}
