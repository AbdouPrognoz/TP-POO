package Sensors;

import Zone.Zone;
import java.io.Serializable;

public class WaterSensor extends NumericSensor implements Serializable {
    private static final long serialVersionUID = 1L;
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
