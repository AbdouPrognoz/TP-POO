package Crops;

import java.time.LocalDate;

public abstract class Crops {

    // attributes
    private String id;
    private LocalDate plantingDate;
    private LocalDate HarvestDate;
    private GrowthStage growthStage;
    private double minPH;
    private double maxPH;
    private double minMoisture;
    private double maxMoisture;


    // constructor
    public Crops(String id, LocalDate plantingDate, LocalDate HarvestDate, GrowthStage growthStage, double minPH, double maxPH, double minMoisture, double maxMoisture) {
        this.id = id;
        this.plantingDate = plantingDate;
        this.HarvestDate = HarvestDate;
        this.growthStage = growthStage;
        this.minPH = minPH;
        this.maxPH = maxPH;
        this.minMoisture = minMoisture;
        this.maxMoisture = maxMoisture;
    }

    // getters
    public String getId() { return id; }
    public LocalDate getPlantingDate() { return plantingDate; }
    public LocalDate getHarvestDate() { return HarvestDate; }
    public GrowthStage getGrowthStage() { return growthStage; }
    public double getMinPH() { return minPH; }
    public double getMaxPH() { return maxPH; }
    public double getMinMoisture() { return minMoisture; }
    public double getMaxMoisture() { return maxMoisture; }

    // update growth stage
    public void setGrowthStage(GrowthStage stage) { this.growthStage = stage; }


    // get the family of the crop
    public abstract String getFamily();
}
