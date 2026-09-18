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
import model.CustomerHistory;

import java.io.IOException;

public class CustomerHistoryController {

    @FXML
    private TextField searchField;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private Label selectedCustomerLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label totalSpentLabel;

    @FXML
    private TableView<CustomerHistory> historyTable;

    @FXML
    private TableColumn<CustomerHistory, String> customerIdCol;

    @FXML
    private TableColumn<CustomerHistory, String> customerNameCol;

    @FXML
    private TableColumn<CustomerHistory, String> orderIdCol;

    @FXML
    private TableColumn<CustomerHistory, String> serviceCol;

    @FXML
    private TableColumn<CustomerHistory, Integer> quantityCol;

    @FXML
    private TableColumn<CustomerHistory, Double> amountCol;

    @FXML
    private TableColumn<CustomerHistory, String> dateCol;

    @FXML
    private TableColumn<CustomerHistory, String> statusCol;

    private final ObservableList<CustomerHistory> historyList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        customerIdCol.setCellValueFactory(
                new PropertyValueFactory<>("customerId"));

        customerNameCol.setCellValueFactory(
                new PropertyValueFactory<>("customerName"));

        orderIdCol.setCellValueFactory(
                new PropertyValueFactory<>("orderId"));

        serviceCol.setCellValueFactory(
                new PropertyValueFactory<>("service"));

        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));

        amountCol.setCellValueFactory(
                new PropertyValueFactory<>("amount"));

        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        historyTable.setItems(historyList);

        makeColumnsEqualWidth();

        loadSampleData();

        totalCustomersLabel.setText(
                String.valueOf(historyList.size()));

        historyTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {

                    if (selected != null) {

                        selectedCustomerLabel.setText(
                                selected.getCustomerName());

                        int totalOrders = 0;
                        double totalSpent = 0;

                        for (CustomerHistory item : historyList) {

                            if (item.getCustomerId()
                                    .equals(selected.getCustomerId())) {

                                totalOrders++;
                                totalSpent += item.getAmount();
                            }
                        }

                        totalOrdersLabel.setText(
                                String.valueOf(totalOrders));

                        totalSpentLabel.setText(
                                "৳" + String.format("%.2f", totalSpent));
                    }
                });
    }

    private void loadSampleData() {

        historyList.add(
                new CustomerHistory(
                        "C001",
                        "Rahim",
                        "O101",
                        "Wash & Fold",
                        5,
                        250,
                        "2026-09-10",
                        "Completed"
                )
        );

        historyList.add(
                new CustomerHistory(
                        "C002",
                        "Karim",
                        "O102",
                        "Dry Cleaning",
                        3,
                        450,
                        "2026-09-12",
                        "Pending"
                )
        );

        historyList.add(
                new CustomerHistory(
                        "C001",
                        "Rahim",
                        "O103",
                        "Ironing",
                        8,
                        320,
                        "2026-09-15",
                        "Completed"
                )
        );
    }

    @FXML
    private void searchCustomer(ActionEvent event) {

        String keyword =
                searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {

            historyTable.setItems(historyList);
            return;
        }

        ObservableList<CustomerHistory> filtered =
                FXCollections.observableArrayList();

        for (CustomerHistory item : historyList) {

            if (item.getCustomerId().toLowerCase().contains(keyword)
                    || item.getCustomerName().toLowerCase().contains(keyword)) {

                filtered.add(item);
            }
        }

        historyTable.setItems(filtered);
    }

    @FXML
    private void goToDashboard(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/admin-dashboard.fxml")
            );

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("LaundryLink Dashboard");
            stage.show();

        } catch (IOException e) {

            showAlert("Dashboard page not found.");
        }
    }

    private void makeColumnsEqualWidth() {

        double borderPadding = 2.0;

        customerIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        customerNameCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        orderIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        serviceCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        quantityCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        amountCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        dateCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        statusCol.prefWidthProperty().bind(
                Bindings.divide(
                        historyTable.widthProperty().subtract(borderPadding),
                        8));

        customerIdCol.setResizable(false);
        customerNameCol.setResizable(false);
        orderIdCol.setResizable(false);
        serviceCol.setResizable(false);
        quantityCol.setResizable(false);
        amountCol.setResizable(false);
        dateCol.setResizable(false);
        statusCol.setResizable(false);
    }

    private void showAlert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Customer History");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
