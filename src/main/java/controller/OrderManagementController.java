package controller;

import database.DBConnection;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Order;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class OrderManagementController {

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, String> orderIdCol;

    @FXML
    private TableColumn<Order, String> customerCol;

    @FXML
    private TableColumn<Order, String> serviceCol;

    @FXML
    private TableColumn<Order, Integer> quantityCol;

    @FXML
    private TableColumn<Order, String> statusCol;

    @FXML
    private TableColumn<Order, Double> costCol;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label selectedOrderLabel;

    @FXML
    private TextField searchField;

    private final ObservableList<Order> orders =
            FXCollections.observableArrayList();


    // ==========================================
    // INITIALIZE
    // ==========================================

    @FXML
    public void initialize() {

        setupTable();

        loadOrdersFromDatabase();

        ordersTable.setItems(orders);

        updateTotalOrders();

        makeColumnsEqualWidth();

        ordersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {

                    if (newVal != null) {

                        selectedOrderLabel.setText(
                                newVal.getOrderId()
                        );

                    } else {

                        selectedOrderLabel.setText("None");
                    }
                });
    }


    // ==========================================
    // SETUP TABLE
    // ==========================================

    private void setupTable() {

        orderIdCol.setCellValueFactory(
                new PropertyValueFactory<>("orderId")
        );

        customerCol.setCellValueFactory(
                new PropertyValueFactory<>("customer")
        );

        serviceCol.setCellValueFactory(
                new PropertyValueFactory<>("service")
        );

        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        costCol.setCellValueFactory(
                new PropertyValueFactory<>("cost")
        );
    }


    // ==========================================
    // LOAD ORDERS FROM DATABASE
    // ==========================================

    private void loadOrdersFromDatabase() {

        orders.clear();

        String sql = """
                SELECT
                    o.id AS order_id,
                    c.name AS customer_name,
                    s.name AS service_name,
                    oi.quantity AS quantity,
                    o.status AS status,
                    o.total_cost AS total_cost
                FROM orders o
                JOIN customers c
                    ON o.customer_id = c.id
                JOIN order_items oi
                    ON o.id = oi.order_id
                JOIN services s
                    ON oi.service_id = s.id
                ORDER BY o.id DESC
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

                String orderId =
                        "ORD-" + rs.getInt("order_id");

                String customer =
                        rs.getString("customer_name");

                String service =
                        rs.getString("service_name");

                int quantity =
                        rs.getInt("quantity");

                String status =
                        rs.getString("status");

                double cost =
                        rs.getDouble("total_cost");


                Order order =
                        new Order(
                                orderId,
                                customer,
                                service,
                                quantity,
                                status,
                                cost
                        );

                orders.add(order);
            }

            System.out.println(
                    "ORDERS LOADED: " +
                            orders.size()
            );

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Database Error",
                    "Could not load orders from database.\n\n"
                            + e.getMessage()
            );
        }
    }


    // ==========================================
    // EQUAL COLUMN WIDTH
    // ==========================================

    private void makeColumnsEqualWidth() {

        double borderPadding = 2.0;

        orderIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty()
                                .subtract(borderPadding),
                        6
                )
        );

        customerCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty()
                                .subtract(borderPadding),
                        6
                )
        );

        serviceCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty()
                                .subtract(borderPadding),
                        6
                )
        );

        quantityCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty()
                                .subtract(borderPadding),
                        6
                )
        );

        statusCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty()
                                .subtract(borderPadding),
                        6
                )
        );

        costCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty()
                                .subtract(borderPadding),
                        6
                )
        );

        orderIdCol.setResizable(false);
        customerCol.setResizable(false);
        serviceCol.setResizable(false);
        quantityCol.setResizable(false);
        statusCol.setResizable(false);
        costCol.setResizable(false);
    }


    // ==========================================
    // UPDATE TOTAL ORDERS
    // ==========================================

    private void updateTotalOrders() {

        totalOrdersLabel.setText(
                String.valueOf(orders.size())
        );
    }


    // ==========================================
    // ADD ORDER
    // ==========================================

    @FXML
    private void addOrder(ActionEvent event) {

        loadPage(
                event,
                "/fxml/new-order.fxml"
        );
    }

// ==========================================
// UPDATE ORDER
// ==========================================

    @FXML
    private void updateOrder() {

        Order selectedOrder =
                ordersTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedOrder == null) {

            showAlert(
                    "No Order Selected",
                    "Please select an order first."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/update-order.fxml"
                            )
                    );

            Parent root = loader.load();

            UpdateOrderController controller =
                    loader.getController();

            // ORD-5 -> 5
            int orderId = Integer.parseInt(
                    selectedOrder.getOrderId()
                            .replace("ORD-", "")
            );

            // Send selected order ID
            controller.setOrderId(orderId);

            Stage stage =
                    (Stage) ordersTable
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Could not open Update Order page.\n\n"
                            + e.getMessage()
            );

        } catch (NumberFormatException e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Invalid Order ID."
            );
        }
    }


    // ==========================================
    // DELETE ORDER
    // ==========================================

    @FXML
    private void deleteOrder(ActionEvent event) {

        Order selected =
                ordersTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    "Warning",
                    "Please select an order."
            );

            return;
        }

        // Extract numeric ID
        String orderIdText =
                selected.getOrderId()
                        .replace("ORD-", "");

        int orderId;

        try {

            orderId =
                    Integer.parseInt(orderIdText);

        } catch (NumberFormatException e) {

            showAlert(
                    "Error",
                    "Invalid order ID."
            );

            return;
        }


        // Confirmation

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle("Delete Order");

        confirm.setHeaderText(
                "Delete " + selected.getOrderId() + "?"
        );

        confirm.setContentText(
                "This will permanently delete the order."
        );


        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }


        // Delete from database

        String deleteItemsSql =
                "DELETE FROM order_items WHERE order_id = ?";

        String deleteOrderSql =
                "DELETE FROM orders WHERE id = ?";


        try (
                Connection conn =
                        DBConnection.getConnection()
        ) {

            conn.setAutoCommit(false);

            try {

                // First delete order items

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        deleteItemsSql
                                )
                ) {

                    ps.setInt(1, orderId);

                    ps.executeUpdate();
                }


                // Then delete order

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        deleteOrderSql
                                )
                ) {

                    ps.setInt(1, orderId);

                    ps.executeUpdate();
                }


                conn.commit();


                orders.remove(selected);

                updateTotalOrders();

                selectedOrderLabel.setText("None");


                showAlert(
                        "Success",
                        "Order deleted successfully."
                );

            } catch (Exception e) {

                conn.rollback();

                throw e;
            }

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Database Error",
                    "Could not delete order.\n\n"
                            + e.getMessage()
            );
        }
    }


    // ==========================================
    // SEARCH
    // ==========================================

    @FXML
    private void searchOrder(ActionEvent event) {

        String keyword =
                searchField.getText()
                        .trim()
                        .toLowerCase();

        if (keyword.isEmpty()) {

            ordersTable.setItems(orders);

            return;
        }


        ObservableList<Order> filtered =
                FXCollections.observableArrayList();


        for (Order order : orders) {

            if (
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
                                    .contains(keyword)
            ) {

                filtered.add(order);
            }
        }


        ordersTable.setItems(filtered);
    }


    // ==========================================
    // DASHBOARD
    // ==========================================

    @FXML
    private void goDashboard(ActionEvent event) {

        loadPage(
                event,
                "/fxml/admin-dashboard.fxml"
        );
    }


    // ==========================================
    // PAGE LOADING
    // ==========================================

    private void loadPage(
            ActionEvent event,
            String path
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass()
                                    .getResource(path)
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

            showAlert(
                    "Error",
                    "Could not load:\n" + path
            );
        }
    }


    // ==========================================
    // ALERT
    // ==========================================

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}