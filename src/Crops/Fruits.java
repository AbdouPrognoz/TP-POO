package Crops;

import java.time.LocalDate;
import java.io.Serializable;

public class Fruits extends Crops implements Serializable {
    private static final long serialVersionUID = 1L;

    private F fruitType;

    public Fruits(String id, LocalDate plantingDate, LocalDate harvestDate, GrowthStage growthStage, double minPH, double maxPH, double minMoisture, double maxMoisture, F fruitType) {
        super(id, plantingDate, harvestDate, growthStage, minPH, maxPH, minMoisture, maxMoisture);
        this.fruitType = fruitType;
    }

    public F getFruitType() {
        return fruitType;
    }

    @Override
    public String getFamily() {
        return "FRUITS";
    }
}
