package ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import ui.model.FarmDataStore;

public class MainView extends BorderPane {
    private final FarmDataStore store;
    private final ViewRegistry viewRegistry;

    public MainView(FarmDataStore store) {
        this.store = store;
        this.viewRegistry = new ViewRegistry(store);

        setLeft(createNavigation());
        setCenter(viewRegistry.getDashboardView());
        setPadding(new Insets(16));
    }

    private VBox createNavigation() {
        Button dashboard = createNavButton("Dashboard", () -> setCenter(viewRegistry.getDashboardView()));
        Button zones = createNavButton("Gestion des Zones", () -> setCenter(viewRegistry.getZoneView()));
        Button crops = createNavButton("Gestion des Cultures", () -> setCenter(viewRegistry.getCropView()));
        Button animals = createNavButton("Gestion des Animaux", () -> setCenter(viewRegistry.getAnimalView()));
        Button sensors = createNavButton("Gestion des Capteurs", () -> setCenter(viewRegistry.getSensorView()));
        Button alerts = createNavButton("Panneau des Alertes", () -> setCenter(viewRegistry.getAlertView()));
        Button charts = createNavButton("Visualisation Graphique", () -> setCenter(viewRegistry.getChartView()));

        VBox navigation = new VBox(10, dashboard, zones, crops, animals, sensors, alerts, charts);
        navigation.setPadding(new Insets(16));
        navigation.setMinWidth(220);
        navigation.setAlignment(Pos.TOP_LEFT);
        navigation.getStyleClass().add("sidebar");
        return navigation;
    }

    private Button createNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            viewRegistry.refreshAll();
            action.run();
        });
        button.getStyleClass().add("nav-button");
        return button;
    }
}
