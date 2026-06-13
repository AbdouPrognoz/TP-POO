package Zone;
import java.io.Serializable;

public class FeedingProgram implements Serializable {
    private static final long serialVersionUID = 1L;
    private String feedType;
    private double quantityPerMeal;

    public FeedingProgram(String feedType, double quantityPerMeal) {
        this.feedType = feedType;
        this.quantityPerMeal = quantityPerMeal;
    }

    public String getFeedType() { return feedType; }
    public double getQuantityPerMeal() { return quantityPerMeal; }

    @Override
    public String toString() {
        return "Feed=" + feedType + ", Qty/meal=" + quantityPerMeal;
    }
}
