package controller;

import database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerOrdersController {


    // =========================================================
    // FXML COMPONENTS
    // =========================================================

    @FXML
    private VBox ordersContainer;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label activeOrdersLabel;

    @FXML
    private Label completedOrdersLabel;

    @FXML
    private Label orderCountLabel;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        loadOrders();
    }


    // =========================================================
    // LOAD CUSTOMER ORDERS
    // =========================================================

    private void loadOrders() {

        ordersContainer.getChildren().clear();

        String sql = """
                SELECT
                    o.id AS order_id,
                    o.order_date,
                    o.pickup_date,
                    o.delivery_date,
                    o.status,
                    o.total_cost,
                    o.notes,

                    s.name AS service_name,

                    oi.quantity,
                    oi.unit_price,
                    oi.subtotal

                FROM orders o

                JOIN customers c
                    ON o.customer_id = c.id

                LEFT JOIN order_items oi
                    ON o.id = oi.order_id

                LEFT JOIN services s
                    ON oi.service_id = s.id

                WHERE c.user_id = ?

                ORDER BY o.id DESC
                """;


        int totalOrders = 0;
        int activeOrders = 0;
        int completedOrders = 0;


        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            /*
             * IMPORTANT:
             *
             * Session.userId is the currently logged-in
             * customer's users.id.
             *
             * Therefore only that customer's orders
             * will be returned.
             */

            ps.setInt(1, Session.userId);

            ResultSet rs = ps.executeQuery();


            while (rs.next()) {

                totalOrders++;

                String status =
                        rs.getString("status");

                if (status != null) {

                    if (status.equalsIgnoreCase("DELIVERED")) {

                        completedOrders++;

                    } else if (!status.equalsIgnoreCase("CANCELLED")) {

                        activeOrders++;
                    }
                }


                // Create order card

                VBox orderCard =
                        createOrderCard(rs);

                ordersContainer
                        .getChildren()
                        .add(orderCard);
            }


            // Update summary

            totalOrdersLabel.setText(
                    String.valueOf(totalOrders)
            );

            activeOrdersLabel.setText(
                    String.valueOf(activeOrders)
            );

            completedOrdersLabel.setText(
                    String.valueOf(completedOrders)
            );


            orderCountLabel.setText(
                    totalOrders
                            + (totalOrders == 1
                            ? " order"
                            : " orders")
            );


            // No orders

            if (totalOrders == 0) {

                VBox emptyCard =
                        createEmptyOrderCard();

                ordersContainer
                        .getChildren()
                        .add(emptyCard);
            }


        } catch (Exception e) {

            e.printStackTrace();

            totalOrdersLabel.setText("0");
            activeOrdersLabel.setText("0");
            completedOrdersLabel.setText("0");
            orderCountLabel.setText("0 orders");

            showError(
                    "Could not load your orders."
            );
        }
    }


    // =========================================================
    // CREATE ORDER CARD
    // =========================================================

    private VBox createOrderCard(ResultSet rs)
            throws Exception {

        VBox card = new VBox();

        card.setSpacing(16);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #E3EAF0;"
        );


        card.setPadding(
                new javafx.geometry.Insets(
                        22, 25, 22, 25
                )
        );


        // =====================================================
        // TOP ROW
        // =====================================================

        HBox topRow = new HBox();

        topRow.setSpacing(15);

        topRow.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );


        VBox orderInfo = new VBox();

        orderInfo.setSpacing(4);

        HBox.setHgrow(
                orderInfo,
                javafx.scene.layout.Priority.ALWAYS
        );


        int orderId =
                rs.getInt("order_id");


        Label orderIdLabel =
                new Label(
                        "Order #" + orderId
                );

        orderIdLabel.setStyle(
                "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );


        String orderDate =
                getSafeValue(
                        rs.getString("order_date")
                );


        Label orderDateLabel =
                new Label(
                        "Placed: " + orderDate
                );

        orderDateLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #8A98A8;"
        );


        orderInfo
                .getChildren()
                .addAll(
                        orderIdLabel,
                        orderDateLabel
                );


        // Status

        String status =
                getSafeValue(
                        rs.getString("status")
                );


        Label statusLabel =
                new Label(
                        status
                );

        statusLabel.setStyle(
                getStatusStyle(status)
        );


        topRow
                .getChildren()
                .addAll(
                        orderInfo,
                        statusLabel
                );


        // =====================================================
        // SEPARATOR
        // =====================================================

        javafx.scene.control.Separator separator =
                new javafx.scene.control.Separator();


        // =====================================================
        // ORDER DETAILS
        // =====================================================

        HBox detailsRow =
                new HBox();

        detailsRow.setSpacing(35);


        String service =
                getSafeValue(
                        rs.getString("service_name")
                );


        int quantity =
                rs.getInt("quantity");


        double totalCost =
                rs.getDouble("total_cost");


        if (totalCost == 0) {

            double subtotal =
                    rs.getDouble("subtotal");

            totalCost = subtotal;
        }


        String pickupDate =
                getSafeValue(
                        rs.getString("pickup_date")
                );


        String deliveryDate =
                getSafeValue(
                        rs.getString("delivery_date")
                );


        VBox serviceBox =
                createDetailBox(
                        "SERVICE",
                        service
                );


        VBox quantityBox =
                createDetailBox(
                        "QUANTITY",
                        String.valueOf(quantity)
                );


        VBox costBox =
                createDetailBox(
                        "TOTAL COST",
                        String.format(
                                "৳%,.2f",
                                totalCost
                        )
                );


        VBox pickupBox =
                createDetailBox(
                        "PICKUP DATE",
                        pickupDate
                );


        VBox deliveryBox =
                createDetailBox(
                        "DELIVERY DATE",
                        deliveryDate
                );


        detailsRow
                .getChildren()
                .addAll(
                        serviceBox,
                        quantityBox,
                        costBox,
                        pickupBox,
                        deliveryBox
                );


        // =====================================================
        // NOTES
        // =====================================================

        String notes =
                rs.getString("notes");


        if (notes != null
                && !notes.isBlank()) {

            VBox notesBox =
                    new VBox();

            notesBox.setSpacing(5);


            Label notesTitle =
                    new Label("NOTES");

            notesTitle.setStyle(
                    "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #8A98A8;"
            );


            Label notesLabel =
                    new Label(notes);

            notesLabel.setWrapText(true);

            notesLabel.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-text-fill: #536273;" +
                            "-fx-background-color: #F7F9FB;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 12 10 12;"
            );


            notesBox
                    .getChildren()
                    .addAll(
                            notesTitle,
                            notesLabel
                    );


            card
                    .getChildren()
                    .addAll(
                            topRow,
                            separator,
                            detailsRow,
                            notesBox
                    );

        } else {

            card
                    .getChildren()
                    .addAll(
                            topRow,
                            separator,
                            detailsRow
                    );
        }


        return card;
    }


    // =========================================================
    // DETAIL BOX
    // =========================================================

    private VBox createDetailBox(
            String title,
            String value
    ) {

        VBox box = new VBox();

        box.setSpacing(5);

        box.setPrefWidth(150);


        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #8A98A8;"
        );


        Label valueLabel =
                new Label(value);

        valueLabel.setWrapText(true);

        valueLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #27394D;"
        );


        box
                .getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );


        return box;
    }


    // =========================================================
    // EMPTY ORDER CARD
    // =========================================================

    private VBox createEmptyOrderCard() {

        VBox card =
                new VBox();

        card.setSpacing(12);

        card.setAlignment(
                javafx.geometry.Pos.CENTER
        );


        card.setPadding(
                new javafx.geometry.Insets(
                        50
                )
        );


        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #E3EAF0;"
        );


        Label icon =
                new Label("🧺");

        icon.setStyle(
                "-fx-font-size: 45px;"
        );


        Label title =
                new Label(
                        "No Orders Yet"
                );

        title.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );


        Label message =
                new Label(
                        "You haven't placed any laundry orders yet."
                );

        message.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #718096;"
        );


        Button newOrderButton =
                new Button(
                        "+  Create Your First Order"
                );

        newOrderButton.setPrefHeight(40);

        newOrderButton.setStyle(
                "-fx-background-color: #0B4F6C;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );


        newOrderButton.setOnAction(
                event ->
                        openNewOrder(event)
        );


        card
                .getChildren()
                .addAll(
                        icon,
                        title,
                        message,
                        newOrderButton
                );


        return card;
    }


    // =========================================================
    // STATUS STYLE
    // =========================================================

    private String getStatusStyle(
            String status
    ) {

        if (status == null) {
            status = "";
        }


        if (status.equalsIgnoreCase("PENDING")) {

            return
                    "-fx-background-color: #FFF4D6;" +
                            "-fx-text-fill: #A66A00;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15 8 15;" +
                            "-fx-background-radius: 20;";

        } else if (
                status.equalsIgnoreCase("WASHING")
                        || status.equalsIgnoreCase("PROCESSING")
        ) {

            return
                    "-fx-background-color: #E7F3F7;" +
                            "-fx-text-fill: #0B4F6C;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15 8 15;" +
                            "-fx-background-radius: 20;";

        } else if (
                status.equalsIgnoreCase("READY")
                        || status.equalsIgnoreCase("READY FOR DELIVERY")
        ) {

            return
                    "-fx-background-color: #E8F7EE;" +
                            "-fx-text-fill: #16803C;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15 8 15;" +
                            "-fx-background-radius: 20;";

        } else if (
                status.equalsIgnoreCase("DELIVERED")
        ) {

            return
                    "-fx-background-color: #E8F7EE;" +
                            "-fx-text-fill: #16803C;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15 8 15;" +
                            "-fx-background-radius: 20;";

        } else if (
                status.equalsIgnoreCase("CANCELLED")
        ) {

            return
                    "-fx-background-color: #FDECEC;" +
                            "-fx-text-fill: #C0392B;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15 8 15;" +
                            "-fx-background-radius: 20;";

        }


        return
                "-fx-background-color: #EDF1F5;" +
                        "-fx-text-fill: #536273;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-background-radius: 20;";
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void goDashboard(
            ActionEvent event
    ) {

        loadPage(
                event,
                "/fxml/customer-dashboard.fxml"
        );
    }


    // =========================================================
    // NEW ORDER
    // =========================================================

    @FXML
    private void openNewOrder(
            ActionEvent event
    ) {

        loadPage(
                event,
                "/fxml/new-order.fxml"
        );
    }


    // =========================================================
    // PROFILE
    // =========================================================

    @FXML
    private void openProfile(
            ActionEvent event
    ) {

        loadPage(
                event,
                "/fxml/Profile.fxml"
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void logout(
            ActionEvent event
    ) {

        Session.userId = 0;
        Session.username = null;
        Session.role = null;


        loadPage(
                event,
                "/fxml/login.fxml"
        );
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
                            getClass()
                                    .getResource(fxmlPath)
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

            showError(
                    "Could not open page:\n"
                            + fxmlPath
            );
        }
    }


    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String getSafeValue(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return "Not provided";
        }

        return value;
    }


    // =========================================================
    // ERROR ALERT
    // =========================================================

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle("LaundryLink");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}