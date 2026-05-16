package Zone;

public class FeedingProgram {
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
