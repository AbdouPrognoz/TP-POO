package Sensors;

import Zone.Zone;

public class EnvironmentalSensor extends NumericSensor {
    private final EnvironmentalMetric metric;

    public EnvironmentalSensor(String id,
                               Zone zone,
                               double minThreshold,
                               double maxThreshold,
                               String unit,
                               EnvironmentalMetric metric) {
        super(id, zone, minThreshold, maxThreshold, unit);
        this.metric = metric;
    }

    public EnvironmentalMetric getMetric() {
        return metric;
    }
}
