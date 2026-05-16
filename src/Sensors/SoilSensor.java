package Sensors;

import Zone.Zone;

public class SoilSensor extends NumericSensor {
    private final SoilMetric metric;

    public SoilSensor(String id,
                      Zone zone,
                      double minThreshold,
                      double maxThreshold,
                      String unit,
                      SoilMetric metric) {
        super(id, zone, minThreshold, maxThreshold, unit);
        this.metric = metric;
    }

    public SoilMetric getMetric() {
        return metric;
    }
}
