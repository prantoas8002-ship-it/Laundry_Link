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

public class AdminDashboardController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<?> ordersTable;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label pendingOrdersLabel;

    @FXML
    private Label completedOrdersLabel;

    @FXML
    private Label revenueLabel;


    @FXML
    public void initialize() {
            ordersTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
            );

        totalOrdersLabel.setText("1245");
        pendingOrdersLabel.setText("83");
        completedOrdersLabel.setText("1162");
        revenueLabel.setText("12540");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        System.out.println("Dashboard");
    }

    @FXML
    private void openOrders(ActionEvent event) {
        loadPage(event, "/fxml/order-management.fxml");
    }

    @FXML
    private void openCustomers(ActionEvent event) {
        loadPage(event, "/fxml/customer-history.fxml");
    }

    @FXML
    private void openBilling(ActionEvent event) {
        loadPage(event, "/fxml/billing-page.fxml");
    }

    @FXML
    private void openInventory(ActionEvent event) {
        loadPage(event, "/fxml/inventory.fxml");
    }


    @FXML
    private void viewAllOrders(ActionEvent event) {
        showMessage("Showing all orders.");
    }

    @FXML
    private void searchOrders() {

        String keyword = searchField.getText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Search");
        alert.setHeaderText(null);
        alert.setContentText("Searching : " + keyword);
        alert.showAndWait();
    }

    @FXML
    private void logout(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/Login.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));

            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(ActionEvent event, String path) {

        try {

            Parent root =
                    FXMLLoader.load(getClass().getResource(path));

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));

            stage.setMaximized(true);

            stage.show();

        } catch (Exception e) {

            Alert alert =
                    new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open:\n" + path);
            //System.out.println(e);
            alert.showAndWait();
        }
    }

    private void showMessage(String msg) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("LaundryLink");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        alert.showAndWait();
    }
}