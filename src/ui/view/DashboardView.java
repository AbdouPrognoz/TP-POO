package ui.view;

import Alerts.Alert;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import ui.model.FarmDataStore;

public class DashboardView extends BorderPane {
    private final FarmDataStore store;
    private final Label activeZonesValue = new Label();
    private final Label suspendedZonesValue = new Label();
    private final Label totalCropsValue = new Label();
    private final Label totalAnimalsValue = new Label();
    private final Label totalSensorsValue = new Label();
    private final Label criticalAlertsValue = new Label();
    private final Label warningAlertsValue = new Label();
    private final ObservableList<Alert> recentAlerts = FXCollections.observableArrayList();

    public DashboardView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildHeader());
        setCenter(buildContent());
        refresh();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private VBox buildHeader() {
        Label title = new Label("Smart Farming Dashboard");
        title.getStyleClass().add("page-title");
        VBox box = new VBox(title, new Label("Vue globale de l'exploitation"));
        box.setSpacing(6);
        return box;
    }

    private GridPane buildContent() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(16, 0, 16, 0));

        grid.add(card("Zones actives", activeZonesValue), 0, 0);
        grid.add(card("Zones suspendues", suspendedZonesValue), 1, 0);
        grid.add(card("Total cultures", totalCropsValue), 2, 0);
        grid.add(card("Total animaux", totalAnimalsValue), 0, 1);
        grid.add(card("Total capteurs", totalSensorsValue), 1, 1);
        grid.add(card("Alertes critiques", criticalAlertsValue), 2, 1);
        grid.add(card("Alertes avertissement", warningAlertsValue), 0, 2);

        ListView<Alert> alertListView = new ListView<>(recentAlerts);
        alertListView.setPrefHeight(240);
        alertListView.setCellFactory(list -> new AlertCardCell());

        VBox recentBox = new VBox(10, new Label("Alertes récentes"), alertListView);
        recentBox.getStyleClass().add("surface");
        recentBox.setPadding(new Insets(16));
        recentBox.setSpacing(12);
        grid.add(recentBox, 1, 2, 2, 1);

        return grid;
    }

    private VBox card(String title, Label value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        value.getStyleClass().add("card-value");
        VBox box = new VBox(8, titleLabel, value);
        box.getStyleClass().add("metric-card");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        return box;
    }

    public void refresh() {
        activeZonesValue.setText(String.valueOf(store.countActiveZones()));
        suspendedZonesValue.setText(String.valueOf(store.countSuspendedZones()));
        totalCropsValue.setText(String.valueOf(store.getAllCrops().size()));
        totalAnimalsValue.setText(String.valueOf(store.getAllAnimals().size()));
        totalSensorsValue.setText(String.valueOf(store.countTotalSensors()));
        criticalAlertsValue.setText(String.valueOf(store.countCriticalAlerts()));
        warningAlertsValue.setText(String.valueOf(store.countWarningAlerts()));
        recentAlerts.setAll(store.getRecentAlerts(5));
    }
}
