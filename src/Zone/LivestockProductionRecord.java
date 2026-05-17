package Zone;

import java.time.LocalDate;

public class LivestockProductionRecord extends ProductionRecord {
    private String animalId;
    private double weight;

    public LivestockProductionRecord(LocalDate date, String animalId, double weight) {
        super(date);
        this.animalId = animalId;
        this.weight = weight;
    }

    @Override
    public String getDetails() {
        return "Livestock Production [Date: " + getDate() + ", Animal: " + animalId + ", Weight: " + weight + "]";
    }
}
