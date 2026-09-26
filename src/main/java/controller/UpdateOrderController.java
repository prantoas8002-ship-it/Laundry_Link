package controller;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Session;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class UpdateOrderController {

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
    private Button dashboardBtn;

    @FXML
    private Button createOrderBtn;

    @FXML
    private Button clearBtn;


    // Selected order ID
    private int orderId;

    // Current service price
    private double currentServicePrice = 0;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        loadServices();

        serviceBox.setOnAction(event -> calculateCost());

        quantityField.textProperty().addListener(
                (observable, oldValue, newValue) -> calculateCost()
        );
    }


    // =========================================================
    // SET ORDER ID
    // =========================================================

    public void setOrderId(int orderId) {

        this.orderId = orderId;

        System.out.println(
                "Updating Order ID: " + orderId
        );

        loadOrderData();
    }


    // =========================================================
    // LOAD SERVICES
    // =========================================================

    private void loadServices() {

        String sql = """
                SELECT name
                FROM services
                WHERE status = 'ACTIVE'
                ORDER BY id
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            serviceBox.getItems().clear();

            while (rs.next()) {

                serviceBox.getItems().add(
                        rs.getString("name")
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    "Database Error",
                    "Could not load services.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD EXISTING ORDER
    // =========================================================

    private void loadOrderData() {

        String sql = """
                SELECT
                    o.id AS order_id,
                    o.pickup_date,
                    o.status,
                    o.notes,

                    c.name AS customer_name,
                    c.phone AS customer_phone,

                    s.name AS service_name,

                    oi.quantity AS quantity

                FROM orders o

                JOIN customers c
                    ON o.customer_id = c.id

                JOIN order_items oi
                    ON o.id = oi.order_id

                JOIN services s
                    ON oi.service_id = s.id

                WHERE o.id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    // Customer
                    nameField.setText(
                            rs.getString("customer_name")
                    );

                    phoneField.setText(
                            rs.getString("customer_phone")
                    );


                    // Service
                    serviceBox.setValue(
                            rs.getString("service_name")
                    );


                    // Quantity
                    quantityField.setText(
                            String.valueOf(
                                    rs.getInt("quantity")
                            )
                    );


                    // Pickup date
                    String pickupDate =
                            rs.getString("pickup_date");

                    if (pickupDate != null &&
                            !pickupDate.isBlank()) {

                        try {

                            pickupDatePicker.setValue(
                                    LocalDate.parse(pickupDate)
                            );

                        } catch (Exception e) {

                            System.out.println(
                                    "Invalid pickup date: "
                                            + pickupDate
                            );
                        }
                    }


                    // Notes
                    String notes =
                            rs.getString("notes");

                    if (notes != null) {
                        notesArea.setText(notes);
                    } else {
                        notesArea.clear();
                    }


                    // Calculate cost
                    calculateCost();

                } else {

                    showAlert(
                            "Order Not Found",
                            "The selected order could not be found."
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    "Database Error",
                    "Could not load order information.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CALCULATE COST
    // =========================================================

    @FXML
    private void calculateCost() {

        String service = serviceBox.getValue();
        String quantityText =
                quantityField.getText().trim();

        if (service == null ||
                quantityText.isEmpty()) {

            costField.setText("");
            currentServicePrice = 0;
            return;
        }

        int quantity;

        try {

            quantity = Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            costField.setText("");
            return;
        }

        if (quantity <= 0) {

            costField.setText("");
            return;
        }


        String sql = """
                SELECT price
                FROM services
                WHERE name = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, service);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    currentServicePrice =
                            rs.getDouble("price");

                    double total =
                            currentServicePrice * quantity;

                    costField.setText(
                            String.format(
                                    "%.2f",
                                    total
                            )
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            costField.setText("");
        }
    }


    // =========================================================
    // UPDATE ORDER
    // =========================================================

    @FXML
    private void createOrder(ActionEvent event) {

        String name =
                nameField.getText().trim();

        String phone =
                phoneField.getText().trim();

        String service =
                serviceBox.getValue();

        String quantityText =
                quantityField.getText().trim();

        LocalDate pickupDate =
                pickupDatePicker.getValue();

        String notes =
                notesArea.getText().trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (name.isEmpty()) {

            showAlert(
                    "Validation Error",
                    "Customer name is required."
            );

            return;
        }


        if (phone.isEmpty()) {

            showAlert(
                    "Validation Error",
                    "Phone number is required."
            );

            return;
        }


        if (service == null ||
                service.isBlank()) {

            showAlert(
                    "Validation Error",
                    "Please select a service."
            );

            return;
        }


        if (quantityText.isEmpty()) {

            showAlert(
                    "Validation Error",
                    "Please enter quantity."
            );

            return;
        }


        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            showAlert(
                    "Validation Error",
                    "Quantity must be a valid number."
            );

            return;
        }


        if (quantity <= 0) {

            showAlert(
                    "Validation Error",
                    "Quantity must be greater than 0."
            );

            return;
        }


        if (pickupDate == null) {

            showAlert(
                    "Validation Error",
                    "Please select a pickup date."
            );

            return;
        }


        // =====================================================
        // GET SERVICE ID AND PRICE
        // =====================================================

        int serviceId;
        double servicePrice;

        String serviceSql = """
                SELECT id, price
                FROM services
                WHERE name = ?
                AND status = 'ACTIVE'
                """;


        // =====================================================
        // DATABASE TRANSACTION
        // =====================================================

        try (Connection conn =
                     DBConnection.getConnection()) {

            conn.setAutoCommit(false);


            try {

                // ---------------------------------------------
                // 1. Get service information
                // ---------------------------------------------

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(serviceSql)
                ) {

                    ps.setString(1, service);

                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new SQLException(
                                    "Selected service was not found."
                            );
                        }

                        serviceId =
                                rs.getInt("id");

                        servicePrice =
                                rs.getDouble("price");
                    }
                }


                // ---------------------------------------------
                // 2. Calculate new total
                // ---------------------------------------------

                double subtotal =
                        servicePrice * quantity;


                // ---------------------------------------------
                // 3. Get customer ID from order
                // ---------------------------------------------

                int customerId;

                String getCustomerSql = """
                        SELECT customer_id
                        FROM orders
                        WHERE id = ?
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        getCustomerSql
                                )
                ) {

                    ps.setInt(1, orderId);

                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new SQLException(
                                    "Order not found."
                            );
                        }

                        customerId =
                                rs.getInt("customer_id");
                    }
                }


                // ---------------------------------------------
                // 4. Update customer information
                // ---------------------------------------------

                String updateCustomerSql = """
                        UPDATE customers
                        SET name = ?,
                            phone = ?
                        WHERE id = ?
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        updateCustomerSql
                                )
                ) {

                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.setInt(3, customerId);

                    ps.executeUpdate();
                }


                // ---------------------------------------------
                // 5. Update order
                // ---------------------------------------------

                String updateOrderSql = """
                        UPDATE orders
                        SET pickup_date = ?,
                            total_cost = ?,
                            notes = ?
                        WHERE id = ?
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        updateOrderSql
                                )
                ) {

                    ps.setString(
                            1,
                            pickupDate.toString()
                    );

                    ps.setDouble(
                            2,
                            subtotal
                    );

                    ps.setString(
                            3,
                            notes
                    );

                    ps.setInt(
                            4,
                            orderId
                    );

                    ps.executeUpdate();
                }


                // ---------------------------------------------
                // 6. Update order item
                // ---------------------------------------------

                String updateItemSql = """
                        UPDATE order_items
                        SET service_id = ?,
                            quantity = ?,
                            unit_price = ?,
                            subtotal = ?
                        WHERE order_id = ?
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        updateItemSql
                                )
                ) {

                    ps.setInt(1, serviceId);

                    ps.setInt(2, quantity);

                    ps.setDouble(
                            3,
                            servicePrice
                    );

                    ps.setDouble(
                            4,
                            subtotal
                    );

                    ps.setInt(
                            5,
                            orderId
                    );

                    ps.executeUpdate();
                }


                // ---------------------------------------------
                // 7. Commit
                // ---------------------------------------------

                conn.commit();


                // ---------------------------------------------
                // 8. Success
                // ---------------------------------------------

                showAlert(
                        "Success",
                        "Order updated successfully."
                );


                // Back to Order Management
                goOrderManagement();


            } catch (Exception e) {

                // Rollback if anything fails
                conn.rollback();

                throw e;
            }

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Update Failed",
                    "Could not update order.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CLEAR FORM
    // =========================================================

    @FXML
    private void clearForm() {

        nameField.clear();

        phoneField.clear();

        serviceBox.setValue(null);

        quantityField.clear();

        pickupDatePicker.setValue(null);

        costField.clear();

        notesArea.clear();
    }


    // =========================================================
    // GO TO ORDER MANAGEMENT
    // =========================================================

    private void goOrderManagement() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/order-management.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) nameField
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
                    "Navigation Error",
                    "Could not open Order Management."
            );
        }
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void goDashboard(ActionEvent event) {

        try {

            String dashboardPath;

            if (Session.role != null &&
                    Session.role.equalsIgnoreCase("ADMIN")) {

                dashboardPath =
                        "/fxml/admin-dashboard.fxml";

            } else if (
                    Session.role != null &&
                            Session.role.equalsIgnoreCase("STAFF")
            ) {

                dashboardPath =
                        "/fxml/staff-dashboard.fxml";

            } else {

                dashboardPath =
                        "/fxml/customer-dashboard.fxml";
            }


            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    dashboardPath
                            )
                    );

            Parent root =
                    loader.load();


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
                    "Navigation Error",
                    "Could not open dashboard.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // ALERT
    // =========================================================

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