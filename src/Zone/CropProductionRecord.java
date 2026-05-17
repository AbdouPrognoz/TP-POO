package Zone;

import java.time.LocalDate;

public class CropProductionRecord extends ProductionRecord {
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
