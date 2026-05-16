package Crops;

import java.time.LocalDate;

public class Fruits extends Crops {

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
