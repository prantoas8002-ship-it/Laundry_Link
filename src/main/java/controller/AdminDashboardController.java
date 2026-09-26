package controller;

import database.DBConnection;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboardController {

    // =========================================================
    // FXML COMPONENTS
    // =========================================================

    @FXML
    private TextField searchField;

    @FXML
    private TableView<OrderRow> ordersTable;

    @FXML
    private TableColumn<OrderRow, String> orderIdColumn;

    @FXML
    private TableColumn<OrderRow, String> customerColumn;

    @FXML
    private TableColumn<OrderRow, String> serviceColumn;

    @FXML
    private TableColumn<OrderRow, String> statusColumn;

    @FXML
    private TableColumn<OrderRow, Double> amountColumn;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label pendingOrdersLabel;

    @FXML
    private Label completedOrdersLabel;

    @FXML
    private Label revenueLabel;


    // =========================================================
    // ORDER LIST
    // =========================================================

    private final ObservableList<OrderRow> orderList =
            FXCollections.observableArrayList();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        ordersTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        setupColumns();

        loadDashboardData();

        loadRecentOrders();
    }


    // =========================================================
    // TABLE COLUMNS
    // =========================================================

    private void setupColumns() {

        orderIdColumn.setCellValueFactory(
                data -> data.getValue().orderIdProperty()
        );

        customerColumn.setCellValueFactory(
                data -> data.getValue().customerProperty()
        );

        serviceColumn.setCellValueFactory(
                data -> data.getValue().serviceProperty()
        );

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        amountColumn.setCellValueFactory(
                data -> data.getValue().amountProperty().asObject()
        );
    }


    // =========================================================
    // DASHBOARD STATISTICS
    // =========================================================

    private void loadDashboardData() {

        String totalSql =
                "SELECT COUNT(*) FROM orders";

        String pendingSql =
                "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'";

        String completedSql =
                "SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED'";

        String revenueSql =
                """
                SELECT COALESCE(SUM(total_cost), 0)
                FROM orders
                WHERE status = 'DELIVERED'
                """;


        try (Connection conn = DBConnection.getConnection()) {

            // -------------------------------------------------
            // TOTAL ORDERS
            // -------------------------------------------------

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(totalSql);

                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    totalOrdersLabel.setText(
                            String.valueOf(
                                    rs.getInt(1)
                            )
                    );
                }
            }


            // -------------------------------------------------
            // PENDING ORDERS
            // -------------------------------------------------

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(pendingSql);

                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    pendingOrdersLabel.setText(
                            String.valueOf(
                                    rs.getInt(1)
                            )
                    );
                }
            }


            // -------------------------------------------------
            // COMPLETED ORDERS
            // -------------------------------------------------

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(completedSql);

                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    completedOrdersLabel.setText(
                            String.valueOf(
                                    rs.getInt(1)
                            )
                    );
                }
            }


            // -------------------------------------------------
            // REVENUE
            // -------------------------------------------------

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(revenueSql);

                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    double revenue =
                            rs.getDouble(1);

                    revenueLabel.setText(
                            String.format(
                                    "%.2f",
                                    revenue
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    "Could not load dashboard data."
            );
        }
    }


    // =========================================================
    // RECENT ORDERS
    // =========================================================

    private void loadRecentOrders() {

        orderList.clear();

        String sql =
                """
                SELECT
                    o.id AS order_id,
                    c.name AS customer_name,
                    s.name AS service_name,
                    o.status,
                    o.total_cost
                FROM orders o

                JOIN customers c
                    ON o.customer_id = c.id

                LEFT JOIN order_items oi
                    ON o.id = oi.order_id

                LEFT JOIN services s
                    ON oi.service_id = s.id

                ORDER BY o.id DESC

                LIMIT 5
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                OrderRow row =
                        new OrderRow(

                                "ORD-" +
                                        rs.getInt("order_id"),

                                rs.getString(
                                        "customer_name"
                                ),

                                rs.getString(
                                        "service_name"
                                ),

                                rs.getString(
                                        "status"
                                ),

                                rs.getDouble(
                                        "total_cost"
                                )
                        );

                orderList.add(row);
            }

            ordersTable.setItems(orderList);

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    "Could not load recent orders."
            );
        }
    }


    // =========================================================
    // DASHBOARD BUTTON
    // =========================================================

    @FXML
    private void showDashboard(ActionEvent event) {

        loadDashboardData();

        loadRecentOrders();

        searchField.clear();
    }


    // =========================================================
    // ORDERS
    // =========================================================

    @FXML
    private void openOrders(ActionEvent event) {

        loadPage(
                event,
                "/fxml/order-management.fxml"
        );
    }


    // =========================================================
    // CUSTOMERS
    // =========================================================

    @FXML
    private void openCustomers(ActionEvent event) {

        loadPage(
                event,
                "/fxml/customer-history.fxml"
        );
    }


    // =========================================================
    // BILLING
    // =========================================================

    @FXML
    private void openBilling(ActionEvent event) {

        loadPage(
                event,
                "/fxml/billing-page.fxml"
        );
    }


    // =========================================================
    // INVENTORY
    // =========================================================

    @FXML
    private void openInventory(ActionEvent event) {

        loadPage(
                event,
                "/fxml/inventory.fxml"
        );
    }


    // =========================================================
    // VIEW ALL ORDERS
    // =========================================================

    @FXML
    private void viewAllOrders(ActionEvent event) {

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

        String keyword =
                searchField.getText()
                        .trim()
                        .toLowerCase();


        // Empty search
        if (keyword.isEmpty()) {

            ordersTable.setItems(
                    orderList
            );

            return;
        }


        ObservableList<OrderRow> filtered =
                FXCollections.observableArrayList();


        for (OrderRow order : orderList) {

            boolean matches =

                    order.getOrderId()
                            .toLowerCase()
                            .contains(keyword)

                            ||

                            order.getCustomer()
                                    .toLowerCase()
                                    .contains(keyword)

                            ||

                            order.getService()
                                    .toLowerCase()
                                    .contains(keyword)

                            ||

                            order.getStatus()
                                    .toLowerCase()
                                    .contains(keyword);


            if (matches) {

                filtered.add(order);
            }
        }


        ordersTable.setItems(filtered);
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void logout(ActionEvent event) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/Login.fxml"
                            )
                    );


            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            stage.setScene(
                    new Scene(root)
            );

            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            showMessage(
                    "Could not logout."
            );
        }
    }


    // =========================================================
    // PAGE LOADER
    // =========================================================

    private void loadPage(
            ActionEvent event,
            String path
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(path)
                    );


            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            stage.setScene(
                    new Scene(root)
            );

            stage.setMaximized(true);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle("Error");

            alert.setHeaderText(null);

            alert.setContentText(
                    "Could not open:\n" +
                            path
            );

            alert.showAndWait();
        }
    }


    // =========================================================
    // MESSAGE
    // =========================================================

    private void showMessage(String msg) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "LaundryLink"
        );

        alert.setHeaderText(null);

        alert.setContentText(msg);

        alert.showAndWait();
    }


    // =========================================================
    // RECENT ORDER MODEL
    // =========================================================

    public static class OrderRow {

        private final SimpleStringProperty orderId;

        private final SimpleStringProperty customer;

        private final SimpleStringProperty service;

        private final SimpleStringProperty status;

        private final SimpleDoubleProperty amount;


        public OrderRow(
                String orderId,
                String customer,
                String service,
                String status,
                double amount
        ) {

            this.orderId =
                    new SimpleStringProperty(
                            orderId
                    );

            this.customer =
                    new SimpleStringProperty(
                            customer
                    );

            this.service =
                    new SimpleStringProperty(
                            service
                    );

            this.status =
                    new SimpleStringProperty(
                            status
                    );

            this.amount =
                    new SimpleDoubleProperty(
                            amount
                    );
        }


        // -----------------------------------------------------
        // PROPERTIES
        // -----------------------------------------------------

        public StringProperty orderIdProperty() {

            return orderId;
        }


        public StringProperty customerProperty() {

            return customer;
        }


        public StringProperty serviceProperty() {

            return service;
        }


        public StringProperty statusProperty() {

            return status;
        }


        public DoubleProperty amountProperty() {

            return amount;
        }


        // -----------------------------------------------------
        // GETTERS
        // -----------------------------------------------------

        public String getOrderId() {

            return orderId.get();
        }


        public String getCustomer() {

            return customer.get();
        }


        public String getService() {

            return service.get();
        }


        public String getStatus() {

            return status.get();
        }


        public double getAmount() {

            return amount.get();
        }
    }
}