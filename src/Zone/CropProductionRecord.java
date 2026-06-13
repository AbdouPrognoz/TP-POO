package Zone;

import java.time.LocalDate;
import java.io.Serializable;

public class CropProductionRecord extends ProductionRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private String cropId;
    private double quantity;

    public CropProductionRecord(LocalDate date, String cropId, double quantity) {
        super(date);
        this.cropId = cropId;
        this.quantity = quantity;
    }

    @Override
    public String getDetails() {
        return "Crop Production [Date: " + getDate() + ", Crop: " + cropId + ", Yield: " + quantity + "]";
    }
}
