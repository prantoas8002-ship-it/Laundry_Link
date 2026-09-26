package controller;

import database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        loadDashboardStats();
    }


    // =========================================================
    // LOAD DASHBOARD STATISTICS
    // =========================================================

    private void loadDashboardStats() {

        String pendingSql = """
                SELECT COUNT(*)
                FROM orders
                WHERE status = 'PENDING'
                """;

        String processingSql = """
                SELECT COUNT(*)
                FROM orders
                WHERE status = 'PROCESSING'
                """;

        String readySql = """
                SELECT COUNT(*)
                FROM orders
                WHERE status = 'READY'
                """;

        String completedTodaySql = """
                SELECT COUNT(*)
                FROM orders
                WHERE status = 'DELIVERED'
                AND delivery_date = date('now')
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement pendingPs =
                        conn.prepareStatement(pendingSql);

                PreparedStatement processingPs =
                        conn.prepareStatement(processingSql);

                PreparedStatement readyPs =
                        conn.prepareStatement(readySql);

                PreparedStatement completedPs =
                        conn.prepareStatement(completedTodaySql);

                ResultSet pendingRs =
                        pendingPs.executeQuery();

                ResultSet processingRs =
                        processingPs.executeQuery();

                ResultSet readyRs =
                        readyPs.executeQuery();

                ResultSet completedRs =
                        completedPs.executeQuery()
        ) {

            // Pending

            if (pendingRs.next()) {

                pendingOrdersLabel.setText(
                        String.valueOf(
                                pendingRs.getInt(1)
                        )
                );
            }


            // Processing

            if (processingRs.next()) {

                washingOrdersLabel.setText(
                        String.valueOf(
                                processingRs.getInt(1)
                        )
                );
            }


            // Ready

            if (readyRs.next()) {

                readyDeliveryLabel.setText(
                        String.valueOf(
                                readyRs.getInt(1)
                        )
                );
            }


            // Completed Today

            if (completedRs.next()) {

                completedTodayLabel.setText(
                        String.valueOf(
                                completedRs.getInt(1)
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    "Could not load dashboard statistics."
            );
        }
    }


    // =========================================================
    // HOME
    // =========================================================

    @FXML
    private void showHome(ActionEvent event) {

        loadDashboardStats();
    }


    // =========================================================
    // NEW ORDER
    // =========================================================

    @FXML
    private void openNewOrder(ActionEvent event) {

        loadPage(
                event,
                "/fxml/new-order.fxml"
        );
    }


    @FXML
    private void createNewOrder(ActionEvent event) {

        loadPage(
                event,
                "/fxml/new-order.fxml"
        );
    }


    // =========================================================
    // ORDER MANAGEMENT
    // =========================================================

    @FXML
    private void openOrderManagement(ActionEvent event) {

        loadPage(
                event,
                "/fxml/order-management.fxml"
        );
    }


    // =========================================================
    // INVENTORY
    // =========================================================

    @FXML
    private void openInventory(ActionEvent event) {

        loadPage(
                event,
                "/fxml/Inventory.fxml"
        );
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @FXML
    private void updateStatus(ActionEvent event) {

        loadPage(
                event,
                "/fxml/order-management.fxml"
        );
    }


    // =========================================================
    // MARK DELIVERED
    // =========================================================

    @FXML
    private void markDelivered(ActionEvent event) {

        loadPage(
                event,
                "/fxml/order-management.fxml"
        );
    }


    // =========================================================
    // SEARCH ORDERS
    // =========================================================

    @FXML
    private void searchOrders() {

        String search =
                searchField.getText().trim();

        if (search.isEmpty()) {

            showMessage(
                    "Please enter an order ID or customer name."
            );

            return;
        }

        /*
         * Order Management page contains
         * the actual order search/filter system.
         */

        showMessage(
                "Search: " + search +
                        "\n\nPlease use Order Management to search orders."
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void logout(ActionEvent event) {

        model.Session.userId = 0;
        model.Session.username = null;
        model.Session.role = null;

        loadPage(
                event,
                "/fxml/Login.fxml"
        );
    }


    // =========================================================
    // PAGE LOADER
    // =========================================================

    private void loadPage(
            ActionEvent event,
            String path
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(path)
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setMaximized(true);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    "Cannot open page:\n" + path
            );
        }
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showMessage(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}