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
import model.Billing;

import java.io.IOException;
import java.time.LocalDate;

public class BillingController {

    @FXML
    private TextField orderIdField;

    @FXML
    private TextField customerField;

    @FXML
    private TextField serviceField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField unitPriceField;

    @FXML
    private TextField totalAmountField;

    @FXML
    private TableView<Billing> billingTable;

    @FXML
    private TableColumn<Billing, Integer> billIdCol;

    @FXML
    private TableColumn<Billing, String> orderIdCol;

    @FXML
    private TableColumn<Billing, String> customerCol;

    @FXML
    private TableColumn<Billing, String> serviceCol;

    @FXML
    private TableColumn<Billing, Double> amountCol;

    @FXML
    private TableColumn<Billing, String> dateCol;

    @FXML
    private TableColumn<Billing, String> statusCol;

    private final ObservableList<Billing> billingList =
            FXCollections.observableArrayList();

    private int nextBillId = 1;

    @FXML
    public void initialize() {

        billIdCol.setCellValueFactory(
                new PropertyValueFactory<>("billId"));

        orderIdCol.setCellValueFactory(
                new PropertyValueFactory<>("orderId"));

        customerCol.setCellValueFactory(
                new PropertyValueFactory<>("customer"));

        serviceCol.setCellValueFactory(
                new PropertyValueFactory<>("service"));

        amountCol.setCellValueFactory(
                new PropertyValueFactory<>("amount"));

        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        billingTable.setItems(billingList);

        makeColumnsEqualWidth();
    }

    @FXML
    private void calculateBill(ActionEvent event) {

        try {

            int quantity =
                    Integer.parseInt(quantityField.getText());

            double unitPrice =
                    Double.parseDouble(unitPriceField.getText());

            double total = quantity * unitPrice;

            totalAmountField.setText(
                    String.format("%.2f", total));

        } catch (Exception e) {

            showAlert("Enter valid quantity and price.");
        }
    }

    @FXML
    private void saveBill(ActionEvent event) {

        if (orderIdField.getText().isEmpty()
                || customerField.getText().isEmpty()
                || serviceField.getText().isEmpty()
                || totalAmountField.getText().isEmpty()) {

            showAlert("Please complete bill information.");
            return;
        }

        Billing bill = new Billing(
                nextBillId++,
                orderIdField.getText(),
                customerField.getText(),
                serviceField.getText(),
                Double.parseDouble(totalAmountField.getText()),
                LocalDate.now().toString(),
                "Pending"
        );

        billingList.add(bill);

        clearFields();
    }

    @FXML
    private void printBill(ActionEvent event) {

        Billing selected =
                billingTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showAlert("Select a bill first.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Bill");

        alert.setHeaderText("Bill Information");

        alert.setContentText(
                "Bill ID : " + selected.getBillId() +
                        "\nOrder ID : " + selected.getOrderId() +
                        "\nCustomer : " + selected.getCustomer() +
                        "\nService : " + selected.getService() +
                        "\nAmount : " + selected.getAmount()
        );

        alert.showAndWait();
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
            stage.show();

        } catch (IOException e) {

            showAlert("Dashboard page not found.");
        }
    }

    private void clearFields() {

        orderIdField.clear();
        customerField.clear();
        serviceField.clear();
        quantityField.clear();
        unitPriceField.clear();
        totalAmountField.clear();
    }

    private void makeColumnsEqualWidth() {

        double borderPadding = 2.0;

        billIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        orderIdCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        customerCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        serviceCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        amountCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        dateCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        statusCol.prefWidthProperty().bind(
                Bindings.divide(
                        billingTable.widthProperty().subtract(borderPadding),
                        7));

        billIdCol.setResizable(false);
        orderIdCol.setResizable(false);
        customerCol.setResizable(false);
        serviceCol.setResizable(false);
        amountCol.setResizable(false);
        dateCol.setResizable(false);
        statusCol.setResizable(false);
    }

    private void showAlert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Billing");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}