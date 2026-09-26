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
import model.CustomerHistory;
import model.CustomerOrderHistory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerHistoryController {

    // =========================================================
    // SEARCH / LABELS
    // =========================================================

    @FXML
    private TextField searchField;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private Label selectedCustomerLabel;

    @FXML
    private Label selectedTotalOrdersLabel;

    @FXML
    private Label selectedTotalSpentLabel;

    @FXML
    private Label orderHistoryHintLabel;


    // =========================================================
    // CUSTOMER SUMMARY TABLE
    // =========================================================

    @FXML
    private TableView<CustomerHistory> customerTable;

    @FXML
    private TableColumn<CustomerHistory, String> customerIdCol;

    @FXML
    private TableColumn<CustomerHistory, String> customerNameCol;

    @FXML
    private TableColumn<CustomerHistory, String> phoneCol;

    @FXML
    private TableColumn<CustomerHistory, String> emailCol;

    @FXML
    private TableColumn<CustomerHistory, Integer> totalOrdersCol;

    @FXML
    private TableColumn<CustomerHistory, Double> totalSpentCol;


    // =========================================================
    // ORDER HISTORY TABLE
    // =========================================================

    @FXML
    private TableView<CustomerOrderHistory> orderHistoryTable;

    @FXML
    private TableColumn<CustomerOrderHistory, String> orderIdCol;

    @FXML
    private TableColumn<CustomerOrderHistory, String> serviceCol;

    @FXML
    private TableColumn<CustomerOrderHistory, Integer> quantityCol;

    @FXML
    private TableColumn<CustomerOrderHistory, Double> amountCol;

    @FXML
    private TableColumn<CustomerOrderHistory, String> dateCol;

    @FXML
    private TableColumn<CustomerOrderHistory, String> statusCol;


    // =========================================================
    // LISTS
    // =========================================================

    private final ObservableList<CustomerHistory> customerList =
            FXCollections.observableArrayList();

    private final ObservableList<CustomerOrderHistory> orderHistoryList =
            FXCollections.observableArrayList();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        setupCustomerColumns();

        setupOrderHistoryColumns();

        customerTable.setItems(customerList);

        orderHistoryTable.setItems(orderHistoryList);

        makeCustomerColumnsEqualWidth();

        makeOrderColumnsEqualWidth();

        loadCustomers();

        loadTotalCustomers();

        setupCustomerSelectionListener();
    }


    // =========================================================
    // CUSTOMER TABLE COLUMNS
    // =========================================================

    private void setupCustomerColumns() {

        customerIdCol.setCellValueFactory(
                new PropertyValueFactory<>("customerId")
        );

        customerNameCol.setCellValueFactory(
                new PropertyValueFactory<>("customerName")
        );

        phoneCol.setCellValueFactory(
                new PropertyValueFactory<>("phone")
        );

        emailCol.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        totalOrdersCol.setCellValueFactory(
                new PropertyValueFactory<>("totalOrders")
        );

        totalSpentCol.setCellValueFactory(
                new PropertyValueFactory<>("totalSpent")
        );


        // Currency formatting
        totalSpentCol.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Double amount,
                            boolean empty
                    ) {

                        super.updateItem(amount, empty);

                        if (empty || amount == null) {

                            setText(null);

                        } else {

                            setText(
                                    "৳" +
                                            String.format(
                                                    "%.2f",
                                                    amount
                                            )
                            );
                        }
                    }
                }
        );
    }


    // =========================================================
    // ORDER HISTORY COLUMNS
    // =========================================================

    private void setupOrderHistoryColumns() {

        orderIdCol.setCellValueFactory(
                new PropertyValueFactory<>("orderId")
        );

        serviceCol.setCellValueFactory(
                new PropertyValueFactory<>("service")
        );

        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        amountCol.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date")
        );

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );


        // Currency formatting
        amountCol.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Double amount,
                            boolean empty
                    ) {

                        super.updateItem(amount, empty);

                        if (empty || amount == null) {

                            setText(null);

                        } else {

                            setText(
                                    "৳" +
                                            String.format(
                                                    "%.2f",
                                                    amount
                                            )
                            );
                        }
                    }
                }
        );


        // Status formatting
        statusCol.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String status,
                            boolean empty
                    ) {

                        super.updateItem(status, empty);

                        if (empty || status == null) {

                            setText(null);

                        } else {

                            setText(status);
                        }
                    }
                }
        );
    }


    // =========================================================
    // LOAD UNIQUE CUSTOMERS
    // =========================================================

    private void loadCustomers() {

        customerList.clear();

        String sql =
                """
                SELECT
                    c.id AS customer_id,
                    c.name AS customer_name,
                    c.phone AS phone,
                    c.email AS email,

                    COUNT(o.id) AS total_orders,

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

                FROM customers c

                LEFT JOIN orders o
                    ON c.id = o.customer_id

                GROUP BY
                    c.id,
                    c.name,
                    c.phone,
                    c.email

                ORDER BY c.id DESC
                """;


        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                CustomerHistory customer =
                        new CustomerHistory(

                                "C" +
                                        String.format(
                                                "%03d",
                                                rs.getInt(
                                                        "customer_id"
                                                )
                                        ),

                                rs.getString(
                                        "customer_name"
                                ),

                                rs.getString(
                                        "phone"
                                ),

                                rs.getString(
                                        "email"
                                ),

                                rs.getInt(
                                        "total_orders"
                                ),

                                rs.getDouble(
                                        "total_spent"
                                )
                        );

                customerList.add(customer);
            }

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Could not load customers."
            );
        }
    }


    // =========================================================
    // TOTAL CUSTOMERS
    // =========================================================

    private void loadTotalCustomers() {

        String sql =
                "SELECT COUNT(*) FROM customers";


        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            if (rs.next()) {

                totalCustomersLabel.setText(
                        String.valueOf(
                                rs.getInt(1)
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            totalCustomersLabel.setText("0");
        }
    }


    // =========================================================
    // CUSTOMER SELECTION
    // =========================================================

    private void setupCustomerSelectionListener() {

        customerTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldValue, selected) -> {

                            if (selected == null) {

                                clearSelectedCustomer();

                                return;
                            }


                            String customerId =
                                    selected.getCustomerId();

                            String customerName =
                                    selected.getCustomerName();


                            selectedCustomerLabel.setText(
                                    customerName
                            );

                            selectedTotalOrdersLabel.setText(
                                    String.valueOf(
                                            selected.getTotalOrders()
                                    )
                            );

                            selectedTotalSpentLabel.setText(
                                    "৳" +
                                            String.format(
                                                    "%.2f",
                                                    selected.getTotalSpent()
                                            )
                            );


                            orderHistoryHintLabel.setText(
                                    "Order history for " +
                                            customerName
                            );


                            loadOrderHistory(
                                    customerId
                            );
                        }
                );
    }


    // =========================================================
    // LOAD SELECTED CUSTOMER ORDERS
    // =========================================================

    private void loadOrderHistory(
            String displayCustomerId
    ) {

        orderHistoryList.clear();


        int customerId;


        try {

            customerId =
                    Integer.parseInt(
                            displayCustomerId.substring(1)
                    );

        } catch (Exception e) {

            return;
        }


        String sql =
                """
                SELECT
                    o.id AS order_id,
                    s.name AS service_name,
                    oi.quantity AS quantity,
                    oi.subtotal AS amount,
                    o.order_date AS order_date,
                    o.status AS status

                FROM orders o

                JOIN order_items oi
                    ON o.id = oi.order_id

                JOIN services s
                    ON oi.service_id = s.id

                WHERE o.customer_id = ?

                ORDER BY o.id DESC
                """;


        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    customerId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    CustomerOrderHistory order =
                            new CustomerOrderHistory(

                                    "ORD-" +
                                            rs.getInt(
                                                    "order_id"
                                            ),

                                    rs.getString(
                                            "service_name"
                                    ),

                                    rs.getInt(
                                            "quantity"
                                    ),

                                    rs.getDouble(
                                            "amount"
                                    ),

                                    rs.getString(
                                            "order_date"
                                    ),

                                    rs.getString(
                                            "status"
                                    )
                            );

                    orderHistoryList.add(order);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Could not load order history."
            );
        }
    }


    // =========================================================
    // SEARCH CUSTOMER
    // =========================================================

    @FXML
    private void searchCustomer(
            ActionEvent event
    ) {

        String keyword =
                searchField.getText()
                        .trim()
                        .toLowerCase();


        if (keyword.isEmpty()) {

            customerTable.setItems(
                    customerList
            );

            return;
        }


        ObservableList<CustomerHistory> filtered =
                FXCollections.observableArrayList();


        for (CustomerHistory customer :
                customerList) {

            String customerId =
                    customer.getCustomerId() == null
                            ? ""
                            : customer.getCustomerId().toLowerCase();


            String customerName =
                    customer.getCustomerName() == null
                            ? ""
                            : customer.getCustomerName().toLowerCase();


            String phone =
                    customer.getPhone() == null
                            ? ""
                            : customer.getPhone().toLowerCase();


            if (
                    customerId.contains(keyword)
                            ||
                            customerName.contains(keyword)
                            ||
                            phone.contains(keyword)
            ) {

                filtered.add(customer);
            }
        }


        customerTable.setItems(filtered);

        clearSelectedCustomer();
    }


    // =========================================================
    // CLEAR SEARCH
    // =========================================================

    @FXML
    private void clearSearch(
            ActionEvent event
    ) {

        searchField.clear();

        customerTable.setItems(
                customerList
        );

        customerTable
                .getSelectionModel()
                .clearSelection();

        clearSelectedCustomer();
    }


    // =========================================================
    // CLEAR SELECTED CUSTOMER
    // =========================================================

    private void clearSelectedCustomer() {

        selectedCustomerLabel.setText(
                "None"
        );

        selectedTotalOrdersLabel.setText(
                "0"
        );

        selectedTotalSpentLabel.setText(
                "৳0"
        );

        orderHistoryHintLabel.setText(
                "Select a customer above"
        );

        orderHistoryList.clear();
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void goToDashboard(
            ActionEvent event
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/fxml/admin-dashboard.fxml"
                            )
                    );


            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();


            stage.setScene(
                    new Scene(root)
            );


            stage.setTitle(
                    "LaundryLink Dashboard"
            );


            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            showAlert(
                    "Dashboard page not found."
            );
        }
    }


    // =========================================================
    // CUSTOMER TABLE WIDTH
    // =========================================================

    private void makeCustomerColumnsEqualWidth() {

        int columnCount = 6;

        double borderPadding = 2.0;


        customerIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        customerTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        customerNameCol.prefWidthProperty().bind(
                Bindings.divide(
                        customerTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        phoneCol.prefWidthProperty().bind(
                Bindings.divide(
                        customerTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        emailCol.prefWidthProperty().bind(
                Bindings.divide(
                        customerTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        totalOrdersCol.prefWidthProperty().bind(
                Bindings.divide(
                        customerTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        totalSpentCol.prefWidthProperty().bind(
                Bindings.divide(
                        customerTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );


        customerIdCol.setResizable(false);
        customerNameCol.setResizable(false);
        phoneCol.setResizable(false);
        emailCol.setResizable(false);
        totalOrdersCol.setResizable(false);
        totalSpentCol.setResizable(false);
    }


    // =========================================================
    // ORDER TABLE WIDTH
    // =========================================================

    private void makeOrderColumnsEqualWidth() {

        int columnCount = 6;

        double borderPadding = 2.0;


        orderIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        orderHistoryTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        serviceCol.prefWidthProperty().bind(
                Bindings.divide(
                        orderHistoryTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        quantityCol.prefWidthProperty().bind(
                Bindings.divide(
                        orderHistoryTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        amountCol.prefWidthProperty().bind(
                Bindings.divide(
                        orderHistoryTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        dateCol.prefWidthProperty().bind(
                Bindings.divide(
                        orderHistoryTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );

        statusCol.prefWidthProperty().bind(
                Bindings.divide(
                        orderHistoryTable.widthProperty()
                                .subtract(borderPadding),
                        columnCount
                )
        );


        orderIdCol.setResizable(false);
        serviceCol.setResizable(false);
        quantityCol.setResizable(false);
        amountCol.setResizable(false);
        dateCol.setResizable(false);
        statusCol.setResizable(false);
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Customer History"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}