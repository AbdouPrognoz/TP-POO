package ui.view;

import Alerts.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AlertCardCell extends ListCell<Alert> {
    @Override
    protected void updateItem(Alert item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        Label title = new Label(item.getMessage());
        Label meta = new Label(item.getTimestamp() + " | " + item.getSeverity() + " | " + item.getStatus());
        VBox box = new VBox(4, title, meta);
        box.getStyleClass().add("alert-card");
        setGraphic(box);
    }
}
