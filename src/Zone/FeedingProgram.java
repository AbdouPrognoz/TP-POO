package Zone;
import java.io.Serializable;

import java.time.LocalDate;

public class FeedingProgram implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate dateDefined;
    private String feedType;
    private double quantityPerMeal;

    public FeedingProgram(String feedType, double quantityPerMeal) {
        this.dateDefined = LocalDate.now();
        this.feedType = feedType;
        this.quantityPerMeal = quantityPerMeal;
    }

    public LocalDate getDateDefined() { return dateDefined; }
    public String getFeedType() { return feedType; }
    public double getQuantityPerMeal() { return quantityPerMeal; }

    @Override
    public String toString() {
        return "Feed=" + feedType + ", Qty/meal=" + quantityPerMeal;
    }
}
