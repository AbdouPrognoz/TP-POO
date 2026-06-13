package ui.view;

import Alerts.Alert;
import Alerts.AlertStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ui.model.FarmDataStore;

public class AlertView extends BorderPane {
    private final FarmDataStore store;
    private final ObservableList<Alert> alerts = FXCollections.observableArrayList();
    private final TableView<Alert> tableView = new TableView<>(alerts);

    public AlertView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildActions());
        setCenter(buildTable());
        refresh();
    }

    private HBox buildActions() {
        Button acknowledge = new Button("Acquitter");
        Button delete = new Button("Supprimer");
        Button refresh = new Button("Actualiser");

        acknowledge.setOnAction(e -> {
            Alert selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                store.acknowledgeAlert(selected.getId());
                refresh();
            }
        });
        delete.setOnAction(e -> {
            Alert selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                store.deleteAlert(selected.getId());
                refresh();
            }
        });
        refresh.setOnAction(e -> refresh());

        return new HBox(10, acknowledge, delete, refresh);
    }

    private TableView<Alert> buildTable() {
        TableColumn<Alert, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        TableColumn<Alert, String> zone = new TableColumn<>("Zone");
        zone.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        TableColumn<Alert, String> sensor = new TableColumn<>("Sensor");
        sensor.setCellValueFactory(new PropertyValueFactory<>("sensorId"));
        TableColumn<Alert, String> message = new TableColumn<>("Message");
        message.setCellValueFactory(new PropertyValueFactory<>("message"));
        TableColumn<Alert, Object> severity = new TableColumn<>("Severity");
        severity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        TableColumn<Alert, Object> state = new TableColumn<>("State");
        state.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.getColumns().addAll(date, zone, sensor, message, severity, state);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        alerts.setAll(store.getAlerts());
    }
}
