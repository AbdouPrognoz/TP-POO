package ui.view;

import Animals.Animal;
import Zone.AquacultureZone;
import Zone.LivestockZone;
import Zone.Zone;
import javafx.scene.control.TextInputDialog;
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

public class AnimalView extends BorderPane {
    private final FarmDataStore store;
    private final ObservableList<Animal> animals = FXCollections.observableArrayList();
    private final TableView<Animal> tableView = new TableView<>(animals);

    public AnimalView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildActions());
        setCenter(buildTable());
        refresh();
    }

    private HBox buildActions() {
        Button updateWeight = new Button("Mettre à jour le poids");
        Button refresh = new Button("Actualiser");
        updateWeight.setOnAction(e -> updateWeight());
        refresh.setOnAction(e -> refresh());
        return new HBox(10, updateWeight, refresh);
    }

    private void updateWeight() {
        Animal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextInputDialog weightDialog = new TextInputDialog(String.valueOf(selected.getWeight()));
        weightDialog.setHeaderText("Nouveau poids pour " + selected.getId());
        String text = weightDialog.showAndWait().orElse(null);
        if (text == null || text.isBlank()) {
            return;
        }

        try {
            double newWeight = Double.parseDouble(text.trim());
            for (Zone zone : store.getZones()) {
                if (zone instanceof LivestockZone livestockZone && livestockZone.getAnimals().contains(selected)) {
                    store.updateWeight(zone.getCode(), selected.getId(), newWeight);
                    refresh();
                    return;
                }
                if (zone instanceof AquacultureZone aquacultureZone && aquacultureZone.getSpecies().contains(selected)) {
                    store.updateWeight(zone.getCode(), selected.getId(), newWeight);
                    refresh();
                    return;
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private TableView<Animal> buildTable() {
        TableColumn<Animal, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Animal, String> species = new TableColumn<>("Espèce");
        species.setCellValueFactory(new PropertyValueFactory<>("species"));
        TableColumn<Animal, Number> age = new TableColumn<>("Âge");
        age.setCellValueFactory(new PropertyValueFactory<>("age"));
        TableColumn<Animal, Number> weight = new TableColumn<>("Poids");
        weight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        TableColumn<Animal, Object> health = new TableColumn<>("Santé");
        health.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));

        tableView.getColumns().addAll(id, species, age, weight, health);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        animals.setAll(store.getAllAnimals());
    }
}
