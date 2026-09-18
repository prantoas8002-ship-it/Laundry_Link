package controller;

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

    @FXML
    public void initialize() {

        orderIdCol.setCellValueFactory(
                new PropertyValueFactory<>("orderId"));

        customerCol.setCellValueFactory(
                new PropertyValueFactory<>("customer"));

        serviceCol.setCellValueFactory(
                new PropertyValueFactory<>("service"));

        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        costCol.setCellValueFactory(
                new PropertyValueFactory<>("cost"));

        loadSampleData();

        ordersTable.setItems(orders);

        updateTotalOrders();

        makeColumnsEqualWidth();

        ordersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {

                    if (newVal != null) {
                        selectedOrderLabel.setText(
                                newVal.getOrderId());
                    } else {
                        selectedOrderLabel.setText("None");
                    }
                });
    }

    private void makeColumnsEqualWidth() {

        double borderPadding = 2.0;

        orderIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty().subtract(borderPadding),
                        6));

        customerCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty().subtract(borderPadding),
                        6));

        serviceCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty().subtract(borderPadding),
                        6));

        quantityCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty().subtract(borderPadding),
                        6));

        statusCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty().subtract(borderPadding),
                        6));

        costCol.prefWidthProperty().bind(
                Bindings.divide(
                        ordersTable.widthProperty().subtract(borderPadding),
                        6));

        orderIdCol.setResizable(false);
        customerCol.setResizable(false);
        serviceCol.setResizable(false);
        quantityCol.setResizable(false);
        statusCol.setResizable(false);
        costCol.setResizable(false);
    }

    private void loadSampleData() {

        orders.add(new Order(
                "ORD-1001",
                "Pranto",
                "Wash",
                5,
                "Pending",
                150));

        orders.add(new Order(
                "ORD-1002",
                "Rahim",
                "Dry Clean",
                3,
                "Processing",
                240));

        orders.add(new Order(
                "ORD-1003",
                "Karim",
                "Iron",
                8,
                "Completed",
                160));
    }

    private void updateTotalOrders() {

        totalOrdersLabel.setText(
                String.valueOf(orders.size()));
    }

    @FXML
    private void addOrder(ActionEvent event) {

        loadPage(
                event,
                "/fxml/new-order.fxml");
    }

    @FXML
    private void updateOrder(ActionEvent event) {

        Order selected =
                ordersTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    "Warning",
                    "Please select an order first.");

            return;
        }

        showAlert(
                "Update",
                "Update feature will be implemented later.");
    }

    @FXML
    private void deleteOrder(ActionEvent event) {

        Order selected =
                ordersTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    "Warning",
                    "Please select an order.");

            return;
        }

        orders.remove(selected);

        ordersTable.refresh();

        updateTotalOrders();

        selectedOrderLabel.setText("None");

        showAlert(
                "Success",
                "Order deleted successfully.");
    }

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

            if (order.getOrderId()
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
                            .contains(keyword)) {

                filtered.add(order);
            }
        }

        ordersTable.setItems(filtered);
    }

    @FXML
    private void goDashboard(ActionEvent event) {

        loadPage(
                event,
                "/fxml/admin-dashboard.fxml");
    }

    private void loadPage(
            ActionEvent event,
            String path) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass()
                                    .getResource(path));

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root));

            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Could not load:\n" + path);
        }
    }

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}