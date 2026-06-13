package ui.view;

import Animals.Animal;
import Readings.Coordinates;
import Readings.Reading;
import Sensors.*;
import Zone.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import ui.model.FarmDataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // ──────────────────────────────────────────────
    //  Toolbar
    // ──────────────────────────────────────────────

    private VBox buildActions() {
        Button add         = new Button("Ajouter un capteur");
        Button thresholds  = new Button("Modifier seuils (min/max)");
        Button suspend     = new Button("Suspendre");
        Button reactivate  = new Button("Réactiver");
        Button faulty      = new Button("Défaillant");
        Button refresh     = new Button("Actualiser");

        Button simulate    = new Button("Simuler un relevé");
        Button history     = new Button("Historique");
        Button chart       = new Button("Graphique d'évolution");

        add.setOnAction(e -> showAddSensorDialog());
        thresholds.setOnAction(e -> showThresholdDialog());
        suspend.setOnAction(e -> updateStatus(SensorStatus.SUSPENDED));
        reactivate.setOnAction(e -> updateStatus(SensorStatus.ACTIVE));
        faulty.setOnAction(e -> updateStatus(SensorStatus.FAULTY));
        refresh.setOnAction(e -> refresh());
        
        simulate.setOnAction(e -> showSimulateReadingDialog());
        history.setOnAction(e -> showSensorHistoryDialog());
        chart.setOnAction(e -> showSensorEvolutionChart());

        HBox row1 = new HBox(10, add, thresholds, suspend, reactivate, faulty, refresh);
        HBox row2 = new HBox(10, simulate, history, chart);
        row2.setPadding(new Insets(4, 0, 0, 0));

        VBox box = new VBox(4, row1, row2);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    // ──────────────────────────────────────────────
    //  Add sensor — type-aware dialog
    // ──────────────────────────────────────────────

    private void showAddSensorDialog() {
        // Step 1: choose sensor type
        List<String> sensorTypes = List.of(
                "Environmental (CropZone)",
                "Soil (CropZone)",
                "Water (AquacultureZone)",
                "Biometric (LivestockZone → Animal)",
                "GPS (LivestockZone → Animal)"
        );
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>(sensorTypes.get(0), sensorTypes);
        typeDialog.setTitle("Ajouter un capteur");
        typeDialog.setHeaderText("Choisissez le type de capteur");
        typeDialog.setContentText("Type :");
        Optional<String> typeOpt = typeDialog.showAndWait();
        if (typeOpt.isEmpty()) return;
        String type = typeOpt.get();

        // Step 2: choose zone (filtered by type)
        List<Zone> compatibleZones = getCompatibleZones(type);
        if (compatibleZones.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Aucune zone compatible avec ce type de capteur.").showAndWait();
            return;
        }

        ChoiceDialog<Zone> zoneDialog = new ChoiceDialog<>(compatibleZones.get(0), compatibleZones);
        zoneDialog.setTitle("Zone du capteur");
        zoneDialog.setHeaderText("Sélectionnez la zone");
        zoneDialog.setContentText("Zone :");
        Optional<Zone> zoneOpt = zoneDialog.showAndWait();
        if (zoneOpt.isEmpty()) return;
        Zone zone = zoneOpt.get();

        // Step 3: for animal sensors (GPS / Biometric) pick the animal
        Animal targetAnimal = null;
        if (type.startsWith("GPS") || type.startsWith("Biometric")) {
            List<Animal> animals = getAnimalsInZone(zone);
            if (animals.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Aucun animal dans cette zone.").showAndWait();
                return;
            }
            List<String> animalLabels = animals.stream()
                    .map(a -> a.getId() + " (" + a.getSpecies() + ")")
                    .toList();
            ChoiceDialog<String> animalDialog =
                    new ChoiceDialog<>(animalLabels.get(0), animalLabels);
            animalDialog.setTitle("Animal cible");
            animalDialog.setHeaderText("Choisissez l'animal à équiper");
            animalDialog.setContentText("Code Animal :");
            Optional<String> aOpt = animalDialog.showAndWait();
            if (aOpt.isEmpty()) return;
            
            String selectedLabel = aOpt.get();
            targetAnimal = animals.stream()
                    .filter(a -> selectedLabel.startsWith(a.getId() + " "))
                    .findFirst().orElse(animals.get(0));
        }

        // Step 4: form fields
        Dialog<ButtonType> form = new Dialog<>();
        form.setTitle("Nouveau capteur – " + type);
        form.setHeaderText("Zone : " + zone.getCode() + " – " + zone.getName());
        form.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField idField  = new TextField("S" + System.currentTimeMillis() % 10000);
        TextField minField = new TextField();
        TextField maxField = new TextField();
        TextField unitField = new TextField();
        Control metricControl = buildMetricControl(type, unitField);

        boolean isGps = type.startsWith("GPS");

        switch (type) {
            case "Environmental (CropZone)"           -> { minField.setText("10.0"); maxField.setText("30.0"); }
            case "Soil (CropZone)"                    -> { minField.setText("6.0");  maxField.setText("7.5");  }
            case "Water (AquacultureZone)"            -> { minField.setText("5.0");  maxField.setText("9.0");  }
            case "Biometric (LivestockZone → Animal)" -> { minField.setText("35.0"); maxField.setText("40.0"); }
            default                                   -> { minField.setText("0.0");  maxField.setText("0.0");  }
        }

        int row = 0;
        grid.add(new Label("ID :"), 0, row); grid.add(idField, 1, row++);
        if (!isGps) {
            grid.add(new Label("Seuil min :"), 0, row); grid.add(minField, 1, row++);
            grid.add(new Label("Seuil max :"), 0, row); grid.add(maxField, 1, row++);
            grid.add(new Label("Unité :"), 0, row); grid.add(unitField, 1, row++);
            if (metricControl != null) {
                grid.add(new Label("Métrique :"), 0, row); grid.add(metricControl, 1, row);
            }
        }

        ColumnConstraints col0 = new ColumnConstraints(140);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        form.getDialogPane().setContent(grid);
        
        Optional<ButtonType> formResult = form.showAndWait();
        if (formResult.isEmpty() || formResult.get() != ButtonType.OK) return;

        try {
            double minD = isGps ? 0.0 : Double.parseDouble(minField.getText().trim());
            double maxD = isGps ? 0.0 : Double.parseDouble(maxField.getText().trim());
            String unit = unitField.getText().trim();

            Sensor sensor = switch (type) {
                case "Environmental (CropZone)" -> {
                    EnvironmentalMetric metric = (EnvironmentalMetric) ((ChoiceBox<?>) metricControl).getValue();
                    yield new EnvironmentalSensor(idField.getText().trim(), zone, minD, maxD, unit.isBlank() ? "°C" : unit, metric);
                }
                case "Soil (CropZone)" -> {
                    SoilMetric metric = (SoilMetric) ((ChoiceBox<?>) metricControl).getValue();
                    yield new SoilSensor(idField.getText().trim(), zone, minD, maxD, unit.isBlank() ? "%" : unit, metric);
                }
                case "Water (AquacultureZone)" -> {
                    WaterMetric metric = (WaterMetric) ((ChoiceBox<?>) metricControl).getValue();
                    yield new WaterSensor(idField.getText().trim(), zone, minD, maxD, unit.isBlank() ? "°C" : unit, metric);
                }
                case "Biometric (LivestockZone → Animal)" ->
                    new BiometricSensor(idField.getText().trim(), zone, minD, maxD, unit.isBlank() ? "°C" : unit);
                case "GPS (LivestockZone → Animal)" ->
                    new GpsSensor(idField.getText().trim(), zone);
                default -> null;
            };

            if (sensor == null) return;

            if (targetAnimal != null) {
                targetAnimal.addSensor(sensor);
            } else {
                store.addSensorToFarm(sensor);
            }
            refresh();

        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Valeurs min/max invalides.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
        }
    }

    private Control buildMetricControl(String type, TextField unitField) {
        return switch (type) {
            case "Environmental (CropZone)" -> {
                ChoiceBox<EnvironmentalMetric> cb = new ChoiceBox<>(
                        FXCollections.observableArrayList(EnvironmentalMetric.values()));
                cb.setValue(EnvironmentalMetric.TEMPERATURE);
                unitField.setText("°C");
                cb.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == EnvironmentalMetric.TEMPERATURE) unitField.setText("°C");
                    else if (newVal == EnvironmentalMetric.HUMIDITY) unitField.setText("%");
                    else if (newVal == EnvironmentalMetric.PLUVIOMETRY) unitField.setText("mm");
                });
                yield cb;
            }
            case "Soil (CropZone)" -> {
                ChoiceBox<SoilMetric> cb = new ChoiceBox<>(
                        FXCollections.observableArrayList(SoilMetric.values()));
                cb.setValue(SoilMetric.PH);
                unitField.setText("");
                cb.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == SoilMetric.PH) unitField.setText("");
                    else if (newVal == SoilMetric.HUMIDITY) unitField.setText("%");
                    else if (newVal == SoilMetric.NITROGEN) unitField.setText("mg/kg");
                });
                yield cb;
            }
            case "Water (AquacultureZone)" -> {
                ChoiceBox<WaterMetric> cb = new ChoiceBox<>(
                        FXCollections.observableArrayList(WaterMetric.values()));
                cb.setValue(WaterMetric.TEMPERATURE);
                unitField.setText("°C");
                cb.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == WaterMetric.TEMPERATURE) unitField.setText("°C");
                    else if (newVal == WaterMetric.DISSOLVED_OXYGEN) unitField.setText("mg/L");
                    else if (newVal == WaterMetric.PH) unitField.setText("");
                });
                yield cb;
            }
            default -> null; 
        };
    }

    private List<Zone> getCompatibleZones(String type) {
        List<Zone> all = store.getZones();
        return switch (type) {
            case "Environmental (CropZone)", "Soil (CropZone)" ->
                    all.stream().filter(z -> z instanceof CropZone).toList();
            case "Water (AquacultureZone)" ->
                    all.stream().filter(z -> z instanceof AquacultureZone).toList();
            case "Biometric (LivestockZone → Animal)", "GPS (LivestockZone → Animal)" ->
                    all.stream().filter(z -> z instanceof LivestockZone).toList();
            default -> all;
        };
    }

    private List<Animal> getAnimalsInZone(Zone zone) {
        List<Animal> result = new ArrayList<>();
        if (zone instanceof LivestockZone lz) result.addAll(lz.getAnimals());
        if (zone instanceof AquacultureZone az) result.addAll(az.getSpecies());
        return result;
    }

    // ──────────────────────────────────────────────
    //  Simulate Reading
    // ──────────────────────────────────────────────

    private void showSimulateReadingDialog() {
        Sensor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un capteur.").showAndWait();
            return;
        }

        if (selected.getStatus() != SensorStatus.ACTIVE) {
            new Alert(Alert.AlertType.WARNING, "Le capteur doit être actif pour enregistrer un relevé.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Simuler un relevé – " + selected.getId());
        dialog.setHeaderText("Capteur : " + selected.getId() + " (" + getMetricLabel(selected) + ")");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        boolean isGps = selected instanceof GpsSensor;
        TextField valField = new TextField();
        TextField latField = new TextField();
        TextField lonField = new TextField();

        if (isGps) {
            grid.add(new Label("Latitude :"), 0, 0); grid.add(latField, 1, 0);
            grid.add(new Label("Longitude :"), 0, 1); grid.add(lonField, 1, 1);
            if (selected.getZone() instanceof LivestockZone lz && lz.getCenter() != null) {
                latField.setText(String.valueOf(lz.getCenter().getLatitude()));
                lonField.setText(String.valueOf(lz.getCenter().getLongitude()));
            }
        } else {
            grid.add(new Label("Valeur relevée :"), 0, 0); grid.add(valField, 1, 0);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                if (isGps) {
                    double lat = Double.parseDouble(latField.getText().trim());
                    double lon = Double.parseDouble(lonField.getText().trim());
                    store.addManualGpsReading(selected.getId(), lat, lon);
                } else {
                    double val = Double.parseDouble(valField.getText().trim());
                    store.addManualNumericReading(selected.getId(), val);
                }
                refresh();
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Valeurs invalides.").showAndWait();
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Sensor History
    // ──────────────────────────────────────────────

    private void showSensorHistoryDialog() {
        Sensor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un capteur.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Historique – " + selected.getId());
        dialog.setHeaderText("Relevés pour " + selected.getId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Filters
        HBox filterBox = new HBox(10);
        DatePicker startPicker = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker endPicker = new DatePicker(LocalDate.now());
        Button filterBtn = new Button("Filtrer");
        filterBox.getChildren().addAll(new Label("Du:"), startPicker, new Label("Au:"), endPicker, filterBtn);

        // Table
        TableView<Reading> historyTable = new TableView<>();
        TableColumn<Reading, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> {
            LocalDateTime dt = LocalDateTime.parse(c.getValue().getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        TableColumn<Reading, String> valCol = new TableColumn<>("Valeur");
        valCol.setCellValueFactory(c -> {
            Reading r = c.getValue();
            if (r.getCoordinates() != null) {
                return new SimpleStringProperty(String.format("Lat: %.4f, Lon: %.4f", 
                    r.getCoordinates().getLatitude(), r.getCoordinates().getLongitude()));
            }
            return new SimpleStringProperty(r.getNumericValue() + " " + (r.getUnit() != null ? r.getUnit() : ""));
        });

        TableColumn<Reading, Object> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        statusCol.setCellFactory(col -> new StatusColorCell());

        historyTable.getColumns().addAll(dateCol, valCol, statusCol);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        historyTable.setPrefHeight(300);
        historyTable.setPrefWidth(450);

        ObservableList<Reading> readings = FXCollections.observableArrayList(store.getReadingsForSensor(selected.getId()));
        historyTable.setItems(readings);

        filterBtn.setOnAction(e -> {
            if (startPicker.getValue() == null || endPicker.getValue() == null) return;
            String start = startPicker.getValue().atStartOfDay().toString();
            String end = endPicker.getValue().atTime(23, 59, 59).toString();
            readings.setAll(store.getReadingsForSensorInPeriod(selected.getId(), start, end));
        });

        content.getChildren().addAll(filterBox, historyTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ──────────────────────────────────────────────
    //  Sensor Evolution Chart
    // ──────────────────────────────────────────────

    private void showSensorEvolutionChart() {
        Sensor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un capteur.").showAndWait();
            return;
        }

        if (selected instanceof Sensors.GpsSensor gs) {
            showGpsGrid(gs);
            return;
        }

        List<Reading> readings = store.getReadingsForSensor(selected.getId());
        if (readings.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune donnée disponible pour ce capteur.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Évolution – " + selected.getId());
        dialog.setHeaderText("Évolution des relevés : " + getMetricLabel(selected));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Temps");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(getMetricLabel(selected));

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Historique (" + selected.getId() + ")");
        lineChart.setPrefSize(800, 400);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(selected.getId());

        for (Reading r : readings) {
            LocalDateTime dt = LocalDateTime.parse(r.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String timeLabel = dt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            series.getData().add(new XYChart.Data<>(timeLabel, r.getNumericValue()));
        }

        lineChart.getData().add(series);

        VBox content = new VBox(lineChart);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showGpsGrid(Sensors.GpsSensor selected) {
        List<Reading> readings = store.getReadingsForSensor(selected.getId());
        if (readings.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune donnée disponible pour ce capteur GPS.").showAndWait();
            return;
        }

        Zone zone = selected.getZone();
        double cx = 0, cy = 0, radius = 10;
        if (zone instanceof LivestockZone lz) {
            if (lz.getCenter() != null) {
                cx = lz.getCenter().getLatitude();
                cy = lz.getCenter().getLongitude();
                radius = lz.getRadius();
            }
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Grille GPS – " + selected.getId());
        dialog.setHeaderText("Positions de l'animal et limite de zone");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Longitude (X)");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(cx - radius - 5);
        xAxis.setUpperBound(cx + radius + 5);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Latitude (Y)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(cy - radius - 5);
        yAxis.setUpperBound(cy + radius + 5);

        javafx.scene.chart.ScatterChart<Number, Number> chart = new javafx.scene.chart.ScatterChart<>(xAxis, yAxis);
        chart.setTitle("Carte 2D");
        chart.setPrefSize(700, 600);

        XYChart.Series<Number, Number> boundarySeries = new XYChart.Series<>();
        boundarySeries.setName("Limite de Zone (Rayon=" + radius + ")");
        for (int i = 0; i <= 360; i += 2) { 
            double rad = Math.toRadians(i);
            double bx = cx + (radius * Math.cos(rad));
            double by = cy + (radius * Math.sin(rad));
            boundarySeries.getData().add(new XYChart.Data<>(bx, by));
        }

        XYChart.Series<Number, Number> pathSeries = new XYChart.Series<>();
        pathSeries.setName("Positions enregistrées");
        for (Reading r : readings) {
            if (r.getCoordinates() != null) {
                pathSeries.getData().add(new XYChart.Data<>(r.getCoordinates().getLongitude(), r.getCoordinates().getLatitude()));
            }
        }

        chart.getData().addAll(boundarySeries, pathSeries);

        VBox content = new VBox(chart);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ──────────────────────────────────────────────
    //  Threshold editor
    // ──────────────────────────────────────────────

    private void showThresholdDialog() {
        Sensor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Veuillez sélectionner un capteur dans le tableau.").showAndWait();
            return;
        }
        if (selected instanceof GpsSensor) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Les capteurs GPS n'ont pas de seuils numériques.").showAndWait();
            return;
        }

        Dialog<ButtonType> form = new Dialog<>();
        form.setTitle("Modifier les seuils – " + selected.getId());
        form.setHeaderText("Capteur : " + selected.getId()
                + "  (" + selected.getClass().getSimpleName() + ")");
        form.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField minField = new TextField(String.valueOf(selected.getMinThreshold()));
        TextField maxField = new TextField(String.valueOf(selected.getMaxThreshold()));

        grid.add(new Label("Seuil min :"), 0, 0); grid.add(minField, 1, 0);
        grid.add(new Label("Seuil max :"), 0, 1); grid.add(maxField, 1, 1);

        ColumnConstraints col0 = new ColumnConstraints(130);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        form.getDialogPane().setContent(grid);
        form.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                double min = Double.parseDouble(minField.getText().trim());
                double max = Double.parseDouble(maxField.getText().trim());
                if (min >= max) {
                    new Alert(Alert.AlertType.ERROR,
                            "Le seuil min doit être inférieur au seuil max.").showAndWait();
                    return;
                }
                store.updateSensorThresholds(selected.getId(), min, max);
                refresh();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Valeurs invalides.").showAndWait();
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Status update
    // ──────────────────────────────────────────────

    private void updateStatus(SensorStatus status) {
        Sensor selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        store.configureSensorStatus(selected.getId(), status);
        refresh();
    }

    // ──────────────────────────────────────────────
    //  Table
    // ──────────────────────────────────────────────

    private TableView<Sensor> buildTable() {
        TableColumn<Sensor, String> code = new TableColumn<>("Code");
        code.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Sensor, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> cell.getValue().getClass().getSimpleName()));

        TableColumn<Sensor, String> metric = new TableColumn<>("Métrique");
        metric.setCellValueFactory(cell -> new SimpleStringProperty(getMetricLabel(cell.getValue())));

        TableColumn<Sensor, String> zone = new TableColumn<>("Zone");
        zone.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getZone() != null
                        ? cell.getValue().getZone().getCode() + " – " + cell.getValue().getZone().getName()
                        : "—"));

        TableColumn<Sensor, String> animal = new TableColumn<>("Animal");
        animal.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getOwnerAnimal() != null
                        ? cell.getValue().getOwnerAnimal().getId()
                          + " (" + cell.getValue().getOwnerAnimal().getSpecies() + ")"
                        : "—"));

        TableColumn<Sensor, Object> status = new TableColumn<>("Statut");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        status.setCellFactory(col -> new StatusColorCell());

        TableColumn<Sensor, String> min = new TableColumn<>("Min");
        min.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue() instanceof GpsSensor ? "—"
                        : String.valueOf(cell.getValue().getMinThreshold())));

        TableColumn<Sensor, String> max = new TableColumn<>("Max");
        max.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue() instanceof GpsSensor ? "—"
                        : String.valueOf(cell.getValue().getMaxThreshold())));

        TableColumn<Sensor, String> latest = new TableColumn<>("Dernière lecture");
        latest.setCellValueFactory(cell ->
                Bindings.createStringBinding(() ->
                        store.formatSensorLatestReading(cell.getValue())));

        tableView.getColumns().addAll(code, type, metric, zone, animal, status, min, max, latest);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    private String getMetricLabel(Sensor s) {
        if (s instanceof EnvironmentalSensor es) return es.getMetric().name();
        if (s instanceof SoilSensor ss)         return ss.getMetric().name();
        if (s instanceof WaterSensor ws)         return ws.getMetric().name();
        if (s instanceof BiometricSensor bs)     return "Temp & Activity";
        if (s instanceof GpsSensor)              return "GPS";
        return "—";
    }

    public void refresh() {
        sensors.setAll(store.getAllSensors());
    }

    // ──────────────────────────────────────────────
    //  Coloured status cell
    // ──────────────────────────────────────────────

    public static class StatusColorCell<T> extends TableCell<T, Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); return; }
            setText(item.toString());
            String color = switch (item.toString()) {
                case "ACTIVE", "NORMAL"  -> "#2e7d32"; // Green
                case "SUSPENDED", "WARNING" -> "#ef6c00"; // Orange
                case "FAULTY", "CRITICAL"   -> "#c62828"; // Red
                default                  -> "#37474f";
            };
            setTextFill(Color.WHITE);
            setStyle("-fx-background-color: " + color
                    + "; -fx-padding: 4 8 4 8; -fx-background-radius: 6;");
        }
    }
}
