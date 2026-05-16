package Animals;

import java.time.LocalDate;

public class HealthEvent {
    private LocalDate date;
    private HealthEventType type;
    private String notes;

    public HealthEvent(LocalDate date, HealthEventType type, String notes) {
        this.date = date;
        this.type = type;
        this.notes = notes;
    }

    public LocalDate getDate() { return date; }
    public HealthEventType getType() { return type; }
    public String getNotes() { return notes; }
}
