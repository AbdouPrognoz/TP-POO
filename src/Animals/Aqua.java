package Animals;
import java.io.Serializable;

public class Aqua extends Animal implements Serializable {
    private static final long serialVersionUID = 1L;

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
