package Sensors;

import Zone.Zone;

public class WaterSensor extends NumericSensor {
    private final WaterMetric metric;

    public WaterSensor(String id,
                       Zone zone,
                       double minThreshold,
                       double maxThreshold,
                       String unit,
                       WaterMetric metric) {
        super(id, zone, minThreshold, maxThreshold, unit);
        this.metric = metric;
    }

    public WaterMetric getMetric() {
        return metric;
    }
}
