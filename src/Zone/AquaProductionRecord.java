package Zone;

import java.time.LocalDate;
import java.io.Serializable;

public class AquaProductionRecord extends ProductionRecord implements Serializable {
    private static final long serialVersionUID = 1L;
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
