package Sensors;

import Zone.Zone;
import java.io.Serializable;

public class EnvironmentalSensor extends NumericSensor implements Serializable {
    private static final long serialVersionUID = 1L;
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
