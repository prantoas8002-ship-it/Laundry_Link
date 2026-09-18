package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class NewOrderController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> serviceBox;

    @FXML
    private TextField quantityField;

    @FXML
    private DatePicker pickupDatePicker;

    @FXML
    private TextField costField;

    @FXML
    private TextArea notesArea;

    @FXML
    public void initialize() {

        serviceBox.getItems().addAll(
                "Wash",
                "Iron",
                "Dry Clean",
                "Premium Wash"
        );
    }

    @FXML
    private void calculateCost() {

        try {

            if (serviceBox.getValue() == null ||
                    quantityField.getText().isEmpty()) {
                return;
            }

            int quantity = Integer.parseInt(
                    quantityField.getText()
            );

            double pricePerItem = 0;

            switch (serviceBox.getValue()) {

                case "Wash":
                    pricePerItem = 30;
                    break;

                case "Iron":
                    pricePerItem = 20;
                    break;

                case "Dry Clean":
                    pricePerItem = 80;
                    break;

                case "Premium Wash":
                    pricePerItem = 50;
                    break;
            }

            double total = quantity * pricePerItem;

            costField.setText("৳ " + total);

        } catch (Exception ignored) {

        }
    }

    @FXML
    private void createOrder(ActionEvent event) {

        if (nameField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                serviceBox.getValue() == null ||
                quantityField.getText().trim().isEmpty() ||
                pickupDatePicker.getValue() == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Please fill all required fields."
            );
            alert.showAndWait();

            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Order Created Successfully");
        alert.setContentText(
                "Customer: " + nameField.getText()
                        + "\nService: " + serviceBox.getValue()
                        + "\nCost: " + costField.getText()
        );

        alert.showAndWait();

        clearForm();
    }

    @FXML
    private void clearForm() {

        nameField.clear();
        phoneField.clear();
        quantityField.clear();
        costField.clear();
        notesArea.clear();

        serviceBox.getSelectionModel().clearSelection();

        pickupDatePicker.setValue(null);
    }

    @FXML
    private void goDashboard(ActionEvent event) {

        loadPage(
                event,
                "/fxml/customer-dashboard.fxml"
        );
    }

    private void loadPage(
            ActionEvent event,
            String fxmlPath
    ) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            Alert alert = new Alert(
                    Alert.AlertType.ERROR
            );

            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Could not load:\n" + fxmlPath
            );

            alert.showAndWait();
        }
    }
}