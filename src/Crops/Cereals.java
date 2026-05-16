package Crops;

import java.time.LocalDate;

public class Cereals extends Crops {

    private C cerealType;

    public Cereals(String id, LocalDate plantingDate, LocalDate harvestDate, GrowthStage growthStage, double minPH, double maxPH, double minMoisture, double maxMoisture, C cerealType) {
        super(id, plantingDate, harvestDate, growthStage, minPH, maxPH, minMoisture, maxMoisture);
        this.cerealType = cerealType;
    }

    public C getCerealType() {
        return cerealType;
    }

    @Override
    public String getFamily() {
        return "CEREALS";
    }
}
