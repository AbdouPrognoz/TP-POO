package ui.view;

import Zone.Zone;
import Zone.CropZone;
import Zone.LivestockZone;
import Zone.AquacultureZone;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

    private HBox buildActions() {
        Button add = new Button("Ajouter");
        Button modify = new Button("Modifier");
        Button suspend = new Button("Suspendre");
        Button reactivate = new Button("Réactiver");
        Button refresh = new Button("Actualiser");

        add.setOnAction(e -> addZone());
        modify.setOnAction(e -> modifyZone());
        suspend.setOnAction(e -> {
            Zone selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                store.suspendZone(selected.getCode());
                refresh();
            }
        });
        reactivate.setOnAction(e -> {
            Zone selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                store.reactivateZone(selected.getCode());
                refresh();
            }
        });
        refresh.setOnAction(e -> refresh());

        HBox box = new HBox(10, add, modify, suspend, reactivate, refresh);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    private void addZone() {
        TextInputDialog typeDialog = new TextInputDialog("CROP");
        typeDialog.setHeaderText("Type de zone");
        typeDialog.setContentText("CROP, LIVESTOCK ou AQUACULTURE");
        String type = typeDialog.showAndWait().orElse(null);
        if (type == null || type.isBlank()) {
            return;
        }

        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setHeaderText("Code de zone");
        String code = codeDialog.showAndWait().orElse(null);
        if (code == null || code.isBlank()) {
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setHeaderText("Nom de zone");
        String name = nameDialog.showAndWait().orElse(null);
        if (name == null || name.isBlank()) {
            return;
        }

        Zone zone = switch (type.trim().toUpperCase()) {
            case "LIVESTOCK" -> new LivestockZone(code.trim(), name.trim(), "LIVESTOCK");
            case "AQUACULTURE" -> new AquacultureZone(code.trim(), name.trim(), "AQUACULTURE");
            default -> new CropZone(code.trim(), name.trim(), "CROP");
        };

        store.addZone(zone);
        refresh();
    }

    private void modifyZone() {
        Zone selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setHeaderText("Nouveau nom pour " + selected.getCode());
        String newName = nameDialog.showAndWait().orElse(selected.getName());
        if (newName == null || newName.isBlank()) {
            return;
        }

        store.getFarmSystem().editZone(selected.getCode(), newName.trim(), selected.getType());
        refresh();
    }

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
        hosted.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getHostedCount()));

        tableView.getColumns().addAll(code, name, type, status, hosted);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        zones.setAll(store.getZones());
    }
}
