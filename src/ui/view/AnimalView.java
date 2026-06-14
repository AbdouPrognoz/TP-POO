package ui.view;

import Animals.*;
import Zone.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import ui.model.FarmDataStore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // ──────────────────────────────────────────────
    //  Toolbar
    // ──────────────────────────────────────────────

    private VBox buildActions() {
        // Row 1: main actions
        Button addBtn       = new Button("Ajouter un animal");
        Button healthBtn    = new Button("Événements sanitaires");
        Button refreshBtn   = new Button("Actualiser");

        addBtn.setOnAction(e -> showAddAnimalDialog());
        healthBtn.setOnAction(e -> showHealthDialog());
        refreshBtn.setOnAction(e -> refresh());

        HBox row1 = new HBox(10, addBtn, healthBtn, refreshBtn);

        // Row 2: quick-log actions
        Label quickLabel    = new Label("Mise à jour rapide :");
        quickLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        Button quickWeightBtn = new Button("⚖  Poids");
        Button quickHealthBtn = new Button("💊  État de santé");

        quickWeightBtn.setOnAction(e -> quickUpdateWeight());
        quickHealthBtn.setOnAction(e -> quickUpdateHealthStatus());

        HBox row2 = new HBox(10, quickLabel, quickWeightBtn, quickHealthBtn);
        row2.setStyle("-fx-padding: 4 0 0 0;");

        VBox box = new VBox(6, row1, row2);
        box.setPadding(new Insets(0, 0, 12, 0));
        return box;
    }

    // ──────────────────────────────────────────────
    //  Quick-log actions (no notes required)
    // ──────────────────────────────────────────────

    private void quickUpdateWeight() {
        Animal selected = requireSelection();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getWeight()));
        dialog.setTitle("Mise à jour du poids");
        dialog.setHeaderText("Nouveau poids pour " + selected.getId() + " (" + selected.getSpecies() + ")");
        dialog.setContentText("Poids (kg) :");
        dialog.showAndWait().ifPresent(text -> {
            try {
                double newWeight = Double.parseDouble(text.trim());
                Zone zone = findZoneOf(selected);
                if (zone != null) {
                    store.updateWeight(zone.getCode(), selected.getId(), newWeight);
                    refresh();
                }
            } catch (NumberFormatException ignored) {
                new Alert(Alert.AlertType.ERROR, "Poids invalide.").showAndWait();
            }
        });
    }

    private void quickUpdateHealthStatus() {
        Animal selected = requireSelection();
        if (selected == null) return;

        ChoiceDialog<HealthStatus> dialog = new ChoiceDialog<>(
                selected.getHealthStatus(), HealthStatus.values());
        dialog.setTitle("Mise à jour de l'état de santé");
        dialog.setHeaderText("Nouvel état pour " + selected.getId() + " (" + selected.getSpecies() + ")");
        dialog.setContentText("État de santé :");
        dialog.showAndWait().ifPresent(newStatus -> {
            Zone zone = findZoneOf(selected);
            if (zone != null) {
                store.updateHealthStatus(zone.getCode(), selected.getId(), newStatus);
                refresh();
            }
        });
    }

    private Animal requireSelection() {
        Animal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Veuillez sélectionner un animal dans le tableau.").showAndWait();
        }
        return selected;
    }

    // ──────────────────────────────────────────────
    //  Health events dialog (history + new event)
    // ──────────────────────────────────────────────

    private void showHealthDialog() {
        Animal selected = requireSelection();
        if (selected == null) return;

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(buildHistoryTab(selected), buildNewEventTab(selected));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Événements sanitaires – " + selected.getId());
        dialog.setHeaderText(selected.getCategory() + " | " + selected.getSpecies()
                + "  (ID : " + selected.getId() + ")");
        dialog.getDialogPane().setContent(tabs);
        dialog.getDialogPane().setPrefWidth(620);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        refresh();
    }

    /** Tab 1 — full chronological history */
    private Tab buildHistoryTab(Animal animal) {
        ObservableList<HealthEvent> events =
                FXCollections.observableArrayList(animal.getHealthEvents());

        TableColumn<HealthEvent, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDate().toString()));
        dateCol.setPrefWidth(100);

        TableColumn<HealthEvent, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d ->
                new SimpleStringProperty(formatEventType(d.getValue().getType())));
        typeCol.setPrefWidth(160);

        TableColumn<HealthEvent, String> notesCol = new TableColumn<>("Détails");
        notesCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNotes()));

        TableView<HealthEvent> table = new TableView<>(events);
        table.getColumns().addAll(dateCol, typeCol, notesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(300);
        table.setPlaceholder(new Label("Aucun événement enregistré pour cet animal."));

        Label summary = new Label("Total : " + events.size() + " événement(s)");
        summary.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        VBox content = new VBox(10, table, summary);
        content.setPadding(new Insets(12));

        Tab tab = new Tab("📋  Historique");
        tab.setContent(content);
        return tab;
    }

    /** Tab 2 — log a new illness / weight change / health status change */
    private Tab buildNewEventTab(Animal animal) {
        // --- Event type radio ---
        Label typeLabel  = new Label("Type d'événement :");
        ToggleGroup group = new ToggleGroup();
        RadioButton illnessRb = new RadioButton("Maladie");
        RadioButton weightRb  = new RadioButton("Évolution de poids");
        RadioButton statusRb  = new RadioButton("État de santé");
        illnessRb.setToggleGroup(group);
        weightRb.setToggleGroup(group);
        statusRb.setToggleGroup(group);
        illnessRb.setSelected(true);
        HBox radioBox = new HBox(16, illnessRb, weightRb, statusRb);

        // --- Weight field ---
        Label weightLabel = new Label("Nouveau poids (kg) :");
        TextField weightField = new TextField(String.valueOf(animal.getWeight()));

        // --- Health status picker ---
        Label statusLabel = new Label("Nouvel état de santé :");
        ChoiceBox<HealthStatus> statusBox =
                new ChoiceBox<>(FXCollections.observableArrayList(HealthStatus.values()));
        statusBox.setValue(animal.getHealthStatus());

        // --- Notes field ---
        Label notesLabel = new Label("Notes (optionnel) :");
        TextArea notesField = new TextArea();
        notesField.setPromptText("Observations ou détails supplémentaires...");
        notesField.setPrefRowCount(3);
        notesField.setWrapText(true);

        // Initially hide weight + status controls
        setVisible(false, weightLabel, weightField, statusLabel, statusBox);

        group.selectedToggleProperty().addListener((obs, old, now) -> {
            boolean isWeight = (now == weightRb);
            boolean isStatus = (now == statusRb);
            setVisible(isWeight, weightLabel, weightField);
            setVisible(isStatus, statusLabel, statusBox);
        });

        // --- Save button ---
        Button saveBtn = new Button("Enregistrer l'événement");
        saveBtn.setStyle("-fx-font-weight: bold;");
        Label feedbackLabel = new Label();

        saveBtn.setOnAction(e -> {
            Zone zone = findZoneOf(animal);
            if (zone == null) {
                showFeedback(feedbackLabel, false, "⚠ Zone introuvable pour cet animal.");
                return;
            }

            String notes = notesField.getText().trim();

            if (illnessRb.isSelected()) {
                if (notes.isBlank()) {
                    showFeedback(feedbackLabel, false, "⚠ Veuillez saisir des notes pour la maladie.");
                    return;
                }
                store.getFarmSystem().logIllness(zone.getCode(), animal.getId(), notes);

            } else if (weightRb.isSelected()) {
                try {
                    double newWeight = Double.parseDouble(weightField.getText().trim());
                    store.updateWeight(zone.getCode(), animal.getId(), newWeight);
                } catch (NumberFormatException ex) {
                    showFeedback(feedbackLabel, false, "⚠ Poids invalide.");
                    return;
                }

            } else if (statusRb.isSelected()) {
                HealthStatus newStatus = statusBox.getValue();
                store.updateHealthStatus(zone.getCode(), animal.getId(), newStatus);
            }

            notesField.clear();
            showFeedback(feedbackLabel, true, "✔ Événement enregistré avec succès !");
        });

        // --- Layout ---
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        grid.add(typeLabel,    0, 0); grid.add(radioBox,    1, 0);
        grid.add(weightLabel,  0, 1); grid.add(weightField, 1, 1);
        grid.add(statusLabel,  0, 2); grid.add(statusBox,   1, 2);
        grid.add(notesLabel,   0, 3); grid.add(notesField,  1, 3);
        grid.add(saveBtn,      1, 4);
        grid.add(feedbackLabel, 1, 5);

        ColumnConstraints col0 = new ColumnConstraints(170);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        Tab tab = new Tab("✏  Nouvel événement");
        tab.setContent(grid);
        return tab;
    }

    private void setVisible(boolean visible, javafx.scene.Node... nodes) {
        for (javafx.scene.Node n : nodes) {
            n.setVisible(visible);
            n.setManaged(visible);
        }
    }

    private void showFeedback(Label lbl, boolean ok, String msg) {
        lbl.setStyle(ok ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #c62828;");
        lbl.setText(msg);
    }

    private String formatEventType(HealthEventType type) {
        return switch (type) {
            case ILLNESS              -> "🤒 Maladie";
            case WEIGHT_CHANGE        -> "⚖ Évolution de poids";
            case HEALTH_STATUS_CHANGE -> "💊 État de santé";
        };
    }

    private Zone findZoneOf(Animal animal) {
        for (Zone zone : store.getZones()) {
            if (zone instanceof LivestockZone lz && lz.getAnimals().contains(animal)) return zone;
            if (zone instanceof AquacultureZone az && az.getSpecies().contains(animal)) return zone;
        }
        return null;
    }

    // ──────────────────────────────────────────────
    //  Add animal dialog
    // ──────────────────────────────────────────────

    private void showAddAnimalDialog() {
        List<Zone> animalZones = store.getZones().stream()
                .filter(z -> (z instanceof LivestockZone || z instanceof AquacultureZone) && z.getStatus() == StatusZone.ACTIVE)
                .collect(Collectors.toList());

        if (animalZones.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Aucune zone d'élevage ou aquacole disponible.").showAndWait();
            return;
        }

        ChoiceDialog<Zone> zoneDialog = new ChoiceDialog<>(animalZones.get(0), animalZones);
        zoneDialog.setTitle("Nouvel animal");
        zoneDialog.setHeaderText("Sélectionnez la zone de destination");
        zoneDialog.setContentText("Zone :");
        Optional<Zone> zoneOpt = zoneDialog.showAndWait();
        if (zoneOpt.isEmpty()) return;
        Zone chosenZone = zoneOpt.get();

        if (chosenZone instanceof LivestockZone) {
            showLandAnimalDialog(chosenZone.getCode());
        } else if (chosenZone instanceof AquacultureZone) {
            showAquaAnimalDialog(chosenZone.getCode());
        }
    }

    private void showLandAnimalDialog(String zoneCode) {
        ChoiceDialog<String> catDialog = new ChoiceDialog<>("RUMINANT", "RUMINANT", "POULTRY");
        catDialog.setTitle("Type d'animal terrestre");
        catDialog.setHeaderText("Choisissez la catégorie");
        catDialog.setContentText("Catégorie :");
        Optional<String> catOpt = catDialog.showAndWait();
        if (catOpt.isEmpty()) return;

        String speciesName;
        if ("POULTRY".equals(catOpt.get())) {
            ChoiceDialog<Poultry> specDialog = new ChoiceDialog<>(Poultry.CHICKEN, Poultry.values());
            specDialog.setTitle("Espèce de volaille");
            specDialog.setHeaderText("Choisissez l'espèce");
            specDialog.setContentText("Espèce :");
            Optional<Poultry> s = specDialog.showAndWait();
            if (s.isEmpty()) return;
            speciesName = s.get().name();
        } else {
            ChoiceDialog<Ruminant> specDialog = new ChoiceDialog<>(Ruminant.COW, Ruminant.values());
            specDialog.setTitle("Espèce de ruminant");
            specDialog.setHeaderText("Choisissez l'espèce");
            specDialog.setContentText("Espèce :");
            Optional<Ruminant> s = specDialog.showAndWait();
            if (s.isEmpty()) return;
            speciesName = s.get().name();
        }

        AnimalFormResult result = showAnimalForm();
        if (result == null) return;

        try {
            Animal animal = "POULTRY".equals(catOpt.get())
                    ? new Land(result.id, Poultry.valueOf(speciesName), result.age, result.weight, result.health)
                    : new Land(result.id, Ruminant.valueOf(speciesName), result.age, result.weight, result.health);
            store.addAnimalToZone(zoneCode, animal);
            refresh();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
        }
    }

    private void showAquaAnimalDialog(String zoneCode) {
        ChoiceDialog<AquaSpecies> specDialog = new ChoiceDialog<>(AquaSpecies.FISH, AquaSpecies.values());
        specDialog.setTitle("Espèce aquacole");
        specDialog.setHeaderText("Choisissez l'espèce aquacole");
        specDialog.setContentText("Espèce :");
        Optional<AquaSpecies> specOpt = specDialog.showAndWait();
        if (specOpt.isEmpty()) return;

        AnimalFormResult result = showAnimalForm();
        if (result == null) return;

        try {
            Animal animal = new Aqua(result.id, specOpt.get(), result.age, result.weight, result.health);
            store.addAnimalToZone(zoneCode, animal);
            refresh();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
        }
    }

    private AnimalFormResult showAnimalForm() {
        Dialog<ButtonType> form = new Dialog<>();
        form.setTitle("Informations de l'animal");
        form.setHeaderText("Renseignez les informations de l'animal");
        form.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField idField     = new TextField("AN" + System.currentTimeMillis() % 10000);
        TextField ageField    = new TextField("2");
        TextField weightField = new TextField("50.0");
        ChoiceBox<HealthStatus> healthBox =
                new ChoiceBox<>(FXCollections.observableArrayList(HealthStatus.values()));
        healthBox.setValue(HealthStatus.HEALTHY);

        grid.addRow(0, new Label("ID :"), idField);
        grid.addRow(1, new Label("Âge (années) :"), ageField);
        grid.addRow(2, new Label("Poids (kg) :"), weightField);
        grid.addRow(3, new Label("État de santé :"), healthBox);

        form.getDialogPane().setContent(grid);

        Optional<ButtonType> btn = form.showAndWait();
        if (btn.isEmpty() || btn.get() != ButtonType.OK) return null;

        try {
            return new AnimalFormResult(
                    idField.getText().trim(),
                    Integer.parseInt(ageField.getText().trim()),
                    Double.parseDouble(weightField.getText().trim()),
                    healthBox.getValue());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Âge ou poids invalide.").showAndWait();
            return null;
        }
    }

    private record AnimalFormResult(String id, int age, double weight, HealthStatus health) {}

    // ──────────────────────────────────────────────
    //  Table
    // ──────────────────────────────────────────────

    private TableView<Animal> buildTable() {
        TableColumn<Animal, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Animal, String> category = new TableColumn<>("Catégorie");
        category.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Animal, String> species = new TableColumn<>("Espèce");
        species.setCellValueFactory(new PropertyValueFactory<>("species"));

        TableColumn<Animal, Number> age = new TableColumn<>("Âge");
        age.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<Animal, Number> weight = new TableColumn<>("Poids (kg)");
        weight.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<Animal, Object> health = new TableColumn<>("Santé");
        health.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));

        TableColumn<Animal, String> zone = new TableColumn<>("Zone");
        zone.setCellValueFactory(data -> {
            Animal a = data.getValue();
            for (Zone z : store.getZones()) {
                if (z instanceof LivestockZone lz && lz.getAnimals().contains(a))
                    return new SimpleStringProperty(z.getCode() + " – " + z.getName());
                if (z instanceof AquacultureZone az && az.getSpecies().contains(a))
                    return new SimpleStringProperty(z.getCode() + " – " + z.getName());
            }
            return new SimpleStringProperty("-");
        });

        tableView.getColumns().addAll(id, category, species, age, weight, health, zone);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        animals.setAll(store.getAllAnimals());
    }
}
