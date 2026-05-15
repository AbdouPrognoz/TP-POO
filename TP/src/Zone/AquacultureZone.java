package Zone;

import java.util.ArrayList;
import java.util.List;

public class AquacultureZone extends Zone {

    private List<String> species = new ArrayList<>();

    public AquacultureZone(String code, String name, String type) {
        super(code, name, type);
    }

     public void addSpecies(String speciesId) {
        species.add(speciesId);
     }

     public List<String> getSpecies() {
         return species;
     }



    @Override
    public int getHostedCount() { return species.size(); }

    @Override
    public String getType() {
        return "AQUACULTURE";
    }
}
