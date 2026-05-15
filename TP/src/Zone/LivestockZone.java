package Zone;

import Animals.Land;
import java.util.ArrayList;
import java.util.List;

public class LivestockZone extends Zone {

    private List<Land> animals = new ArrayList<>();
    private FeedingProgram feedingProgram;

    public LivestockZone(String code, String name, String type) {
        super(code, name, type);
    }

    public void addAnimal(Land animal) {
        animals.add(animal);
    }

    public List<Land> getAnimals() {
        return animals;
    }

    public void setFeedingProgram(FeedingProgram program) {
        this.feedingProgram = program;
    }

    public FeedingProgram getFeedingProgram() {
        return feedingProgram;
    }

    @Override
    public int getHostedCount() { return animals.size(); }

    @Override
    public String getType() { return "LIVESTOCK"; }
}