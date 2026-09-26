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
import javafx.stage.Stage;
import model.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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


    // ==========================================
    // INITIALIZE DASHBOARD
    // ==========================================

    @FXML
    public void initialize() {

        loadCustomerStatistics();
        loadRecentOrders();
    }


    // ==========================================
    // LOAD CUSTOMER STATISTICS
    // ==========================================

    private void loadCustomerStatistics() {

        String sql = """
                SELECT
                    COUNT(o.id) AS total_orders,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN o.status NOT IN ('DELIVERED', 'CANCELLED')
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS active_orders,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN o.status != 'CANCELLED'
                                THEN o.total_cost
                                ELSE 0
                            END
                        ),
                        0
                    ) AS total_spent
                FROM orders o
                JOIN customers c
                    ON o.customer_id = c.id
                WHERE c.user_id = ?
                """;


        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, Session.userId);

            ResultSet rs = ps.executeQuery();


            if (rs.next()) {

                int totalOrders =
                        rs.getInt("total_orders");

                int activeOrders =
                        rs.getInt("active_orders");

                double totalSpent =
                        rs.getDouble("total_spent");


                totalOrdersLabel.setText(
                        String.valueOf(totalOrders)
                );

                activeOrdersLabel.setText(
                        String.valueOf(activeOrders)
                );

                totalSpentLabel.setText(
                        String.format(
                                "৳%,.2f",
                                totalSpent
                        )
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            totalOrdersLabel.setText("0");
            activeOrdersLabel.setText("0");
            totalSpentLabel.setText("৳0.00");
        }
    }


    // ==========================================
    // LOAD RECENT ORDERS
    // ==========================================

    private void loadRecentOrders() {

        String sql = """
                SELECT
                    o.id AS order_id,
                    o.status AS status
                FROM orders o
                JOIN customers c
                    ON o.customer_id = c.id
                WHERE c.user_id = ?
                ORDER BY o.id DESC
                LIMIT 2
                """;


        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, Session.userId);

            ResultSet rs = ps.executeQuery();


            // Default values

            order1Label.setText("No Order");
            status1Label.setText("-");

            order2Label.setText("No Order");
            status2Label.setText("-");


            int count = 0;


            while (rs.next()) {

                int orderId =
                        rs.getInt("order_id");

                String status =
                        rs.getString("status");


                String orderText =
                        "Order #" + orderId;


                if (count == 0) {

                    order1Label.setText(orderText);
                    status1Label.setText(status);

                } else if (count == 1) {

                    order2Label.setText(orderText);
                    status2Label.setText(status);
                }


                count++;
            }


        } catch (Exception e) {

            e.printStackTrace();

            order1Label.setText("No Order");
            status1Label.setText("-");

            order2Label.setText("No Order");
            status2Label.setText("-");
        }
    }


    // ==========================================
    // DASHBOARD
    // ==========================================

    @FXML
    private void showDashboard(ActionEvent event) {

        showInfo(
                "You are already on Dashboard."
        );
    }


    // ==========================================
    // NEW ORDER
    // ==========================================

    @FXML
    private void openNewOrder(ActionEvent event) {

        loadPage(
                event,
                "/fxml/new-order.fxml"
        );
    }


    // ==========================================
    // TRACK ORDERS
    // ==========================================

    @FXML
    private void openTrackOrder(ActionEvent event) {

        loadPage(
                event,
                "/fxml/customer-orders.fxml"
        );
    }


    // ==========================================
    // PROFILE
    // ==========================================

    @FXML
    private void openProfile(ActionEvent event) {

        loadPage(
                event,
                "/fxml/Profile.fxml"
        );
    }


    // ==========================================
    // LOGOUT
    // ==========================================

    @FXML
    private void logout(ActionEvent event) {

        // Clear current session

        Session.userId = 0;
        Session.username = null;
        Session.role = null;


        loadPage(
                event,
                "/fxml/login.fxml"
        );
    }


    // ==========================================
    // LOAD PAGE
    // ==========================================

    private void loadPage(
            ActionEvent event,
            String fxmlPath
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(fxmlPath)
                    );


            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();


            Scene scene =
                    new Scene(root);


            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();


        } catch (IOException e) {

            Alert alert =
                    new Alert(Alert.AlertType.ERROR);

            alert.setTitle("FXML Error");

            alert.setHeaderText(
                    "Could not load page"
            );

            alert.setContentText(
                    fxmlPath
            );

            alert.showAndWait();

            e.printStackTrace();
        }
    }


    // ==========================================
    // INFO ALERT
    // ==========================================

    private void showInfo(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle("LaundryLink");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}

