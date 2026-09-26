package controller;

import database.DBConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerRegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea addressField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;


    // ==========================================
    // REGISTER CUSTOMER
    // ==========================================

    @FXML
    private void register(ActionEvent event) {

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();


        // ==========================================
        // VALIDATION
        // ==========================================

        if (name.isEmpty()
                || phone.isEmpty()
                || username.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            showWarning(
                    "Please fill in all required fields."
            );

            return;
        }


        if (!password.equals(confirmPassword)) {

            showWarning(
                    "Password and Confirm Password do not match."
            );

            return;
        }


        if (password.length() < 4) {

            showWarning(
                    "Password must contain at least 4 characters."
            );

            return;
        }


        // ==========================================
        // DATABASE TRANSACTION
        // ==========================================

        String checkUsernameSql =
                "SELECT id FROM users WHERE username = ?";


        String insertUserSql = """
                INSERT INTO users
                (username, password, role)
                VALUES (?, ?, 'CUSTOMER')
                """;


        String insertCustomerSql = """
                INSERT INTO customers
                (user_id, name, phone, email, address)
                VALUES (?, ?, ?, ?, ?)
                """;


        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);


            try {

                // ==========================================
                // CHECK USERNAME
                // ==========================================

                try (PreparedStatement ps =
                             conn.prepareStatement(checkUsernameSql)) {

                    ps.setString(1, username);

                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {

                        conn.rollback();

                        showWarning(
                                "This username already exists.\n"
                                        + "Please choose another username."
                        );

                        return;
                    }
                }


                // ==========================================
                // CREATE USER
                // ==========================================

                int userId;

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     insertUserSql,
                                     java.sql.Statement.RETURN_GENERATED_KEYS
                             )) {

                    ps.setString(1, username);
                    ps.setString(2, password);

                    ps.executeUpdate();

                    ResultSet keys =
                            ps.getGeneratedKeys();

                    if (!keys.next()) {

                        throw new Exception(
                                "Could not create user account."
                        );
                    }

                    userId = keys.getInt(1);
                }


                // ==========================================
                // CREATE CUSTOMER PROFILE
                // ==========================================

                try (PreparedStatement ps =
                             conn.prepareStatement(insertCustomerSql)) {

                    ps.setInt(1, userId);
                    ps.setString(2, name);
                    ps.setString(3, phone);

                    if (email.isEmpty()) {
                        ps.setString(4, null);
                    } else {
                        ps.setString(4, email);
                    }

                    if (address.isEmpty()) {
                        ps.setString(5, null);
                    } else {
                        ps.setString(5, address);
                    }

                    ps.executeUpdate();
                }


                // ==========================================
                // EVERYTHING SUCCESSFUL
                // ==========================================

                conn.commit();

                showSuccess(
                        "Account created successfully!\n\n"
                                + "You can now login using your username and password."
                );

                clearForm();

                goToLogin(event);

            } catch (Exception e) {

                conn.rollback();

                throw e;
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Registration failed.\n\n"
                            + e.getMessage()
            );
        }
    }


    // ==========================================
    // CLEAR FORM
    // ==========================================

    @FXML
    private void clearForm() {

        nameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }


    // ==========================================
    // BACK TO LOGIN
    // ==========================================

    @FXML
    private void goLogin(ActionEvent event) {

        goToLogin(event);
    }


    private void goToLogin(ActionEvent event) {

        loadPage(
                event,
                "/fxml/login.fxml"
        );
    }


    // ==========================================
    // LOAD PAGE
    // ==========================================

    private void loadPage(
            ActionEvent event,
            String fxmlPath
    ) {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(fxmlPath)
                    );

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setWidth(1920);
            stage.setHeight(1080);

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


    // ==========================================
    // WARNING
    // ==========================================

    private void showWarning(String message) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Registration");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }


    // ==========================================
    // SUCCESS
    // ==========================================

    private void showSuccess(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Registration Successful");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }


    // ==========================================
    // ERROR
    // ==========================================

    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Registration Failed");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}
