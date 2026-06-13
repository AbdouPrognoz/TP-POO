package ui.view;

import Crops.*;
import Zone.CropZone;
import Zone.Zone;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import ui.model.FarmDataStore;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CropView extends BorderPane {
    private final FarmDataStore store;
    private final ObservableList<Crops> crops = FXCollections.observableArrayList();
    private final TableView<Crops> tableView = new TableView<>(crops);

    public CropView(FarmDataStore store) {
        this.store = store;
        setPadding(new Insets(16));
        setTop(buildActions());
        setCenter(buildTable());
        refresh();
    }

    private HBox buildActions() {
        Button addBtn = new Button("Ajouter une culture");
        Button updateStageBtn = new Button("Mettre à jour le stade");
        Button refreshBtn = new Button("Actualiser");

        addBtn.setOnAction(e -> showAddCropDialog());
        updateStageBtn.setOnAction(e -> updateGrowthStage());
        refreshBtn.setOnAction(e -> refresh());

        HBox box = new HBox(10, addBtn, updateStageBtn, refreshBtn);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    private void showAddCropDialog() {
        // --- Step 1: choose zone ---
        List<Zone> cropZones = store.getZones().stream()
                .filter(z -> z instanceof CropZone)
                .collect(Collectors.toList());

        if (cropZones.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucune zone de culture disponible.").showAndWait();
            return;
        }

        ChoiceDialog<Zone> zoneDialog = new ChoiceDialog<>(cropZones.get(0), cropZones);
        zoneDialog.setTitle("Nouvelle culture");
        zoneDialog.setHeaderText("Sélectionnez la zone de culture");
        zoneDialog.setContentText("Zone :");
        Optional<Zone> zoneOpt = zoneDialog.showAndWait();
        if (zoneOpt.isEmpty()) return;
        String zoneCode = zoneOpt.get().getCode();

        // --- Step 2: choose family ---
        ChoiceDialog<String> familyDialog = new ChoiceDialog<>("CEREALS", "CEREALS", "FRUITS", "VEGETABLES");
        familyDialog.setTitle("Nouvelle culture");
        familyDialog.setHeaderText("Famille de la culture");
        familyDialog.setContentText("Famille :");
        Optional<String> familyOpt = familyDialog.showAndWait();
        if (familyOpt.isEmpty()) return;
        String family = familyOpt.get();

        // --- Step 3: choose specific type depending on family ---
        String cropType;
        switch (family) {
            case "FRUITS" -> {
                ChoiceDialog<F> typeDialog = new ChoiceDialog<>(F.APPLE, F.values());
                typeDialog.setTitle("Type de fruit");
                typeDialog.setHeaderText("Choisissez le type de fruit");
                typeDialog.setContentText("Type :");
                Optional<F> t = typeDialog.showAndWait();
                if (t.isEmpty()) return;
                cropType = t.get().name();
            }
            case "VEGETABLES" -> {
                ChoiceDialog<V> typeDialog = new ChoiceDialog<>(V.TOMATO, V.values());
                typeDialog.setTitle("Type de légume");
                typeDialog.setHeaderText("Choisissez le type de légume");
                typeDialog.setContentText("Type :");
                Optional<V> t = typeDialog.showAndWait();
                if (t.isEmpty()) return;
                cropType = t.get().name();
            }
            default -> { // CEREALS
                ChoiceDialog<C> typeDialog = new ChoiceDialog<>(C.WHEAT, C.values());
                typeDialog.setTitle("Type de céréale");
                typeDialog.setHeaderText("Choisissez le type de céréale");
                typeDialog.setContentText("Type :");
                Optional<C> t = typeDialog.showAndWait();
                if (t.isEmpty()) return;
                cropType = t.get().name();
            }
        }

        // --- Step 4: fill general fields in a grid dialog ---
        Dialog<ButtonType> form = new Dialog<>();
        form.setTitle("Nouvelle culture - Détails");
        form.setHeaderText("Renseignez les informations de la culture");
        form.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField idField = new TextField("C" + System.currentTimeMillis() % 10000);
        DatePicker plantingPicker = new DatePicker(LocalDate.now());
        DatePicker harvestPicker = new DatePicker(LocalDate.now().plusMonths(6));
        ChoiceBox<GrowthStage> stageBox = new ChoiceBox<>(FXCollections.observableArrayList(GrowthStage.values()));
        stageBox.setValue(GrowthStage.SOWING);
        TextField minPhField = new TextField("6.0");
        TextField maxPhField = new TextField("7.5");
        TextField minMoistureField = new TextField("40");
        TextField maxMoistureField = new TextField("70");

        grid.addRow(0, new Label("ID :"), idField);
        grid.addRow(1, new Label("Date de plantation :"), plantingPicker);
        grid.addRow(2, new Label("Date de récolte prévue :"), harvestPicker);
        grid.addRow(3, new Label("Stade de croissance :"), stageBox);
        grid.addRow(4, new Label("pH min :"), minPhField);
        grid.addRow(5, new Label("pH max :"), maxPhField);
        grid.addRow(6, new Label("Humidité min (%) :"), minMoistureField);
        grid.addRow(7, new Label("Humidité max (%) :"), maxMoistureField);

        form.getDialogPane().setContent(grid);

        Optional<ButtonType> result = form.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            String id = idField.getText().trim();
            LocalDate planting = plantingPicker.getValue();
            LocalDate harvest = harvestPicker.getValue();
            GrowthStage stage = stageBox.getValue();
            double minPh = Double.parseDouble(minPhField.getText().trim());
            double maxPh = Double.parseDouble(maxPhField.getText().trim());
            double minMoisture = Double.parseDouble(minMoistureField.getText().trim());
            double maxMoisture = Double.parseDouble(maxMoistureField.getText().trim());

            Crops crop = switch (family) {
                case "FRUITS" -> new Fruits(id, planting, harvest, stage, minPh, maxPh, minMoisture, maxMoisture, F.valueOf(cropType));
                case "VEGETABLES" -> new Vegetables(id, planting, harvest, stage, minPh, maxPh, minMoisture, maxMoisture, V.valueOf(cropType));
                default -> new Cereals(id, planting, harvest, stage, minPh, maxPh, minMoisture, maxMoisture, C.valueOf(cropType));
            };

            store.addCropToZone(zoneCode, crop);
            refresh();

        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Valeurs numériques invalides.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
        }
    }

    private void updateGrowthStage() {
        Crops selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        ChoiceDialog<GrowthStage> dialog = new ChoiceDialog<>(selected.getGrowthStage(), GrowthStage.values());
        dialog.setTitle("Mettre à jour le stade");
        dialog.setHeaderText("Nouveau stade pour " + selected.getId());
        dialog.setContentText("Stade :");
        dialog.showAndWait().ifPresent(stage -> {
            // find which zone owns this crop
            for (Zone zone : store.getZones()) {
                if (zone instanceof CropZone cropZone && cropZone.getCrops().contains(selected)) {
                    store.updateGrowthStage(zone.getCode(), selected.getId(), stage);
                    refresh();
                    return;
                }
            }
        });
    }

    private TableView<Crops> buildTable() {
        TableColumn<Crops, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Crops, String> zone = new TableColumn<>("Zone");
        zone.setCellValueFactory(data -> {
            Crops c = data.getValue();
            for (Zone z : store.getZones()) {
                if (z instanceof CropZone cz && cz.getCrops().contains(c))
                    return new SimpleStringProperty(z.getCode() + " – " + z.getName());
            }
            return new SimpleStringProperty("-");
        });

        TableColumn<Crops, String> family = new TableColumn<>("Famille");
        family.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFamily()));

        TableColumn<Crops, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(data -> {
            Crops c = data.getValue();
            String t = "";
            if (c instanceof Fruits f) t = f.getFruitType().name();
            else if (c instanceof Vegetables v) t = v.getVegetableType().name();
            else if (c instanceof Cereals ce) t = ce.getCerealType().name();
            return new SimpleStringProperty(t);
        });

        TableColumn<Crops, Object> planting = new TableColumn<>("Plantation");
        planting.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));

        TableColumn<Crops, Object> harvest = new TableColumn<>("Récolte prévue");
        harvest.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));

        TableColumn<Crops, Object> growth = new TableColumn<>("Stade");
        growth.setCellValueFactory(new PropertyValueFactory<>("growthStage"));

        TableColumn<Crops, String> ph = new TableColumn<>("pH (min-max)");
        ph.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMinPH() + " – " + data.getValue().getMaxPH()));

        TableColumn<Crops, String> moisture = new TableColumn<>("Humidité % (min-max)");
        moisture.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMinMoisture() + " – " + data.getValue().getMaxMoisture()));

        tableView.getColumns().addAll(id, zone, family, type, planting, harvest, growth, ph, moisture);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        crops.setAll(store.getAllCrops());
    }
}
