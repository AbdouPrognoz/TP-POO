package Sensors;

import Zone.Zone;
import java.io.Serializable;

public class SoilSensor extends NumericSensor implements Serializable {
    private static final long serialVersionUID = 1L;
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
