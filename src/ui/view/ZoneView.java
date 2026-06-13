package ui.view;

import Crops.Cereals;
import Crops.Crops;
import Crops.Fruits;
import Crops.Vegetables;
import Readings.Reading;
import Zone.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ui.model.FarmDataStore;

public class ZoneView extends BorderPane {
    private final FarmDataStore store;
    private final ObservableList<Zone> zones = FXCollections.observableArrayList();
    private final TableView<Zone> tableView = new TableView<>(zones);

    public ZoneView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildActions());
        setCenter(buildTable());
        refresh();
    }

    // ──────────────────────────────────────────────
    //  Toolbar
    // ──────────────────────────────────────────────

    private VBox buildActions() {
        Button add        = new Button("Ajouter");
        Button modify     = new Button("Modifier");
        Button suspend    = new Button("Suspendre");
        Button reactivate = new Button("Réactiver");
        Button report     = new Button("Rapport des cultures");
        Button feeding    = new Button("Programme d'alimentation");
        Button dashboard  = new Button("Tableau de bord (Capteurs)");
        Button chart      = new Button("Évolution (Graphique)");
        Button refresh    = new Button("Actualiser");

        add.setOnAction(e -> addZone());
        modify.setOnAction(e -> modifyZone());
        suspend.setOnAction(e -> {
            Zone selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) { store.suspendZone(selected.getCode()); refresh(); }
        });
        reactivate.setOnAction(e -> {
            Zone selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) { store.reactivateZone(selected.getCode()); refresh(); }
        });
        report.setOnAction(e -> showCropReport());
        feeding.setOnAction(e -> showFeedingProgramDialog());
        dashboard.setOnAction(e -> showZoneReadingsDashboard());
        chart.setOnAction(e -> showZoneEvolutionChart());
        refresh.setOnAction(e -> refresh());

        HBox row1 = new HBox(10, add, modify, suspend, reactivate, refresh);
        HBox row2 = new HBox(10, report, feeding, dashboard, chart);
        row2.setPadding(new Insets(4, 0, 0, 0));

        VBox box = new VBox(4, row1, row2);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    // ──────────────────────────────────────────────
    //  Feeding program dialog
    // ──────────────────────────────────────────────

    private void showFeedingProgramDialog() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Veuillez sélectionner une zone dans le tableau.").showAndWait();
            return;
        }
        if (!(selected instanceof LivestockZone) && !(selected instanceof AquacultureZone)) {
            new Alert(Alert.AlertType.WARNING,
                    "La zone \"" + selected.getCode() + "\" n'est pas une zone animale.\n"
                    + "Les programmes d'alimentation s'appliquent aux zones d'élevage et aquacoles.")
                    .showAndWait();
            return;
        }

        // Read current program
        FeedingProgram current = (selected instanceof LivestockZone lz)
                ? lz.getFeedingProgram()
                : ((AquacultureZone) selected).getFeedingProgram();

        // ── Build dialog ──
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Programme d'alimentation – " + selected.getCode());
        dialog.setHeaderText("Zone : " + selected.getCode() + " – " + selected.getName()
                + "  (" + selected.getType() + ")");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // Current program summary (read-only)
        Label currentLabel = new Label("Programme actuel :");
        currentLabel.setStyle("-fx-font-weight: bold;");
        String currentText = (current == null)
                ? "Aucun programme défini"
                : "Type d'aliment : " + current.getFeedType()
                  + "   |   Quantité / repas : " + current.getQuantityPerMeal() + " kg";
        Label currentValue = new Label(currentText);
        currentValue.setStyle(current == null
                ? "-fx-text-fill: #c62828;"
                : "-fx-text-fill: #2e7d32;");

        Separator sep = new Separator();

        // Editable fields
        Label newLabel = new Label("Définir / modifier :");
        newLabel.setStyle("-fx-font-weight: bold;");

        Label feedTypeLabel = new Label("Type d'aliment :");
        TextField feedTypeField = new TextField(current != null ? current.getFeedType() : "");
        feedTypeField.setPromptText("Ex : Foin, Granulés, Poissons…");

        Label qtyLabel = new Label("Quantité par repas (kg) :");
        TextField qtyField = new TextField(current != null
                ? String.valueOf(current.getQuantityPerMeal()) : "");
        qtyField.setPromptText("Ex : 5.0");

        grid.add(currentLabel, 0, 0); grid.add(currentValue,  1, 0);
        grid.add(sep,          0, 1, 2, 1);
        grid.add(newLabel,     0, 2, 2, 1);
        grid.add(feedTypeLabel,0, 3); grid.add(feedTypeField, 1, 3);
        grid.add(qtyLabel,     0, 4); grid.add(qtyField,      1, 4);

        ColumnConstraints col0 = new ColumnConstraints(200);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(520);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            String feedType = feedTypeField.getText().trim();
            String qtyText  = qtyField.getText().trim();

            if (feedType.isBlank() || qtyText.isBlank()) {
                new Alert(Alert.AlertType.WARNING,
                        "Veuillez remplir les deux champs.").showAndWait();
                return;
            }
            try {
                double qty = Double.parseDouble(qtyText);
                store.getFarmSystem().defineFeedingProgram(selected.getCode(), feedType, qty);
                new Alert(Alert.AlertType.INFORMATION,
                        "Programme d'alimentation enregistré :\n"
                        + "Type : " + feedType + "\n"
                        + "Quantité / repas : " + qty + " kg").showAndWait();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Quantité invalide.").showAndWait();
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Zone Readings Dashboard
    // ──────────────────────────────────────────────

    private void showZoneReadingsDashboard() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une zone.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tableau de bord – " + selected.getCode());
        dialog.setHeaderText("Relevés des capteurs – Zone " + selected.getName());

        TableView<Reading> table = new TableView<>();
        
        TableColumn<Reading, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(c.getValue().getTimestamp(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return new javafx.beans.property.SimpleStringProperty(dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        TableColumn<Reading, String> sensorCol = new TableColumn<>("Capteur (ID)");
        sensorCol.setCellValueFactory(new PropertyValueFactory<>("sensorId"));

        TableColumn<Reading, String> valCol = new TableColumn<>("Valeur");
        valCol.setCellValueFactory(c -> {
            Reading r = c.getValue();
            if (r.getCoordinates() != null) {
                return new javafx.beans.property.SimpleStringProperty(String.format("Lat: %.4f, Lon: %.4f", 
                    r.getCoordinates().getLatitude(), r.getCoordinates().getLongitude()));
            }
            return new javafx.beans.property.SimpleStringProperty(r.getNumericValue() + " " + (r.getUnit() != null ? r.getUnit() : ""));
        });

        TableColumn<Reading, Object> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        statusCol.setCellFactory(col -> new SensorView.StatusColorCell<>());

        table.getColumns().addAll(dateCol, sensorCol, valCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefWidth(550);
        table.setPrefHeight(400);

        ObservableList<Reading> readings = FXCollections.observableArrayList(store.getReadingsForZone(selected.getCode()));
        table.setItems(readings);

        VBox content = new VBox(10, table);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ──────────────────────────────────────────────
    //  Zone Evolution Chart
    // ──────────────────────────────────────────────

    private void showZoneEvolutionChart() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une zone.").showAndWait();
            return;
        }

        List<Sensors.Sensor> zoneSensors = store.getFarmSystem().getSensorsForZone(selected);
        List<Sensors.Sensor> numericSensors = zoneSensors.stream()
                .filter(s -> !(s instanceof Sensors.GpsSensor))
                .toList();

        if (numericSensors.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun capteur numérique actif dans cette zone pour générer un graphique.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Évolution – " + selected.getCode());
        dialog.setHeaderText("Évolution des capteurs : Zone " + selected.getName());

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Temps");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Valeurs mesurées");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Comparaison des capteurs");
        lineChart.setPrefSize(900, 500);

        for (Sensors.Sensor sensor : numericSensors) {
            List<Reading> readings = sensor.getHistory();
            if (readings.isEmpty()) continue;

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(sensor.getId() + " (" + getMetricLabel(sensor) + ")");

            for (Reading r : readings) {
                java.time.LocalDateTime dt = java.time.LocalDateTime.parse(r.getTimestamp(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String timeLabel = dt.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                series.getData().add(new XYChart.Data<>(timeLabel, r.getNumericValue()));
            }
            lineChart.getData().add(series);
        }

        VBox content = new VBox(lineChart);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String getMetricLabel(Sensors.Sensor s) {
        if (s instanceof Sensors.EnvironmentalSensor es) return es.getMetric().name();
        if (s instanceof Sensors.SoilSensor ss)         return ss.getMetric().name();
        if (s instanceof Sensors.WaterSensor ws)         return ws.getMetric().name();
        if (s instanceof Sensors.BiometricSensor)        return "Temp & Activity";
        return "—";
    }

    // ──────────────────────────────────────────────
    //  Crop status report dialog
    // ──────────────────────────────────────────────

    private void showCropReport() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Veuillez sélectionner une zone dans le tableau.").showAndWait();
            return;
        }
        if (!(selected instanceof CropZone cropZone)) {
            new Alert(Alert.AlertType.WARNING,
                    "La zone sélectionnée \"" + selected.getCode()
                    + "\" n'est pas une zone de culture.").showAndWait();
            return;
        }
        if (cropZone.getCrops().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Aucune culture enregistrée dans la zone " + cropZone.getCode() + ".").showAndWait();
            return;
        }

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        Label zoneLabel = new Label("Zone : " + cropZone.getCode() + " – " + cropZone.getName());
        zoneLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label countLabel = new Label("Nombre de cultures : " + cropZone.getCrops().size());
        countLabel.setStyle("-fx-text-fill: #888;");

        content.getChildren().addAll(zoneLabel, countLabel, new Separator());

        for (Crops crop : cropZone.getCrops()) {
            String family = crop.getFamily();
            String specificType = "";
            if (crop instanceof Fruits f)         specificType = f.getFruitType().name();
            else if (crop instanceof Vegetables v) specificType = v.getVegetableType().name();
            else if (crop instanceof Cereals c)    specificType = c.getCerealType().name();

            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(6);
            grid.setPadding(new Insets(12));
            grid.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 6;");

            int row = 0;
            addRow(grid, row++, "ID",                    crop.getId());
            addRow(grid, row++, "Famille",               family);
            addRow(grid, row++, "Type",                  specificType);
            addRow(grid, row++, "Stade de croissance",   crop.getGrowthStage().name());
            addRow(grid, row++, "Date de plantation",    crop.getPlantingDate().toString());
            addRow(grid, row++, "Récolte prévue",        crop.getHarvestDate().toString());
            addRow(grid, row++, "pH (min – max)",
                    crop.getMinPH() + " – " + crop.getMaxPH());
            addRow(grid, row,   "Humidité % (min – max)",
                    crop.getMinMoisture() + " – " + crop.getMaxMoisture());

            content.getChildren().addAll(grid, new Separator());
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(480);
        scroll.setPrefViewportWidth(520);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rapport des cultures – " + cropZone.getCode());
        dialog.setHeaderText("État des cultures – "
                + cropZone.getCode() + " – " + cropZone.getName());
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void addRow(GridPane grid, int row, String labelText, String valueText) {
        Label lbl = new Label(labelText + " :");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        grid.add(lbl, 0, row);
        grid.add(new Label(valueText), 1, row);
    }

    // ──────────────────────────────────────────────
    //  Add / modify zone
    // ──────────────────────────────────────────────

    private void addZone() {
        TextInputDialog typeDialog = new TextInputDialog("CROP");
        typeDialog.setHeaderText("Type de zone");
        typeDialog.setContentText("CROP, LIVESTOCK ou AQUACULTURE");
        String type = typeDialog.showAndWait().orElse(null);
        if (type == null || type.isBlank()) return;

        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setHeaderText("Code de zone");
        String code = codeDialog.showAndWait().orElse(null);
        if (code == null || code.isBlank()) return;

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setHeaderText("Nom de zone");
        String name = nameDialog.showAndWait().orElse(null);
        if (name == null || name.isBlank()) return;

        Zone zone;
        if ("LIVESTOCK".equalsIgnoreCase(type.trim())) {
            Dialog<ButtonType> boundsDialog = new Dialog<>();
            boundsDialog.setTitle("Limites de la zone d'élevage");
            boundsDialog.setHeaderText("Définissez le centre et le rayon de la zone");
            boundsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField latField = new TextField("0.0");
            TextField lonField = new TextField("0.0");
            TextField radField = new TextField("100.0");

            grid.add(new Label("Latitude :"), 0, 0); grid.add(latField, 1, 0);
            grid.add(new Label("Longitude :"), 0, 1); grid.add(lonField, 1, 1);
            grid.add(new Label("Rayon (m) :"), 0, 2); grid.add(radField, 1, 2);

            boundsDialog.getDialogPane().setContent(grid);
            if (boundsDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            LivestockZone lz = new LivestockZone(code.trim(), name.trim(), "LIVESTOCK");
            try {
                double lat = Double.parseDouble(latField.getText().trim());
                double lon = Double.parseDouble(lonField.getText().trim());
                double rad = Double.parseDouble(radField.getText().trim());
                lz.setBoundary(new Readings.Coordinates(lat, lon), rad);
            } catch (NumberFormatException ignored) {}
            zone = lz;
        } else if ("AQUACULTURE".equalsIgnoreCase(type.trim())) {
            zone = new AquacultureZone(code.trim(), name.trim(), "AQUACULTURE");
        } else {
            zone = new CropZone(code.trim(), name.trim(), "CROP");
        }

        store.addZone(zone);
        refresh();
    }

    private void modifyZone() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setHeaderText("Nouveau nom pour " + selected.getCode());
        String newName = nameDialog.showAndWait().orElse(selected.getName());
        if (newName == null || newName.isBlank()) return;

        store.getFarmSystem().editZone(selected.getCode(), newName.trim(), selected.getType());
        refresh();
    }

    // ──────────────────────────────────────────────
    //  Table
    // ──────────────────────────────────────────────

    private TableView<Zone> buildTable() {
        TableColumn<Zone, String> code = new TableColumn<>("Code");
        code.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Zone, String> name = new TableColumn<>("Nom");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Zone, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Zone, Object> status = new TableColumn<>("Statut");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Zone, Number> hosted = new TableColumn<>("Entités hébergées");
        hosted.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getHostedCount()));

        TableColumn<Zone, String> feedingCol = new TableColumn<>("Programme d'alimentation");
        feedingCol.setCellValueFactory(data -> {
            Zone z = data.getValue();
            FeedingProgram fp = null;
            if (z instanceof LivestockZone lz)   fp = lz.getFeedingProgram();
            else if (z instanceof AquacultureZone az) fp = az.getFeedingProgram();
            if (fp == null) return new javafx.beans.property.SimpleStringProperty("—");
            return new javafx.beans.property.SimpleStringProperty(
                    fp.getFeedType() + "  (" + fp.getQuantityPerMeal() + " kg/repas)");
        });

        TableColumn<Zone, String> boundsCol = new TableColumn<>("Limites (GPS)");
        boundsCol.setCellValueFactory(data -> {
            Zone z = data.getValue();
            if (z instanceof LivestockZone lz && lz.getCenter() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("Lat: %.4f, Lon: %.4f, R: %.1fm",
                                lz.getCenter().getLatitude(), lz.getCenter().getLongitude(), lz.getRadius()));
            }
            return new javafx.beans.property.SimpleStringProperty("—");
        });

        tableView.getColumns().addAll(code, name, type, status, hosted, feedingCol, boundsCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        zones.setAll(store.getZones());
    }
}
