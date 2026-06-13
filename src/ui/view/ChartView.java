package ui.view;

import Readings.Reading;
import Sensors.Sensor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.FarmDataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChartView extends BorderPane {
    private final FarmDataStore store;
    private final ObservableList<Sensor> sensors = FXCollections.observableArrayList();
    private final ChoiceBox<Sensor> sensorChoiceBox = new ChoiceBox<>(sensors);
    private final DatePicker startPicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker endPicker = new DatePicker(LocalDate.now());
    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    private final Label hint = new Label("Sélectionnez un capteur puis une période.");

    public ChartView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildControls());
        setCenter(buildChart());
        refresh();
    }

    private VBox buildControls() {
        sensorChoiceBox.setPrefWidth(260);
        sensorChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sensor sensor) {
                return sensor == null ? "" : sensor.getId() + " - " + sensor.getClass().getSimpleName();
            }

            @Override
            public Sensor fromString(String string) {
                return null;
            }
        });
        Button load = new Button("Afficher");
        load.setOnAction(e -> updateChart());

        HBox controls = new HBox(10,
                new Label("Capteur"),
                sensorChoiceBox,
                new Label("Début"),
                startPicker,
                new Label("Fin"),
                endPicker,
                load);
        controls.setPadding(new Insets(0, 0, 12, 0));

        VBox box = new VBox(8, controls, hint);
        return box;
    }

    private LineChart<Number, Number> buildChart() {
        chart.setTitle("Evolution des relevés");
        chart.setCreateSymbols(false);
        return chart;
    }

    public void refresh() {
        sensors.setAll(store.getSensorsForChart());
        if (!sensors.isEmpty() && sensorChoiceBox.getValue() == null) {
            sensorChoiceBox.setValue(sensors.get(0));
        }
        updateChart();
    }

    private void updateChart() {
        chart.getData().clear();
        Sensor sensor = sensorChoiceBox.getValue();
        if (sensor == null) {
            hint.setText("Aucun capteur disponible pour le graphe.");
            return;
        }

        LocalDate startDate = startPicker.getValue();
        LocalDate endDate = endPicker.getValue();
        String start = startDate != null ? startDate.atStartOfDay().toString() : LocalDate.now().minusDays(7).atStartOfDay().toString();
        String end = endDate != null ? endDate.plusDays(1).atStartOfDay().toString() : LocalDate.now().plusDays(1).atStartOfDay().toString();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(sensor.getId());

        int index = 0;
        for (Reading reading : store.getReadingsForSensor(sensor.getId())) {
            if (reading.getTimestamp().compareTo(start) >= 0 && reading.getTimestamp().compareTo(end) <= 0) {
                Number value = reading.getNumericValue() != null ? reading.getNumericValue() : reading.getCoordinates() != null ? reading.getCoordinates().getLatitude() : null;
                if (value != null) {
                    series.getData().add(new XYChart.Data<>(index++, value));
                }
            }
        }

        chart.getData().add(series);
        hint.setText("Capteur: " + sensor.getId() + " | points: " + series.getData().size());
    }
}
