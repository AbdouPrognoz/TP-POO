package Alerts;

public class Alert {
    private final String id;
    private final String timestamp;
    private final String sensorId;
    private final String readingId;
    private final Severity severity;
    private AlertStatus status;
    private final String message;

    public Alert(String id,
                 String timestamp,
                 String sensorId,
                 String readingId,
                 Severity severity,
                 AlertStatus status,
                 String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.sensorId = sensorId;
        this.readingId = readingId;
        this.severity = severity;
        this.status = status;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getReadingId() {
        return readingId;
    }

    public Severity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void acknowledge() {
        if (status == AlertStatus.ACTIVE) {
            status = AlertStatus.ACKNOLEGED;
        }
    }

    public void delete() {
        status = AlertStatus.DELETED;
    }
}
