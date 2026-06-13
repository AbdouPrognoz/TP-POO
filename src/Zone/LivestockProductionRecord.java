package Zone;

import java.time.LocalDate;
import java.io.Serializable;

public class LivestockProductionRecord extends ProductionRecord implements Serializable {
    private static final long serialVersionUID = 1L;
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
