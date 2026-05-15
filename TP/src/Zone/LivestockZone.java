package Zone;

import java.util.ArrayList;
import java.util.List;

public class LivestockZone extends Zone {

    private List<String> animals = new ArrayList<>();

    public LivestockZone(String code, String name, String type) {
        super(code, name, type);
    }

    public void addAnimal(String animalId) {
        animals.add(animalId);
    }

    public List<String> getAnimals() {
        return animals;
    }

    @Override
    public int getHostedCount() { return animals.size(); }
}

    @Override
    public String getType() {
        return "LIVESTOCK";
    }
}
