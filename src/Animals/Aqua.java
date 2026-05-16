package Animals;

public class Aqua extends Animal {

    private AquaSpecies species;

    public Aqua(String id, AquaSpecies species, int age, double weight, HealthStatus healthStatus) {
        super(id, species.name(), age, weight, healthStatus);
        this.species = species;
    }

    public AquaSpecies getAquaSpecies() {
        return species;
    }

    @Override
    public String getCategory() {
        return "AQUA";
    }
}
