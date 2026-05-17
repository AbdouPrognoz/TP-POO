package Zone;

import Animals.Land;
import Readings.Coordinates;
import java.util.ArrayList;
import java.util.List;

public class LivestockZone extends Zone {

    private List<Land> animals = new ArrayList<>();
    private FeedingProgram feedingProgram;
    private Coordinates center;
    private double radius;

    public LivestockZone(String code, String name, String type) {
        super(code, name, type);
    }

    public void setBoundary(Coordinates center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public boolean isOutside(Coordinates location) {
        if (center == null) return false;
        double distance = Math.sqrt(Math.pow(location.getLatitude() - center.getLatitude(), 2) +
                                    Math.pow(location.getLongitude() - center.getLongitude(), 2));
        return distance > radius;
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