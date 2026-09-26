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
import model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private TextField searchField;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label selectedOrderLabel;

    @FXML
    private ComboBox<String> statusBox;

    @FXML
    private Button statusUpdateBtn;

    private final ObservableList<Order> orderList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        setupTable();

        loadOrders();

        setupStatusBox();

        ordersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldOrder, newOrder) -> {

                    if (newOrder != null) {

                        selectedOrderLabel.setText(
                                newOrder.getOrderId()
                        );

                        statusBox.setValue(
                                newOrder.getStatus()
                        );

                    } else {

                        selectedOrderLabel.setText("None");
                        statusBox.setValue(null);
                    }
                });

        updateStatusButtonState();
    }


    // =========================================================
    // TABLE SETUP
    // =========================================================

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

        ordersTable.setItems(orderList);

        orderIdCol.prefWidthProperty()
                .bind(ordersTable.widthProperty().multiply(0.12));

        customerCol.prefWidthProperty()
                .bind(ordersTable.widthProperty().multiply(0.20));

        serviceCol.prefWidthProperty()
                .bind(ordersTable.widthProperty().multiply(0.22));

        quantityCol.prefWidthProperty()
                .bind(ordersTable.widthProperty().multiply(0.12));

        statusCol.prefWidthProperty()
                .bind(ordersTable.widthProperty().multiply(0.17));

        costCol.prefWidthProperty()
                .bind(ordersTable.widthProperty().multiply(0.17));
    }


    // =========================================================
    // STATUS COMBOBOX
    // =========================================================

    private void setupStatusBox() {

        statusBox.setItems(
                FXCollections.observableArrayList(
                        "PENDING",
                        "PROCESSING",
                        "READY",
                        "DELIVERED",
                        "CANCELLED"
                )
        );

        statusBox.valueProperty()
                .addListener((obs, oldValue, newValue) -> {

                    updateStatusButtonState();
                });
    }


    private void updateStatusButtonState() {

        boolean noOrderSelected =
                ordersTable == null ||
                        ordersTable.getSelectionModel().getSelectedItem() == null;

        boolean noStatusSelected =
                statusBox == null ||
                        statusBox.getValue() == null;

        if (statusUpdateBtn != null) {
            statusUpdateBtn.setDisable(
                    noOrderSelected || noStatusSelected
            );
        }
    }


    // =========================================================
    // LOAD ORDERS
    // =========================================================

    private void loadOrders() {

        orderList.clear();

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
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Order order = new Order(
                        "ORD-" + rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getString("service_name"),
                        rs.getInt("quantity"),
                        rs.getString("status"),
                        rs.getDouble("total_cost")
                );

                orderList.add(order);
            }

            totalOrdersLabel.setText(
                    String.valueOf(orderList.size())
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Database Error",
                    "Could not load orders."
            );
        }
    }


    // =========================================================
    // ADD ORDER
    // =========================================================

    @FXML
    private void addOrder(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/new-order.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Navigation Error",
                    "Could not open New Order page."
            );
        }
    }


    // =========================================================
    // UPDATE ORDER
    // =========================================================

    @FXML
    private void updateOrder(ActionEvent event) {

        Order selectedOrder =
                ordersTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {

            showWarning(
                    "No Order Selected",
                    "Please select an order first."
            );

            return;
        }

        int orderId =
                extractOrderId(selectedOrder.getOrderId());

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

            controller.setOrderId(orderId);

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Navigation Error",
                    "Could not open Update Order page."
            );
        }
    }


    // =========================================================
    // DELETE ORDER
    // =========================================================

    @FXML
    private void deleteOrder(ActionEvent event) {

        Order selectedOrder =
                ordersTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {

            showWarning(
                    "No Order Selected",
                    "Please select an order first."
            );

            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Delete Order");
        confirmation.setHeaderText(
                "Delete " + selectedOrder.getOrderId() + "?"
        );

        confirmation.setContentText(
                "This action cannot be undone."
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get() != ButtonType.OK) {

            return;
        }

        int orderId =
                extractOrderId(selectedOrder.getOrderId());

        String deleteItemsSql =
                "DELETE FROM order_items WHERE order_id = ?";

        String deleteOrderSql =
                "DELETE FROM orders WHERE id = ?";

        try (Connection conn =
                     DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (
                    PreparedStatement deleteItems =
                            conn.prepareStatement(
                                    deleteItemsSql
                            );

                    PreparedStatement deleteOrder =
                            conn.prepareStatement(
                                    deleteOrderSql
                            )
            ) {

                deleteItems.setInt(1, orderId);
                deleteItems.executeUpdate();

                deleteOrder.setInt(1, orderId);

                int deleted =
                        deleteOrder.executeUpdate();

                if (deleted == 0) {

                    conn.rollback();

                    showError(
                            "Delete Failed",
                            "Order was not found."
                    );

                    return;
                }

                conn.commit();
            }

            showSuccess(
                    "Order Deleted",
                    "Order deleted successfully."
            );

            loadOrders();

            selectedOrderLabel.setText("None");
            statusBox.setValue(null);

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Delete Failed",
                    "Could not delete the order."
            );
        }
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private void searchOrder(ActionEvent event) {

        String search =
                searchField.getText()
                        .trim()
                        .toLowerCase();

        if (search.isEmpty()) {

            ordersTable.setItems(orderList);

            totalOrdersLabel.setText(
                    String.valueOf(orderList.size())
            );

            return;
        }

        ObservableList<Order> filtered =
                FXCollections.observableArrayList();

        for (Order order : orderList) {

            if (
                    safeContains(
                            order.getOrderId(),
                            search
                    )
                            ||
                            safeContains(
                                    order.getCustomer(),
                                    search
                            )
                            ||
                            safeContains(
                                    order.getService(),
                                    search
                            )
                            ||
                            safeContains(
                                    order.getStatus(),
                                    search
                            )
            ) {

                filtered.add(order);
            }
        }

        ordersTable.setItems(filtered);

        totalOrdersLabel.setText(
                String.valueOf(filtered.size())
        );
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @FXML
    private void updateStatus(ActionEvent event) {

        Order selectedOrder =
                ordersTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedOrder == null) {

            showWarning(
                    "No Order Selected",
                    "Please select an order first."
            );

            return;
        }

        String currentStatus =
                selectedOrder.getStatus();

        String newStatus =
                statusBox.getValue();

        if (newStatus == null ||
                newStatus.isBlank()) {

            showWarning(
                    "No Status Selected",
                    "Please select a status."
            );

            return;
        }

        if (currentStatus.equalsIgnoreCase(newStatus)) {

            showWarning(
                    "Same Status",
                    "The order already has this status."
            );

            return;
        }


        // =====================================================
        // STATUS TRANSITION VALIDATION
        // =====================================================

        boolean validTransition = false;

        switch (currentStatus.toUpperCase()) {

            case "PENDING":

                if (newStatus.equalsIgnoreCase("PROCESSING")
                        || newStatus.equalsIgnoreCase("CANCELLED")) {

                    validTransition = true;
                }

                break;


            case "PROCESSING":

                if (newStatus.equalsIgnoreCase("READY")
                        || newStatus.equalsIgnoreCase("CANCELLED")) {

                    validTransition = true;
                }

                break;


            case "READY":

                if (newStatus.equalsIgnoreCase("DELIVERED")) {

                    validTransition = true;
                }

                break;


            case "DELIVERED":

                showWarning(
                        "Order Completed",
                        "A delivered order cannot be changed."
                );

                return;


            case "CANCELLED":

                showWarning(
                        "Order Cancelled",
                        "A cancelled order cannot be changed."
                );

                return;
        }


        // Invalid transition
        if (!validTransition) {

            showWarning(
                    "Invalid Status Change",
                    "Cannot change status from "
                            + currentStatus
                            + " to "
                            + newStatus
                            + "."
            );

            statusBox.setValue(currentStatus);

            return;
        }


        // =====================================================
        // DATABASE UPDATE
        // =====================================================

        int orderId =
                extractOrderId(
                        selectedOrder.getOrderId()
                );

        String sql;

        /*
         * When order becomes DELIVERED,
         * automatically save today's date.
         */
        if ("DELIVERED".equalsIgnoreCase(newStatus)) {

            sql = """
                UPDATE orders
                SET status = ?,
                    delivery_date = date('now')
                WHERE id = ?
                """;

        } else {

            sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                """;
        }


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);

            int updated =
                    ps.executeUpdate();

            if (updated == 0) {

                showError(
                        "Update Failed",
                        "Order was not found."
                );

                return;
            }


            showSuccess(
                    "Status Updated",
                    "Order "
                            + selectedOrder.getOrderId()
                            + " changed from "
                            + currentStatus
                            + " to "
                            + newStatus
                            + "."
            );


            // Refresh table
            loadOrders();

            selectedOrderLabel.setText("None");
            statusBox.setValue(null);

            ordersTable.getSelectionModel()
                    .clearSelection();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Status Update Failed",
                    "Could not update order status."
            );
        }
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void goDashboard(ActionEvent event) {

        try {

            String dashboard;

            if ("STAFF".equalsIgnoreCase(Session.role)) {

                dashboard = "/fxml/staff-dashboard.fxml";

            } else {

                dashboard = "/fxml/admin-dashboard.fxml";
            }

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(dashboard)
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Navigation Error",
                    "Could not open dashboard."
            );
        }
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private int extractOrderId(String orderId) {

        return Integer.parseInt(
                orderId.replace("ORD-", "")
        );
    }


    private boolean safeContains(
            String value,
            String search
    ) {

        return value != null &&
                value.toLowerCase()
                        .contains(search);
    }


    private void showWarning(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showSuccess(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}