package ui.view;

import Crops.Crops;
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
        Button refresh = new Button("Actualiser");
        refresh.setOnAction(e -> refresh());
        return new HBox(10, refresh);
    }

    private TableView<Crops> buildTable() {
        TableColumn<Crops, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Crops, Object> planting = new TableColumn<>("Plantation");
        planting.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));
        TableColumn<Crops, Object> harvest = new TableColumn<>("Récolte prévue");
        harvest.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        TableColumn<Crops, Object> growth = new TableColumn<>("Stade");
        growth.setCellValueFactory(new PropertyValueFactory<>("growthStage"));

        tableView.getColumns().addAll(id, planting, harvest, growth);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return tableView;
    }

    public void refresh() {
        crops.setAll(store.getAllCrops());
    }
}
