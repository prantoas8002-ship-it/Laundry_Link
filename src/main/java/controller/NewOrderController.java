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
import java.time.LocalDate;

public class NewOrderController {

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


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        loadServices();

        if ("CUSTOMER".equals(Session.role)) {
            loadCustomerInformation();
        }
    }


    // =========================================================
    // LOAD ACTIVE SERVICES
    // =========================================================

    private void loadServices() {

        serviceBox.getItems().clear();

        String sql = """
                SELECT name
                FROM services
                WHERE status = 'ACTIVE'
                ORDER BY name
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                serviceBox.getItems().add(
                        rs.getString("name")
                );
            }

            System.out.println(
                    "ACTIVE SERVICES LOADED: "
                            + serviceBox.getItems().size()
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load services from database."
            );
        }
    }


    // =========================================================
    // LOAD LOGGED-IN CUSTOMER INFORMATION
    // =========================================================

    private void loadCustomerInformation() {

        String sql = """
                SELECT name, phone
                FROM customers
                WHERE user_id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, Session.userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    nameField.setText(
                            rs.getString("name")
                    );

                    phoneField.setText(
                            rs.getString("phone")
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load customer information."
            );
        }
    }


    // =========================================================
    // CALCULATE COST
    // =========================================================

    @FXML
    private void calculateCost() {

        try {

            if (serviceBox.getValue() == null
                    || quantityField.getText().trim().isEmpty()) {

                costField.clear();
                return;
            }

            int quantity = Integer.parseInt(
                    quantityField.getText().trim()
            );

            if (quantity <= 0) {

                costField.clear();
                return;
            }

            double price =
                    getServicePrice(
                            serviceBox.getValue()
                    );

            double total = quantity * price;

            costField.setText(
                    String.format(
                            "৳ %.2f",
                            total
                    )
            );

        } catch (NumberFormatException e) {

            costField.clear();

        } catch (Exception e) {

            e.printStackTrace();

            costField.clear();

            showError(
                    "Could not calculate service cost."
            );
        }
    }


    // =========================================================
    // GET SERVICE PRICE
    // =========================================================

    private double getServicePrice(
            String serviceName
    ) throws Exception {

        String sql = """
                SELECT price
                FROM services
                WHERE name = ?
                AND status = 'ACTIVE'
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, serviceName);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getDouble("price");
                }
            }
        }

        throw new Exception(
                "Service price not found."
        );
    }


    // =========================================================
    // CREATE ORDER
    // =========================================================

    @FXML
    private void createOrder(ActionEvent event) {

        // -----------------------------------------------------
        // GET FORM DATA
        // -----------------------------------------------------

        String name =
                nameField.getText().trim();

        String phone =
                phoneField.getText().trim();

        String notes =
                notesArea.getText().trim();

        String serviceName =
                serviceBox.getValue();

        String quantityText =
                quantityField.getText().trim();


        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (name.isEmpty()
                || phone.isEmpty()
                || serviceName == null
                || quantityText.isEmpty()
                || pickupDatePicker.getValue() == null) {

            showWarning(
                    "Please fill all required fields."
            );

            return;
        }


        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            showWarning(
                    "Please enter a valid quantity."
            );

            return;
        }


        if (quantity <= 0) {

            showWarning(
                    "Quantity must be greater than 0."
            );

            return;
        }


        LocalDate pickupDate =
                pickupDatePicker.getValue();


        if (pickupDate.isBefore(LocalDate.now())) {

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
                // 1. GET SERVICE ID + PRICE
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
                                conn.prepareStatement(serviceSql)
                ) {

                    ps.setString(
                            1,
                            serviceName
                    );

                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new Exception(
                                    "Selected service is not available."
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

                double totalCost =
                        quantity * servicePrice;


                // -------------------------------------------------
                // 3. FIND CUSTOMER
                // -------------------------------------------------

                int customerId;


                if ("CUSTOMER".equals(Session.role)) {

                    // =============================================
                    // LOGGED-IN CUSTOMER
                    // =============================================

                    customerId =
                            getLoggedInCustomerId(
                                    conn
                            );


                    // Update latest customer information

                    String updateCustomerSql = """
                            UPDATE customers
                            SET name = ?, phone = ?
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

                } else {

                    // =============================================
                    // ADMIN / STAFF WALK-IN CUSTOMER
                    // =============================================

                    customerId =
                            findOrCreateCustomer(
                                    conn,
                                    name,
                                    phone
                            );
                }


                // -------------------------------------------------
                // 4. INSERT ORDER
                // -------------------------------------------------

                String orderSql = """
                        INSERT INTO orders
                        (
                            customer_id,
                            order_date,
                            pickup_date,
                            status,
                            total_cost,
                            notes
                        )
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;


                int orderId;


                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        orderSql,
                                        java.sql.Statement.RETURN_GENERATED_KEYS
                                )
                ) {

                    ps.setInt(
                            1,
                            customerId
                    );

                    ps.setString(
                            2,
                            LocalDate.now().toString()
                    );

                    ps.setString(
                            3,
                            pickupDate.toString()
                    );

                    ps.setString(
                            4,
                            "PENDING"
                    );

                    ps.setDouble(
                            5,
                            totalCost
                    );

                    ps.setString(
                            6,
                            notes.isEmpty()
                                    ? null
                                    : notes
                    );

                    ps.executeUpdate();


                    try (
                            ResultSet keys =
                                    ps.getGeneratedKeys()
                    ) {

                        if (!keys.next()) {

                            throw new Exception(
                                    "Could not create order."
                            );
                        }

                        orderId =
                                keys.getInt(1);
                    }
                }


                // -------------------------------------------------
                // 5. INSERT ORDER ITEM
                // -------------------------------------------------

                String itemSql = """
                        INSERT INTO order_items
                        (
                            order_id,
                            service_id,
                            quantity,
                            unit_price,
                            subtotal
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """;


                try (
                        PreparedStatement ps =
                                conn.prepareStatement(itemSql)
                ) {

                    ps.setInt(
                            1,
                            orderId
                    );

                    ps.setInt(
                            2,
                            serviceId
                    );

                    ps.setInt(
                            3,
                            quantity
                    );

                    ps.setDouble(
                            4,
                            servicePrice
                    );

                    ps.setDouble(
                            5,
                            totalCost
                    );

                    ps.executeUpdate();
                }


                // -------------------------------------------------
                // 6. COMMIT EVERYTHING
                // -------------------------------------------------

                conn.commit();


                // =================================================
                // SUCCESS MESSAGE
                // =================================================

                Alert alert =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );

                alert.setTitle(
                        "Order Created"
                );

                alert.setHeaderText(
                        "Order Created Successfully"
                );

                alert.setContentText(
                        "Order ID: "
                                + orderId
                                + "\nCustomer: "
                                + name
                                + "\nService: "
                                + serviceName
                                + "\nQuantity: "
                                + quantity
                                + "\nTotal Cost: ৳ "
                                + String.format(
                                "%.2f",
                                totalCost
                        )
                );

                alert.showAndWait();


                // -------------------------------------------------
                // CUSTOMER → CUSTOMER DASHBOARD
                // ADMIN / STAFF → CLEAR FORM
                // -------------------------------------------------

                if ("CUSTOMER".equals(Session.role)) {

                    loadPage(
                            event,
                            "/fxml/customer-dashboard.fxml"
                    );

                } else {

                    clearForm();
                }


            } catch (Exception e) {

                // ---------------------------------------------
                // ANY ERROR → ROLLBACK
                // ---------------------------------------------

                conn.rollback();

                throw e;
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not create order.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // GET LOGGED-IN CUSTOMER ID
    // =========================================================

    private int getLoggedInCustomerId(
            Connection conn
    ) throws Exception {

        String sql = """
                SELECT id
                FROM customers
                WHERE user_id = ?
                """;


        try (
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    Session.userId
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    return rs.getInt("id");
                }
            }
        }


        throw new Exception(
                "Customer profile not found."
        );
    }


    // =========================================================
    // FIND OR CREATE WALK-IN CUSTOMER
    // =========================================================

    private int findOrCreateCustomer(
            Connection conn,
            String name,
            String phone
    ) throws Exception {

        // -----------------------------------------------------
        // FIRST: FIND CUSTOMER BY PHONE
        // -----------------------------------------------------

        String findSql = """
                SELECT id
                FROM customers
                WHERE phone = ?
                """;


        try (
                PreparedStatement ps =
                        conn.prepareStatement(findSql)
        ) {

            ps.setString(
                    1,
                    phone
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    int customerId =
                            rs.getInt("id");


                    // Update latest name

                    String updateSql = """
                            UPDATE customers
                            SET name = ?
                            WHERE id = ?
                            """;


                    try (
                            PreparedStatement update =
                                    conn.prepareStatement(
                                            updateSql
                                    )
                    ) {

                        update.setString(
                                1,
                                name
                        );

                        update.setInt(
                                2,
                                customerId
                        );

                        update.executeUpdate();
                    }


                    return customerId;
                }
            }
        }


        // -----------------------------------------------------
        // CUSTOMER DOES NOT EXIST → CREATE
        // -----------------------------------------------------

        String insertSql = """
                INSERT INTO customers
                (
                    user_id,
                    name,
                    phone
                )
                VALUES (NULL, ?, ?)
                """;


        try (
                PreparedStatement ps =
                        conn.prepareStatement(
                                insertSql,
                                java.sql.Statement.RETURN_GENERATED_KEYS
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

            ps.executeUpdate();


            try (
                    ResultSet keys =
                            ps.getGeneratedKeys()
            ) {

                if (keys.next()) {

                    return keys.getInt(1);
                }
            }
        }


        throw new Exception(
                "Could not create customer."
        );
    }


    // =========================================================
    // CLEAR FORM
    // =========================================================

    @FXML
    private void clearForm() {

        // Keep customer information for logged-in customer

        if (!"CUSTOMER".equals(Session.role)) {

            nameField.clear();
            phoneField.clear();
        }

        quantityField.clear();

        costField.clear();

        notesArea.clear();

        serviceBox
                .getSelectionModel()
                .clearSelection();

        pickupDatePicker.setValue(null);
    }


    // =========================================================
    // GO DASHBOARD
    // =========================================================

    @FXML
    private void goDashboard(
            ActionEvent event
    ) {

        switch (Session.role) {

            case "ADMIN":

                loadPage(
                        event,
                        "/fxml/admin-dashboard.fxml"
                );

                break;


            case "STAFF":

                loadPage(
                        event,
                        "/fxml/staff-dashboard.fxml"
                );

                break;


            case "CUSTOMER":

                loadPage(
                        event,
                        "/fxml/customer-dashboard.fxml"
                );

                break;


            default:

                loadPage(
                        event,
                        "/fxml/login.fxml"
                );

                break;
        }
    }


    // =========================================================
    // LOAD PAGE
    // =========================================================

    private void loadPage(
            ActionEvent event,
            String fxmlPath
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    fxmlPath
                            )
                    );


            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            Scene scene =
                    new Scene(root);


            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();


        } catch (IOException e) {

            e.printStackTrace();


            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );


            alert.setTitle(
                    "Navigation Error"
            );

            alert.setHeaderText(null);

            alert.setContentText(
                    "Could not load:\n"
                            + fxmlPath
            );

            alert.showAndWait();
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
    // ERROR
    // =========================================================

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Error"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}