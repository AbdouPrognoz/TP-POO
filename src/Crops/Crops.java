package Crops;

import java.time.LocalDate;

public abstract class Crops {

    private String id;
    private LocalDate plantingDate;
    private LocalDate harvestDate;
    private GrowthStage growthStage;
    private double minPH;
    private double maxPH;
    private double minMoisture;
    private double maxMoisture;

    public Crops(String id, LocalDate plantingDate, LocalDate harvestDate, GrowthStage growthStage,
                 double minPH, double maxPH, double minMoisture, double maxMoisture) {
        this.id = id;
        this.plantingDate = plantingDate;
        this.harvestDate = harvestDate;
        this.growthStage = growthStage;
        this.minPH = minPH;
        this.maxPH = maxPH;
        this.minMoisture = minMoisture;
        this.maxMoisture = maxMoisture;
    }

    public String getId() { return id; }
    public LocalDate getPlantingDate() { return plantingDate; }
    public LocalDate getHarvestDate() { return harvestDate; }
    public GrowthStage getGrowthStage() { return growthStage; }
    public double getMinPH() { return minPH; }
    public double getMaxPH() { return maxPH; }
    public double getMinMoisture() { return minMoisture; }
    public double getMaxMoisture() { return maxMoisture; }

    public void setGrowthStage(GrowthStage stage) { this.growthStage = stage; }

    public abstract String getFamily();
}