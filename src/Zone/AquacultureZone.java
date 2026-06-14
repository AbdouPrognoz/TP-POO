package Zone;

import Animals.Aqua;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class AquacultureZone extends Zone implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Aqua> species = new ArrayList<>();
    private FeedingProgram feedingProgram;
    private final List<FeedingProgram> feedingProgramHistory = new ArrayList<>();

    public AquacultureZone(String code, String name, String type) {
        super(code, name, type);
    }

    public void addSpecies(Aqua speciesObj) {
        species.add(speciesObj);
    }

    public List<Aqua> getSpecies() { return species; }

    public void setFeedingProgram(FeedingProgram program) {
        this.feedingProgram = program;
        if (program != null) {
            this.feedingProgramHistory.add(program);
        }
    }

    public FeedingProgram getFeedingProgram() {
        return feedingProgram;
    }

    public List<FeedingProgram> getFeedingProgramHistory() {
        return feedingProgramHistory;
    }

    @Override
    public int getHostedCount() { return species.size(); }

    @Override
    public String getType() { return "AQUACULTURE"; }
}