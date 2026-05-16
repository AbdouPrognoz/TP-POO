package Crops;

import java.time.LocalDate;

public class Vegetables extends Crops {

    private V vegetableType;

    public Vegetables(String id, LocalDate plantingDate, LocalDate harvestDate, GrowthStage growthStage, double minPH, double maxPH, double minMoisture, double maxMoisture, V vegetableType) {
        super(id, plantingDate, harvestDate, growthStage, minPH, maxPH, minMoisture, maxMoisture);
        this.vegetableType = vegetableType;
    }

    public V getVegetableType() {
        return vegetableType;
    }

    @Override
    public String getFamily() {
        return "VEGETABLES";
    }
}