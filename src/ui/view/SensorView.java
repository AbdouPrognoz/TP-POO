package ui.view;

import Sensors.EnvironmentalMetric;
import Sensors.EnvironmentalSensor;
import Sensors.SensorStatus;
import Sensors.Sensor;
import Zone.Zone;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import ui.model.FarmDataStore;

public class SensorView extends BorderPane {
    private final FarmDataStore store;
    private final ObservableList<Sensor> sensors = FXCollections.observableArrayList();
    private final TableView<Sensor> tableView = new TableView<>(sensors);

    public SensorView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildActions());
        setCenter(buildTable());
        refresh();
    }

    private HBox buildActions() {
        Button add = new Button("Ajouter");
        Button suspend = new Button("Suspendre");
        Button reactivate = new Button("Réactiver");
        Button faulty = new Button("Défaillant");
        Button refresh = new Button("Actualiser");
        add.setOnAction(e -> addSensor());
        suspend.setOnAction(e -> updateStatus(SensorStatus.SUSPENDED));
        reactivate.setOnAction(e -> updateStatus(SensorStatus.ACTIVE));
        faulty.setOnAction(e -> updateStatus(SensorStatus.FAULTY));
        refresh.setOnAction(e -> refresh());
        return new HBox(10, add, suspend, reactivate, faulty, refresh);
    }

    private void addSensor() {
        ChoiceDialog<String> zoneDialog = new ChoiceDialog<>();
        zoneDialog.setTitle("Ajouter capteur");
        zoneDialog.setHeaderText("Choisir une zone");
        zoneDialog.getItems().setAll(store.getZones().stream().map(Zone::getCode).toList());
        String zoneCode = zoneDialog.showAndWait().orElse(null);
        if (zoneCode == null || zoneCode.isBlank()) {
            return;
        }

        Zone zone = store.getFarmSystem().getZoneView(zoneCode);

        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setHeaderText("Code du capteur");
        String code = codeDialog.showAndWait().orElse(null);
        if (code == null || code.isBlank()) {
            return;
        }

        EnvironmentalSensor sensor = new EnvironmentalSensor(code.trim(), zone, 10.0, 30.0, "C", EnvironmentalMetric.TEMPERATURE);
        store.addSensorToFarm(sensor);
        refresh();
    }

    private void updateStatus(SensorStatus status) {
        Sensor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        store.configureSensorStatus(selected.getId(), status);
        refresh();
    }

    private TableView<Sensor> buildTable() {
        TableColumn<Sensor, String> code = new TableColumn<>("Code");
        code.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Sensor, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(cell -> Bindings.createStringBinding(() -> cell.getValue().getClass().getSimpleName()));
        TableColumn<Sensor, String> zone = new TableColumn<>("Zone");
        zone.setCellValueFactory(cell -> Bindings.createStringBinding(() -> cell.getValue().getZone() != null ? cell.getValue().getZone().getCode() : cell.getValue().getOwnerAnimal() != null ? cell.getValue().getOwnerAnimal().getId() : "-"));
        TableColumn<Sensor, Object> status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        status.setCellFactory(column -> new StatusColorCell());
        TableColumn<Sensor, Number> min = new TableColumn<>("Min");
        min.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));
        TableColumn<Sensor, Number> max = new TableColumn<>("Max");
        max.setCellValueFactory(new PropertyValueFactory<>("maxThreshold"));
        TableColumn<Sensor, String> latest = new TableColumn<>("Dernière lecture");
        latest.setCellValueFactory(cell -> Bindings.createStringBinding(() -> store.formatSensorLatestReading(cell.getValue())));

        tableView.getColumns().addAll(code, type, zone, status, min, max, latest);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        sensors.setAll(store.getAllSensors());
    }

    private static class StatusColorCell extends TableCell<Sensor, Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("");
                return;
            }
            setText(item.toString());
            String color = switch (item.toString()) {
                case "ACTIVE" -> "#2e7d32";
                case "SUSPENDED" -> "#ef6c00";
                case "FAULTY" -> "#c62828";
                default -> "#37474f";
            };
            setTextFill(Color.WHITE);
            setStyle("-fx-background-color: " + color + "; -fx-padding: 4 8 4 8; -fx-background-radius: 6;");
        }
    }
}
