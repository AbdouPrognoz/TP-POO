package Zone;

import java.time.LocalDate;
import java.io.Serializable;

public abstract class ProductionRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;

    public ProductionRecord(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public abstract String getDetails();
}
