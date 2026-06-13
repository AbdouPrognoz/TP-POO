package ui.view;

import Alerts.Alert;
import Alerts.AlertStatus;
import Alerts.Severity;
import Zone.Zone;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import ui.model.FarmDataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AlertView extends BorderPane {
    private final FarmDataStore store;
    
    // For Active Alerts Tab
    private final ObservableList<Alert> activeAlerts = FXCollections.observableArrayList();
    private final TableView<Alert> activeTableView = buildBaseTable();
    private final ChoiceBox<String> activeSeverityFilter = new ChoiceBox<>();
    
    // For History Tab
    private final ObservableList<Alert> historyAlerts = FXCollections.observableArrayList();
    private final TableView<Alert> historyTableView = buildBaseTable();

    public AlertView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-tab-min-width: 150px; -fx-tab-min-height: 30px; -fx-font-weight: bold;");
        
        Tab activeTab = new Tab("Alertes Actives");
        activeTab.setContent(buildActiveAlertsPane());
        
        Tab historyTab = new Tab("Historique & Filtres");
        historyTab.setContent(buildHistoryPane());
        
        tabPane.getTabs().addAll(activeTab, historyTab);
        
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == activeTab) {
                refreshActive();
            } else if (newTab == historyTab) {
                historyAlerts.setAll(store.getFarmSystem().getAlertsHistory());
            }
        });

        setCenter(tabPane);
        refreshActive();
    }

    // ──────────────────────────────────────────────
    //  Active Alerts Tab
    // ──────────────────────────────────────────────

    private BorderPane buildActiveAlertsPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16, 0, 0, 0));
        
        Button acknowledge = new Button("Acquitter");
        Button delete = new Button("Supprimer");
        Button refresh = new Button("Actualiser");

        activeSeverityFilter.getItems().addAll("Toutes", "CRITICAL", "WARNING");
        activeSeverityFilter.setValue("Toutes");
        activeSeverityFilter.setOnAction(e -> refreshActive());

        acknowledge.setOnAction(e -> {
            Alert selected = activeTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                store.acknowledgeAlert(selected.getId());
                refreshActive();
            }
        });
        delete.setOnAction(e -> {
            Alert selected = activeTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                store.deleteAlert(selected.getId());
                refreshActive();
            }
        });
        refresh.setOnAction(e -> refreshActive());

        HBox actionsBox = new HBox(10, acknowledge, delete, new Label("  Filtrer:"), activeSeverityFilter, refresh);
        actionsBox.setPadding(new Insets(0, 0, 16, 0));
        
        pane.setTop(actionsBox);
        pane.setCenter(activeTableView);
        return pane;
    }

    public void refresh() {
        refreshActive();
    }

    public void refreshActive() {
        List<Alert> allActive = store.getFarmSystem().getActiveAlertsSorted();
        String selectedSeverity = activeSeverityFilter.getValue();
        if ("Toutes".equals(selectedSeverity)) {
            activeAlerts.setAll(allActive);
        } else {
            activeAlerts.setAll(allActive.stream()
                .filter(a -> a.getSeverity().name().equals(selectedSeverity))
                .collect(Collectors.toList()));
        }
        activeTableView.setItems(activeAlerts);
    }

    // ──────────────────────────────────────────────
    //  History & Filters Tab
    // ──────────────────────────────────────────────

    private BorderPane buildHistoryPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16, 0, 0, 0));

        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(10);
        filterGrid.setVgap(10);
        filterGrid.setPadding(new Insets(0, 0, 16, 0));

        ChoiceBox<String> zoneFilter = new ChoiceBox<>();
        zoneFilter.getItems().add("Toutes");
        store.getZones().forEach(z -> zoneFilter.getItems().add(z.getCode()));
        zoneFilter.setValue("Toutes");

        ChoiceBox<String> typeFilter = new ChoiceBox<>();
        typeFilter.getItems().addAll("Tous", "EnvironmentalSensor", "SoilSensor", "WaterSensor", "BiometricSensor", "GpsSensor");
        typeFilter.setValue("Tous");

        ChoiceBox<String> severityFilter = new ChoiceBox<>();
        severityFilter.getItems().addAll("Toutes", "CRITICAL", "WARNING");
        severityFilter.setValue("Toutes");

        ChoiceBox<String> statusFilter = new ChoiceBox<>();
        statusFilter.getItems().addAll("Toutes", "ACTIVE", "ACKNOLEGED", "RESOLVED", "DELETED");
        statusFilter.setValue("Toutes");

        DatePicker startPicker = new DatePicker(LocalDate.now().minusDays(30));
        DatePicker endPicker = new DatePicker(LocalDate.now());

        filterGrid.add(new Label("Zone :"), 0, 0); filterGrid.add(zoneFilter, 1, 0);
        filterGrid.add(new Label("Capteur :"), 2, 0); filterGrid.add(typeFilter, 3, 0);
        filterGrid.add(new Label("Gravité :"), 4, 0); filterGrid.add(severityFilter, 5, 0);
        
        filterGrid.add(new Label("Statut :"), 0, 1); filterGrid.add(statusFilter, 1, 1);
        filterGrid.add(new Label("Du :"), 2, 1); filterGrid.add(startPicker, 3, 1);
        filterGrid.add(new Label("Au :"), 4, 1); filterGrid.add(endPicker, 5, 1);

        Button applyFilter = new Button("Filtrer");
        Button refreshHistory = new Button("Actualiser/Réinitialiser");
        HBox filterActions = new HBox(10, applyFilter, refreshHistory);
        filterGrid.add(filterActions, 6, 1);

        applyFilter.setOnAction(e -> {
            List<Alert> filtered = store.getFarmSystem().getAlertsHistory().stream()
                .filter(a -> zoneFilter.getValue().equals("Toutes") || (a.getZoneCode() != null && a.getZoneCode().equals(zoneFilter.getValue())))
                .filter(a -> typeFilter.getValue().equals("Tous") || (a.getSensorType() != null && a.getSensorType().equals(typeFilter.getValue())))
                .filter(a -> severityFilter.getValue().equals("Toutes") || a.getSeverity().name().equals(severityFilter.getValue()))
                .filter(a -> statusFilter.getValue().equals("Toutes") || a.getStatus().name().equals(statusFilter.getValue()))
                .filter(a -> {
                    if (startPicker.getValue() == null || endPicker.getValue() == null) return true;
                    String start = startPicker.getValue().atStartOfDay().toString();
                    String end = endPicker.getValue().atTime(23, 59, 59).toString();
                    return a.getTimestamp().compareTo(start) >= 0 && a.getTimestamp().compareTo(end) <= 0;
                })
                .collect(Collectors.toList());
            historyAlerts.setAll(filtered);
        });

        refreshHistory.setOnAction(e -> {
            zoneFilter.setValue("Toutes");
            typeFilter.setValue("Tous");
            severityFilter.setValue("Toutes");
            statusFilter.setValue("Toutes");
            startPicker.setValue(LocalDate.now().minusDays(30));
            endPicker.setValue(LocalDate.now());
            historyAlerts.setAll(store.getFarmSystem().getAlertsHistory());
        });

        pane.setTop(filterGrid);
        
        historyTableView.setItems(historyAlerts);
        pane.setCenter(historyTableView);
        return pane;
    }

    // ──────────────────────────────────────────────
    //  Common Table Builder
    // ──────────────────────────────────────────────

    private TableView<Alert> buildBaseTable() {
        TableView<Alert> table = new TableView<>();
        
        TableColumn<Alert, String> date = new TableColumn<>("Date & Heure");
        date.setCellValueFactory(c -> {
            LocalDateTime dt = LocalDateTime.parse(c.getValue().getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        TableColumn<Alert, String> zone = new TableColumn<>("Zone");
        zone.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));

        TableColumn<Alert, String> sensor = new TableColumn<>("Capteur");
        sensor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSensorId() + " (" + c.getValue().getSensorType() + ")"));

        TableColumn<Alert, String> message = new TableColumn<>("Message");
        message.setCellValueFactory(new PropertyValueFactory<>("message"));

        TableColumn<Alert, Object> severity = new TableColumn<>("Gravité");
        severity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severity.setCellFactory(col -> new SeverityColorCell());

        TableColumn<Alert, Object> state = new TableColumn<>("Statut");
        state.setCellValueFactory(new PropertyValueFactory<>("status"));
        state.setCellFactory(col -> new StatusColorCell());

        table.getColumns().addAll(date, zone, sensor, message, severity, state);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    // ──────────────────────────────────────────────
    //  Coloured Cells
    // ──────────────────────────────────────────────

    private static class SeverityColorCell extends TableCell<Alert, Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); return; }
            setText(item.toString());
            String color = switch (item.toString()) {
                case "CRITICAL" -> "#c62828"; // Red
                case "WARNING"  -> "#ef6c00"; // Orange
                default         -> "#37474f";
            };
            setTextFill(Color.WHITE);
            setStyle("-fx-background-color: " + color + "; -fx-padding: 4 8 4 8; -fx-background-radius: 6;");
        }
    }

    private static class StatusColorCell extends TableCell<Alert, Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); return; }
            setText(item.toString());
            String color = switch (item.toString()) {
                case "ACTIVE"       -> "#d32f2f"; 
                case "ACKNOLEGED"   -> "#2e7d32"; 
                case "RESOLVED"     -> "#1565c0"; 
                case "DELETED"      -> "#757575"; 
                default             -> "#37474f";
            };
            setTextFill(Color.WHITE);
            setStyle("-fx-background-color: " + color + "; -fx-padding: 4 8 4 8; -fx-background-radius: 6;");
        }
    }
}
