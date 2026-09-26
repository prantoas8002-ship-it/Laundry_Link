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
import database.DBConnection;

import java.io.IOException;
import java.sql.*;

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


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        // Categories
        categoryBox.setItems(
                FXCollections.observableArrayList(
                        "Detergent",
                        "Softener",
                        "Bleach",
                        "Packaging",
                        "Cleaning Supply"
                )
        );

        // Units
        unitBox.setItems(
                FXCollections.observableArrayList(
                        "Kg",
                        "Liter",
                        "Piece",
                        "Box",
                        "Bottle"
                )
        );


        // Table columns
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        itemNameCol.setCellValueFactory(
                new PropertyValueFactory<>("itemName")
        );

        categoryCol.setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        unitCol.setCellValueFactory(
                new PropertyValueFactory<>("unit")
        );

        supplierCol.setCellValueFactory(
                new PropertyValueFactory<>("supplier")
        );

        costCol.setCellValueFactory(
                new PropertyValueFactory<>("cost")
        );


        inventoryTable.setItems(inventoryList);


        // Equal column width
        makeColumnsEqualWidth();


        // Load database records
        loadInventory();


        // When table row is selected
        inventoryTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, selected) -> {

                            if (selected != null) {

                                itemNameField.setText(
                                        selected.getItemName()
                                );

                                categoryBox.setValue(
                                        selected.getCategory()
                                );

                                quantityField.setText(
                                        String.valueOf(
                                                selected.getQuantity()
                                        )
                                );

                                unitBox.setValue(
                                        selected.getUnit()
                                );

                                supplierField.setText(
                                        selected.getSupplier()
                                );

                                costField.setText(
                                        String.valueOf(
                                                selected.getCost()
                                        )
                                );
                            }
                        }
                );
    }


    // =========================================================
    // LOAD INVENTORY
    // =========================================================

    @FXML
    private void loadInventory() {

        inventoryList.clear();

        String sql = """
                SELECT
                    id,
                    item_name,
                    category,
                    quantity,
                    unit,
                    supplier,
                    cost
                FROM inventory
                ORDER BY id DESC
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Inventory item =
                        new Inventory(
                                rs.getInt("id"),
                                rs.getString("item_name"),
                                rs.getString("category"),
                                rs.getInt("quantity"),
                                rs.getString("unit"),
                                rs.getString("supplier"),
                                rs.getDouble("cost")
                        );

                inventoryList.add(item);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    "Could not load inventory data."
            );
        }
    }


    // =========================================================
    // ADD ITEM
    // =========================================================

    @FXML
    private void addItem(ActionEvent event) {

        if (!validateFields()) {
            return;
        }


        String sql = """
                INSERT INTO inventory
                (
                    item_name,
                    category,
                    quantity,
                    unit,
                    supplier,
                    cost
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    itemNameField.getText().trim()
            );

            ps.setString(
                    2,
                    categoryBox.getValue()
            );

            ps.setInt(
                    3,
                    Integer.parseInt(
                            quantityField.getText().trim()
                    )
            );

            ps.setString(
                    4,
                    unitBox.getValue()
            );

            ps.setString(
                    5,
                    supplierField.getText().trim()
            );

            ps.setDouble(
                    6,
                    Double.parseDouble(
                            costField.getText().trim()
                    )
            );


            ps.executeUpdate();


            showAlert(
                    "Inventory item added successfully."
            );


            clearFields();

            loadInventory();


        } catch (NumberFormatException e) {

            showAlert(
                    "Quantity must be an integer and cost must be a number."
            );

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    "Could not add inventory item."
            );
        }
    }


    // =========================================================
    // UPDATE ITEM
    // =========================================================

    @FXML
    private void updateItem(ActionEvent event) {

        Inventory selected =
                inventoryTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selected == null) {

            showAlert(
                    "Please select an inventory item first."
            );

            return;
        }


        if (!validateFields()) {
            return;
        }


        String sql = """
                UPDATE inventory
                SET
                    item_name = ?,
                    category = ?,
                    quantity = ?,
                    unit = ?,
                    supplier = ?,
                    cost = ?
                WHERE id = ?
                """;


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    itemNameField.getText().trim()
            );

            ps.setString(
                    2,
                    categoryBox.getValue()
            );

            ps.setInt(
                    3,
                    Integer.parseInt(
                            quantityField.getText().trim()
                    )
            );

            ps.setString(
                    4,
                    unitBox.getValue()
            );

            ps.setString(
                    5,
                    supplierField.getText().trim()
            );

            ps.setDouble(
                    6,
                    Double.parseDouble(
                            costField.getText().trim()
                    )
            );

            ps.setInt(
                    7,
                    selected.getId()
            );


            ps.executeUpdate();


            showAlert(
                    "Inventory item updated successfully."
            );


            clearFields();

            loadInventory();


        } catch (NumberFormatException e) {

            showAlert(
                    "Quantity must be an integer and cost must be a number."
            );

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    "Could not update inventory item."
            );
        }
    }


    // =========================================================
    // DELETE ITEM
    // =========================================================

    @FXML
    private void deleteItem(ActionEvent event) {

        Inventory selected =
                inventoryTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selected == null) {

            showAlert(
                    "Please select an inventory item first."
            );

            return;
        }


        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Delete Inventory Item"
        );

        confirmation.setHeaderText(null);

        confirmation.setContentText(
                "Are you sure you want to delete \""
                        + selected.getItemName()
                        + "\"?"
        );


        if (
                confirmation.showAndWait().orElse(
                        ButtonType.CANCEL
                ) != ButtonType.OK
        ) {

            return;
        }


        String sql =
                "DELETE FROM inventory WHERE id = ?";


        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    selected.getId()
            );

            ps.executeUpdate();


            showAlert(
                    "Inventory item deleted successfully."
            );


            clearFields();

            loadInventory();


        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    "Could not delete inventory item."
            );
        }
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private void searchItem(ActionEvent event) {

        String keyword =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();


        if (keyword.isEmpty()) {

            loadInventory();

            return;
        }


        ObservableList<Inventory> filtered =
                FXCollections.observableArrayList();


        for (Inventory item : inventoryList) {

            if (
                    (item.getItemName() != null
                            &&
                            item.getItemName()
                                    .toLowerCase()
                                    .contains(keyword))

                            ||

                            (item.getCategory() != null
                                    &&
                                    item.getCategory()
                                            .toLowerCase()
                                            .contains(keyword))

                            ||

                            (item.getSupplier() != null
                                    &&
                                    item.getSupplier()
                                            .toLowerCase()
                                            .contains(keyword))
            ) {

                filtered.add(item);
            }
        }


        inventoryTable.setItems(filtered);
    }


    // =========================================================
    // CLEAR
    // =========================================================

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

        searchField.clear();

        inventoryTable
                .getSelectionModel()
                .clearSelection();


        inventoryTable.setItems(
                inventoryList
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private boolean validateFields() {

        if (
                itemNameField.getText()
                        .trim()
                        .isEmpty()
        ) {

            showAlert(
                    "Please enter item name."
            );

            return false;
        }


        if (categoryBox.getValue() == null) {

            showAlert(
                    "Please select a category."
            );

            return false;
        }


        if (
                quantityField.getText()
                        .trim()
                        .isEmpty()
        ) {

            showAlert(
                    "Please enter quantity."
            );

            return false;
        }


        if (unitBox.getValue() == null) {

            showAlert(
                    "Please select a unit."
            );

            return false;
        }


        if (
                supplierField.getText()
                        .trim()
                        .isEmpty()
        ) {

            showAlert(
                    "Please enter supplier name."
            );

            return false;
        }


        if (
                costField.getText()
                        .trim()
                        .isEmpty()
        ) {

            showAlert(
                    "Please enter unit cost."
            );

            return false;
        }


        try {

            int quantity =
                    Integer.parseInt(
                            quantityField
                                    .getText()
                                    .trim()
                    );

            double cost =
                    Double.parseDouble(
                            costField
                                    .getText()
                                    .trim()
                    );


            if (quantity < 0) {

                showAlert(
                        "Quantity cannot be negative."
                );

                return false;
            }


            if (cost < 0) {

                showAlert(
                        "Cost cannot be negative."
                );

                return false;
            }


        } catch (NumberFormatException e) {

            showAlert(
                    "Quantity must be an integer and cost must be a number."
            );

            return false;
        }


        return true;
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void goToDashboard(ActionEvent event) {

        if (Session.role == null) {

            loadPage(
                    event,
                    "/fxml/Login.fxml"
            );

            return;
        }


        switch (
                Session.role.toUpperCase()
        ) {

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
                        "/fxml/Login.fxml"
                );

                break;
        }
    }


    // =========================================================
    // PAGE LOADING
    // =========================================================

    private void loadPage(
            ActionEvent event,
            String fxmlPath
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass()
                                    .getResource(
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
    // EQUAL COLUMN WIDTH
    // =========================================================

    private void makeColumnsEqualWidth() {

        double borderPadding = 2.0;


        idCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        itemNameCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        categoryCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        quantityCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        unitCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        supplierCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        costCol.prefWidthProperty().bind(
                Bindings.divide(
                        inventoryTable
                                .widthProperty()
                                .subtract(borderPadding),
                        7
                )
        );


        idCol.setResizable(false);
        itemNameCol.setResizable(false);
        categoryCol.setResizable(false);
        quantityCol.setResizable(false);
        unitCol.setResizable(false);
        supplierCol.setResizable(false);
        costCol.setResizable(false);
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Inventory Management"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}