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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import java.util.Optional;
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
        Button production = new Button("Historique de production");
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
        production.setOnAction(e -> showProductionHistoryDialog());
        dashboard.setOnAction(e -> showZoneReadingsDashboard());
        chart.setOnAction(e -> showZoneEvolutionChart());
        refresh.setOnAction(e -> refresh());

        HBox row1 = new HBox(10, add, modify, suspend, reactivate, refresh);
        HBox row2 = new HBox(10, report, feeding, production, dashboard, chart);
        row2.setPadding(new Insets(4, 0, 0, 0));

        VBox box = new VBox(4, row1, row2);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    // ──────────────────────────────────────────────
    //  Feeding program dialog
    // ──────────────────────────────────────────────

    private void showProductionHistoryDialog() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une zone.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Historique de production – " + selected.getCode());
        dialog.setHeaderText("Production pour : " + selected.getName() + " (" + selected.getType() + ")");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        TableView<ProductionRecord> historyTable = new TableView<>();
        ObservableList<ProductionRecord> data = FXCollections.observableArrayList(selected.getProductionHistory());
        historyTable.setItems(data);

        TableColumn<ProductionRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);
        
        TableColumn<ProductionRecord, String> detailsCol = new TableColumn<>("Détails");
        detailsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDetails()));
        detailsCol.setPrefWidth(450);

        historyTable.getColumns().addAll(dateCol, detailsCol);
        historyTable.setPrefHeight(200);

        Separator sep = new Separator();

        Label newLabel = new Label("Enregistrer une nouvelle production :");
        newLabel.setStyle("-fx-font-weight: bold;");

        HBox formBox = new HBox(10);
        formBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        datePicker.setPrefWidth(120);

        TextField idField = new TextField();
        idField.setPromptText(selected instanceof LivestockZone ? "ID Animal" : (selected instanceof AquacultureZone ? "Espèce" : "ID Culture"));
        idField.setPrefWidth(100);

        TextField qtyField = new TextField();
        qtyField.setPromptText(selected instanceof CropZone ? "Rendement" : "Poids (kg)");
        qtyField.setPrefWidth(100);

        Button addBtn = new Button("Enregistrer");
        addBtn.setOnAction(e -> {
            if (datePicker.getValue() == null || idField.getText().isBlank() || qtyField.getText().isBlank()) return;
            try {
                double qty = Double.parseDouble(qtyField.getText().trim());
                String id = idField.getText().trim();
                java.time.LocalDate d = datePicker.getValue();
                
                ProductionRecord record = null;
                if (selected instanceof CropZone) {
                    record = new CropProductionRecord(d, id, qty);
                } else if (selected instanceof LivestockZone) {
                    record = new LivestockProductionRecord(d, id, qty);
                } else if (selected instanceof AquacultureZone) {
                    record = new AquaProductionRecord(d, id, qty);
                }
                
                if (record != null) {
                    store.getFarmSystem().recordProduction(selected.getCode(), record);
                    data.setAll(selected.getProductionHistory());
                    idField.clear();
                    qtyField.clear();
                    refresh();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Quantité invalide.").showAndWait();
            }
        });

        formBox.getChildren().addAll(datePicker, idField, qtyField, addBtn);

        VBox content = new VBox(15, historyTable, sep, newLabel, formBox);
        content.setPadding(new Insets(10));
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.showAndWait();
    }

    // ──────────────────────────────────────────────
    //  Feeding program dialog
    // ──────────────────────────────────────────────

    private void showFeedingProgramDialog() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!(selected instanceof LivestockZone) && !(selected instanceof AquacultureZone)) {
            new Alert(Alert.AlertType.INFORMATION, "Cette zone n'accepte pas de programme d'alimentation.").showAndWait();
            return;
        }

        List<FeedingProgram> history;
        if (selected instanceof LivestockZone lz) {
            history = lz.getFeedingProgramHistory();
        } else {
            history = ((AquacultureZone) selected).getFeedingProgramHistory();
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Programme d'alimentation – " + selected.getCode());
        dialog.setHeaderText("Historique des programmes pour : " + selected.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        // TableView for History
        TableView<FeedingProgram> historyTable = new TableView<>();
        ObservableList<FeedingProgram> data = FXCollections.observableArrayList(history);
        historyTable.setItems(data);

        TableColumn<FeedingProgram, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateDefined"));
        dateCol.setPrefWidth(120);

        TableColumn<FeedingProgram, String> typeCol = new TableColumn<>("Type d'aliment");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("feedType"));
        typeCol.setPrefWidth(200);

        TableColumn<FeedingProgram, Double> qtyCol = new TableColumn<>("Quantité (kg)");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityPerMeal"));
        qtyCol.setPrefWidth(120);

        historyTable.getColumns().addAll(dateCol, typeCol, qtyCol);
        historyTable.setPrefHeight(200);

        Separator sep = new Separator();

        // Form to add a new program
        Label newLabel = new Label("Nouveau programme :");
        newLabel.setStyle("-fx-font-weight: bold;");

        HBox formBox = new HBox(10);
        formBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField feedTypeField = new TextField();
        feedTypeField.setPromptText("Type d'aliment");
        
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantité (kg)");
        qtyField.setPrefWidth(100);

        Button addBtn = new Button("Ajouter");
        addBtn.setOnAction(e -> {
            String feedType = feedTypeField.getText().trim();
            String qtyText = qtyField.getText().trim();
            if (feedType.isBlank() || qtyText.isBlank()) return;
            try {
                double qty = Double.parseDouble(qtyText);
                store.getFarmSystem().defineFeedingProgram(selected.getCode(), feedType, qty);
                
                // update table
                if (selected instanceof LivestockZone lz) {
                    data.setAll(lz.getFeedingProgramHistory());
                } else {
                    data.setAll(((AquacultureZone) selected).getFeedingProgramHistory());
                }
                
                feedTypeField.clear();
                qtyField.clear();
                refresh();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Quantité invalide.").showAndWait();
            }
        });

        formBox.getChildren().addAll(new Label("Type:"), feedTypeField, new Label("Qté:"), qtyField, addBtn);

        VBox content = new VBox(15, historyTable, sep, newLabel, formBox);
        content.setPadding(new Insets(10));
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.showAndWait();
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

        if (selected instanceof LivestockZone) {
            LivestockZone lz = (LivestockZone) selected;
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier la zone");
            dialog.setHeaderText("Modification de la zone d'élevage : " + selected.getCode());

            TextField nameField = new TextField(lz.getName());
            TextField latField = new TextField(lz.getCenter() != null ? String.valueOf(lz.getCenter().getLatitude()) : "0.0");
            TextField lonField = new TextField(lz.getCenter() != null ? String.valueOf(lz.getCenter().getLongitude()) : "0.0");
            TextField radField = new TextField(String.valueOf(lz.getRadius()));

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Nom :"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("GPS Latitude :"), 0, 1);
            grid.add(latField, 1, 1);
            grid.add(new Label("GPS Longitude :"), 0, 2);
            grid.add(lonField, 1, 2);
            grid.add(new Label("Rayon (m) :"), 0, 3);
            grid.add(radField, 1, 3);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                store.getFarmSystem().editZone(selected.getCode(), nameField.getText().trim(), selected.getType());
                try {
                    double lat = Double.parseDouble(latField.getText().trim());
                    double lon = Double.parseDouble(lonField.getText().trim());
                    double rad = Double.parseDouble(radField.getText().trim());
                    lz.setBoundary(new Readings.Coordinates(lat, lon), rad);
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Coordonnées GPS invalides.").showAndWait();
                }
                refresh();
            }
        } else {
            TextInputDialog nameDialog = new TextInputDialog(selected.getName());
            nameDialog.setHeaderText("Nouveau nom pour " + selected.getCode());
            String newName = nameDialog.showAndWait().orElse(selected.getName());
            if (newName == null || newName.isBlank()) return;

            store.getFarmSystem().editZone(selected.getCode(), newName.trim(), selected.getType());
            refresh();
        }
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
