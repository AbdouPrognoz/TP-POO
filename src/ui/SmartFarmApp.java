package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.model.FarmDataStore;
import ui.view.MainView;

public class SmartFarmApp extends Application {
    private FarmDataStore store;

    @Override
    public void start(Stage stage) {
        store = new FarmDataStore();
        MainView root = new MainView(store);

        Scene scene = new Scene(root, 1400, 900);
        if (getClass().getResource("/ui/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/ui/style.css").toExternalForm());
        }

        stage.setTitle("Smart Farming Dashboard");
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void stop() throws Exception {
        if (store != null) {
            Persistence.PersistenceService.saveState(
                store.getFarmSystem().getZonesMap(),
                store.getFarmSystem().getAlertsList()
            );
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
