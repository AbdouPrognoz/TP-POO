package Alerts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AlertRepository {
    private final List<Alert> alerts = new ArrayList<>();

    public void add(Alert alert) {
        alerts.add(alert);
    }

    public List<Alert> getActiveSorted() {
        return alerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.ACTIVE)
                .sorted(Comparator.comparing(Alert::getTimestamp))
                .collect(Collectors.toList());
    }

    public void acknowledge(String alertId) {
        for (Alert alert : alerts) {
            if (alert.getId().equals(alertId)) {
                alert.acknowledge();
            }
        }
    }

    public void delete(String alertId) {
        alerts.removeIf(a -> a.getId().equals(alertId));
    }

    public List<Alert> getHistory() {
        return new ArrayList<>(alerts);
    }

    public List<Alert> filterBySeverity(Severity severity) {
        return alerts.stream()
                .filter(a -> a.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    public List<Alert> filterBySensor(String sensorId) {
        return alerts.stream()
                .filter(a -> a.getSensorId().equals(sensorId))
                .collect(Collectors.toList());
    }

    public List<Alert> filterByPeriod(String start, String end) {
        return alerts.stream()
                .filter(a -> a.getTimestamp().compareTo(start) >= 0 && a.getTimestamp().compareTo(end) <= 0)
                .collect(Collectors.toList());
    }
}
