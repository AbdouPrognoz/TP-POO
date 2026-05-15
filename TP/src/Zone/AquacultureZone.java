package Zone;

import Animals.Aqua;
import java.util.ArrayList;
import java.util.List;

public class AquacultureZone extends Zone {

    private List<Aqua> species = new ArrayList<>();

    public AquacultureZone(String code, String name, String type) {
        super(code, name, type);
    }

    public void addSpecies(Aqua speciesObj) {
        species.add(speciesObj);
    }

    public List<Aqua> getSpecies() { return species; }

    @Override
    public int getHostedCount() { return species.size(); }

    @Override
    public String getType() { return "AQUACULTURE"; }
}