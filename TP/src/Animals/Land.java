package Animals;

public class Land extends Animal {

    private Poultry poultrySpecies;
    private Ruminant ruminantSpecies;

    // constructor for poultry
    public Land(String id, Poultry species, int age, double weight, HealthStatus status) {
        super(id, species.name(), age, weight, status);
        this.poultrySpecies = species;
        this.ruminantSpecies = null;
    }

    // constructor for ruminants
    public Land(String id, Ruminant species, int age, double weight, HealthStatus status) {
        super(id, species.name(), age, weight, status);
        this.ruminantSpecies = species;
        this.poultrySpecies = null;
    }

    public Poultry getPoultrySpecies() { return poultrySpecies; }
    public Ruminant getRuminantSpecies() { return ruminantSpecies; }

    @Override
    public String getCategory() { return "LAND"; }
}
