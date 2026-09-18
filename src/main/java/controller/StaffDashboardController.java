package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class StaffDashboardController {

    @FXML
    private TextField searchField;

    @FXML
    private Label pendingOrdersLabel;

    @FXML
    private Label washingOrdersLabel;

    @FXML
    private Label readyDeliveryLabel;

    @FXML
    private Label completedTodayLabel;

    @FXML
    public void initialize() {

        pendingOrdersLabel.setText("12");
        washingOrdersLabel.setText("8");
        readyDeliveryLabel.setText("5");
        completedTodayLabel.setText("17");
    }

    @FXML
    private void showHome(ActionEvent event) {
        System.out.println("Home");
    }

    @FXML
    private void openNewOrder(ActionEvent event) {
        loadPage(event, "/fxml/new-order.fxml");
    }

    @FXML
    private void openProcessing(ActionEvent event) {
        showMessage("Processing page");
    }

    @FXML
    private void openDelivery(ActionEvent event) {
        showMessage("Delivery page");
    }

    @FXML
    private void openInventory(ActionEvent event) {
        loadPage(event, "/fxml/Inventory.fxml");
    }

    @FXML
    private void openSettings(ActionEvent event) {
        showMessage("Settings page");
    }

    @FXML
    private void createNewOrder(ActionEvent event) {
        loadPage(event, "/fxml/new-order.fxml");
    }

    @FXML
    private void updateStatus(ActionEvent event) {
        showMessage("Update status clicked");
    }

    @FXML
    private void markDelivered(ActionEvent event) {
        showMessage("Order marked delivered");
    }

    @FXML
    private void searchOrders() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(
                "Searching : " + searchField.getText()
        );
        alert.showAndWait();
    }

    @FXML
    private void logout(ActionEvent event) {

        loadPage(event, "/fxml/Login.fxml");
    }

    private void loadPage(ActionEvent event, String path) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(path)
                    );

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));

            stage.setMaximized(true);

            stage.show();

        } catch (Exception e) {

            showMessage(
                    "Cannot open page:\n" + path
            );
        }
    }

    private void showMessage(String msg) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(msg);

        alert.showAndWait();
    }
}