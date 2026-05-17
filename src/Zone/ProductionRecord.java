package Zone;

import java.time.LocalDate;

public abstract class ProductionRecord {
    private LocalDate date;

    public ProductionRecord(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public abstract String getDetails();
}
