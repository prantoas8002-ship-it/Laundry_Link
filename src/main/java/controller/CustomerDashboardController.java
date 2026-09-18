package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class CustomerDashboardController {

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label activeOrdersLabel;

    @FXML
    private Label totalSpentLabel;

    @FXML
    private Label order1Label;

    @FXML
    private Label status1Label;

    @FXML
    private Label order2Label;

    @FXML
    private Label status2Label;

    @FXML
    public void initialize() {

        totalOrdersLabel.setText("58");
        activeOrdersLabel.setText("3");
        totalSpentLabel.setText("৳12,500");

        order1Label.setText("Order #1001");
        status1Label.setText("Washing");

        order2Label.setText("Order #1002");
        status2Label.setText("Ready for Delivery");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        showInfo("You are already on Dashboard.");
    }

    @FXML
    private void openNewOrder(ActionEvent event) {
        loadPage(event, "/fxml/new-order.fxml");
    }

    @FXML
    private void openTrackOrder(ActionEvent event) {
        loadPage(event, "/fxml/order-management.fxml");
    }

    @FXML
    private void openProfile(ActionEvent event) {
        loadPage(event, "/fxml/Profile.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        loadPage(event, "/fxml/Login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FXML Error");
            alert.setHeaderText("Could not load page");
            alert.setContentText(fxmlPath);

            alert.showAndWait();

            e.printStackTrace();
        }
    }

    private void showInfo(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("LaundryLink");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}