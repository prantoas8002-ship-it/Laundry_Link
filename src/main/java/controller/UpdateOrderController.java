package controller;

import database.DBConnection;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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


    private int orderId;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        loadServices();

        serviceBox.setOnAction(
                event -> calculateCost()
        );


        quantityField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                calculateCost()
                );
    }


    // =========================================================
    // SET ORDER ID
    // =========================================================

    public void setOrderId(
            int orderId
    ) {

        this.orderId = orderId;

        System.out.println(
                "Updating Order ID: "
                        + orderId
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
                ORDER BY name
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            serviceBox.getItems().clear();


            while (rs.next()) {

                serviceBox.getItems().add(
                        rs.getString("name")
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();

            showError(
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
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    orderId
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {

                    showError(
                            "Order Not Found",
                            "The selected order could not be found."
                    );

                    return;
                }


                // -------------------------------------------------
                // CUSTOMER
                // -------------------------------------------------

                nameField.setText(
                        rs.getString(
                                "customer_name"
                        )
                );


                phoneField.setText(
                        rs.getString(
                                "customer_phone"
                        )
                );


                // -------------------------------------------------
                // SERVICE
                // -------------------------------------------------

                serviceBox.setValue(
                        rs.getString(
                                "service_name"
                        )
                );


                // -------------------------------------------------
                // QUANTITY
                // -------------------------------------------------

                quantityField.setText(
                        String.valueOf(
                                rs.getInt(
                                        "quantity"
                                )
                        )
                );


                // -------------------------------------------------
                // PICKUP DATE
                // -------------------------------------------------

                String pickupDate =
                        rs.getString(
                                "pickup_date"
                        );


                if (pickupDate != null
                        && !pickupDate.isBlank()) {

                    try {

                        pickupDatePicker.setValue(
                                LocalDate.parse(
                                        pickupDate
                                )
                        );

                    } catch (Exception e) {

                        System.out.println(
                                "Invalid pickup date: "
                                        + pickupDate
                        );
                    }
                }


                // -------------------------------------------------
                // NOTES
                // -------------------------------------------------

                String notes =
                        rs.getString("notes");


                if (notes != null) {

                    notesArea.setText(
                            notes
                    );

                } else {

                    notesArea.clear();
                }


                // -------------------------------------------------
                // COST
                // -------------------------------------------------

                calculateCost();
            }


        } catch (SQLException e) {

            e.printStackTrace();

            showError(
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

        String service =
                serviceBox.getValue();

        String quantityText =
                quantityField.getText().trim();


        if (service == null
                || quantityText.isEmpty()) {

            costField.clear();

            return;
        }


        int quantity;


        try {

            quantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            costField.clear();

            return;
        }


        if (quantity <= 0) {

            costField.clear();

            return;
        }


        String sql = """
                SELECT price
                FROM services
                WHERE name = ?
                AND status = 'ACTIVE'
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    service
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    double price =
                            rs.getDouble("price");


                    double total =
                            price * quantity;


                    costField.setText(
                            String.format(
                                    "৳ %.2f",
                                    total
                            )
                    );

                } else {

                    costField.clear();
                }
            }


        } catch (SQLException e) {

            e.printStackTrace();

            costField.clear();
        }
    }


    // =========================================================
    // UPDATE ORDER
    // =========================================================

    @FXML
    private void createOrder(
            ActionEvent event
    ) {

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

            showWarning(
                    "Customer name is required."
            );

            return;
        }


        if (phone.isEmpty()) {

            showWarning(
                    "Phone number is required."
            );

            return;
        }


        if (service == null
                || service.isBlank()) {

            showWarning(
                    "Please select a service."
            );

            return;
        }


        if (quantityText.isEmpty()) {

            showWarning(
                    "Please enter quantity."
            );

            return;
        }


        int quantity;


        try {

            quantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            showWarning(
                    "Quantity must be a valid number."
            );

            return;
        }


        if (quantity <= 0) {

            showWarning(
                    "Quantity must be greater than 0."
            );

            return;
        }


        if (pickupDate == null) {

            showWarning(
                    "Please select a pickup date."
            );

            return;
        }


        if (pickupDate.isBefore(
                LocalDate.now()
        )) {

            showWarning(
                    "Pickup date cannot be before today."
            );

            return;
        }


        // =====================================================
        // DATABASE TRANSACTION
        // =====================================================

        try (
                Connection conn =
                        DBConnection.getConnection()
        ) {

            conn.setAutoCommit(false);


            try {

                // -------------------------------------------------
                // 1. GET SERVICE
                // -------------------------------------------------

                int serviceId;

                double servicePrice;


                String serviceSql = """
                        SELECT id, price
                        FROM services
                        WHERE name = ?
                        AND status = 'ACTIVE'
                        """;


                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        serviceSql
                                )
                ) {

                    ps.setString(
                            1,
                            service
                    );


                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new Exception(
                                    "Selected service is no longer active."
                            );
                        }


                        serviceId =
                                rs.getInt("id");

                        servicePrice =
                                rs.getDouble("price");
                    }
                }


                // -------------------------------------------------
                // 2. CALCULATE TOTAL
                // -------------------------------------------------

                double subtotal =
                        servicePrice * quantity;


                // -------------------------------------------------
                // 3. GET CUSTOMER ID
                // -------------------------------------------------

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

                    ps.setInt(
                            1,
                            orderId
                    );


                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new Exception(
                                    "Order not found."
                            );
                        }


                        customerId =
                                rs.getInt(
                                        "customer_id"
                                );
                    }
                }


                // -------------------------------------------------
                // 4. UPDATE CUSTOMER
                // -------------------------------------------------

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

                    ps.setString(
                            1,
                            name
                    );

                    ps.setString(
                            2,
                            phone
                    );

                    ps.setInt(
                            3,
                            customerId
                    );

                    ps.executeUpdate();
                }


                // -------------------------------------------------
                // 5. UPDATE ORDER
                // -------------------------------------------------

                String updateOrderSql = """
                        UPDATE orders
                        SET pickup_date = ?,
                            total_cost = ?,
                            notes = ?
                        WHERE id = ?
                        """;


                int updatedOrders;


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
                            notes.isEmpty()
                                    ? null
                                    : notes
                    );

                    ps.setInt(
                            4,
                            orderId
                    );


                    updatedOrders =
                            ps.executeUpdate();
                }


                if (updatedOrders == 0) {

                    throw new Exception(
                            "Order could not be updated."
                    );
                }


                // -------------------------------------------------
                // 6. UPDATE ORDER ITEM
                // -------------------------------------------------

                String updateItemSql = """
                        UPDATE order_items
                        SET service_id = ?,
                            quantity = ?,
                            unit_price = ?,
                            subtotal = ?
                        WHERE order_id = ?
                        """;


                int updatedItems;


                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        updateItemSql
                                )
                ) {

                    ps.setInt(
                            1,
                            serviceId
                    );

                    ps.setInt(
                            2,
                            quantity
                    );

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


                    updatedItems =
                            ps.executeUpdate();
                }


                if (updatedItems == 0) {

                    throw new Exception(
                            "Order item could not be updated."
                    );
                }


                // -------------------------------------------------
                // 7. COMMIT
                // -------------------------------------------------

                conn.commit();


                // -------------------------------------------------
                // 8. SUCCESS
                // -------------------------------------------------

                showSuccess(
                        "Order updated successfully."
                );


                // Go back to Order Management

                goOrderManagement();


            } catch (Exception e) {

                conn.rollback();

                throw e;
            }


        } catch (Exception e) {

            e.printStackTrace();

            showError(
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

        serviceBox.getSelectionModel()
                .clearSelection();

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

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/order-management.fxml"
                            )
                    );


            Stage stage =
                    (Stage)
                            nameField.getScene()
                                    .getWindow();


            stage.setScene(
                    new Scene(root)
            );

            stage.setMaximized(true);

            stage.show();


        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Navigation Error",
                    "Could not open Order Management."
            );
        }
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void goDashboard(
            ActionEvent event
    ) {

        String dashboardPath;


        if ("ADMIN".equalsIgnoreCase(
                Session.role
        )) {

            dashboardPath =
                    "/fxml/admin-dashboard.fxml";

        } else if ("STAFF".equalsIgnoreCase(
                Session.role
        )) {

            dashboardPath =
                    "/fxml/staff-dashboard.fxml";

        } else {

            dashboardPath =
                    "/fxml/customer-dashboard.fxml";
        }


        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    dashboardPath
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

            showError(
                    "Navigation Error",
                    "Could not open dashboard.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // WARNING
    // =========================================================

    private void showWarning(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Validation Error"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }


    // =========================================================
    // SUCCESS
    // =========================================================

    private void showSuccess(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Success"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}