package Alerts;
import java.io.Serializable;

public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String timestamp;
    private final String sensorId;
    private final String readingId;
    private final String zoneCode;
    private final String sensorType;
    private final Severity severity;
    private AlertStatus status;
    private final String message;

    public Alert(String id,
                 String timestamp,
                 String sensorId,
                 String readingId,
                 String zoneCode,
                 String sensorType,
                 Severity severity,
                 AlertStatus status,
                 String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.sensorId = sensorId;
        this.readingId = readingId;
        this.zoneCode = zoneCode;
        this.sensorType = sensorType;
        this.severity = severity;
        this.status = status;
        this.message = message;
    }

    public Alert(String id,
                 String timestamp,
                 String sensorId,
                 String readingId,
                 Severity severity,
                 AlertStatus status,
                 String message) {
        this(id, timestamp, sensorId, readingId, null, null, severity, status, message);
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

    public String getZoneCode() {
        return zoneCode;
    }

    public String getSensorType() {
        return sensorType;
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
