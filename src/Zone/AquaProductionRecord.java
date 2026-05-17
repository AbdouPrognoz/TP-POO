package Zone;

import java.time.LocalDate;

public class AquaProductionRecord extends ProductionRecord {
    private String species;
    private double harvestWeight;

    public AquaProductionRecord(LocalDate date, String species, double harvestWeight) {
        super(date);
        this.species = species;
        this.harvestWeight = harvestWeight;
    }

    @Override
    public String getDetails() {
        return "Aqua Production [Date: " + getDate() + ", Species: " + species + ", Harvest: " + harvestWeight + "]";
    }
}
