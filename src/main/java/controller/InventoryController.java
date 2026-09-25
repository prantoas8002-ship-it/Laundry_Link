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
import model.Inventory;
import model.Session;

import java.io.IOException;

public class InventoryController {

    @FXML
    private TextField itemNameField;

    @FXML
    private ComboBox<String> categoryBox;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> unitBox;

    @FXML
    private TextField supplierField;

    @FXML
    private TextField costField;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Inventory> inventoryTable;

    @FXML
    private TableColumn<Inventory, Integer> idCol;

    @FXML
    private TableColumn<Inventory, String> itemNameCol;

    @FXML
    private TableColumn<Inventory, String> categoryCol;

    @FXML
    private TableColumn<Inventory, Integer> quantityCol;

    @FXML
    private TableColumn<Inventory, String> unitCol;

    @FXML
    private TableColumn<Inventory, String> supplierCol;

    @FXML
    private TableColumn<Inventory, Double> costCol;

    private final ObservableList<Inventory> inventoryList =
            FXCollections.observableArrayList();

    private int nextId = 1;

    @FXML
    public void initialize() {

        categoryBox.setItems(FXCollections.observableArrayList(
                "Detergent",
                "Softener",
                "Bleach",
                "Packaging",
                "Cleaning Supply"
        ));

        unitBox.setItems(FXCollections.observableArrayList(
                "Kg",
                "Liter",
                "Piece",
                "Box",
                "Bottle"
        ));

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));

        inventoryTable.setItems(inventoryList);

        makeColumnsEqualWidth();

        inventoryTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {

                    if (selected != null) {

                        itemNameField.setText(selected.getItemName());
                        categoryBox.setValue(selected.getCategory());
                        quantityField.setText(
                                String.valueOf(selected.getQuantity())
                        );
                        unitBox.setValue(selected.getUnit());
                        supplierField.setText(selected.getSupplier());
                        costField.setText(
                                String.valueOf(selected.getCost())
                        );
                    }
                });
    }

    @FXML
    private void addItem(ActionEvent event) {

        try {

            if (itemNameField.getText().isEmpty()
                    || categoryBox.getValue() == null
                    || quantityField.getText().isEmpty()
                    || unitBox.getValue() == null
                    || supplierField.getText().isEmpty()
                    || costField.getText().isEmpty()) {

                showAlert("Please fill all fields.");
                return;
            }

            Inventory item = new Inventory(
                    nextId++,
                    itemNameField.getText(),
                    categoryBox.getValue(),
                    Integer.parseInt(quantityField.getText()),
                    unitBox.getValue(),
                    supplierField.getText(),
                    Double.parseDouble(costField.getText())
            );

            inventoryList.add(item);

            clearFields();

        } catch (NumberFormatException e) {

            showAlert("Quantity and Cost must be numeric.");
        }
    }

    @FXML
    private void updateItem(ActionEvent event) {

        Inventory selected =
                inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showAlert("Select an item first.");
            return;
        }

        try {

            selected.setItemName(itemNameField.getText());
            selected.setCategory(categoryBox.getValue());
            selected.setQuantity(
                    Integer.parseInt(quantityField.getText())
            );
            selected.setUnit(unitBox.getValue());
            selected.setSupplier(supplierField.getText());
            selected.setCost(
                    Double.parseDouble(costField.getText())
            );

            inventoryTable.refresh();

            clearFields();

        } catch (NumberFormatException e) {

            showAlert("Invalid numeric value.");
        }
    }

    @FXML
    private void deleteItem(ActionEvent event) {

        Inventory selected =
                inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showAlert("Select an item first.");
            return;
        }

        inventoryList.remove(selected);

        clearFields();
    }

    @FXML
    private void searchItem(ActionEvent event) {

        String keyword =
                searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {

            inventoryTable.setItems(inventoryList);
            return;
        }

        ObservableList<Inventory> filtered =
                FXCollections.observableArrayList();

        for (Inventory item : inventoryList) {

            if (item.getItemName().toLowerCase().contains(keyword)
                    || item.getCategory().toLowerCase().contains(keyword)
                    || item.getSupplier().toLowerCase().contains(keyword)) {

                filtered.add(item);
            }
        }

        inventoryTable.setItems(filtered);
    }

    @FXML
    private void clearFields(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {

        itemNameField.clear();
        quantityField.clear();
        supplierField.clear();
        costField.clear();

        categoryBox.setValue(null);
        unitBox.setValue(null);

        inventoryTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        switch (Session.role)
        {
            case "ADMIN" :
                loadPage(event, "/fxml/admin-dashboard.fxml");
                break;
            case "STAFF" :
                loadPage(event, "/fxml/staff-dashboard.fxml");
                break;
            case "CUSTOMER" :
                loadPage(event, "/fxml/customer-dashboard.fxml");
                break;
            default:
                loadPage(event, "/fxml/admin-dashboard.fxml");
        }
    }

    private void loadPage(
            ActionEvent event,
            String fxmlPath
    ) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            Alert alert = new Alert(
                    Alert.AlertType.ERROR
            );

            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Could not load:\n" + fxmlPath
            );

            alert.showAndWait();
        }
    }

    private void makeColumnsEqualWidth() {

        double borderPadding = 2.0;

        idCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        itemNameCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        categoryCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        quantityCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        unitCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        supplierCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        costCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable.widthProperty().subtract(borderPadding),
                        7));

        idCol.setResizable(false);
        itemNameCol.setResizable(false);
        categoryCol.setResizable(false);
        quantityCol.setResizable(false);
        unitCol.setResizable(false);
        supplierCol.setResizable(false);
        costCol.setResizable(false);
    }

    private void showAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Inventory Management");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}

