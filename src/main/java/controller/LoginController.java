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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;


    // =========================
    // LOGIN
    // =========================

    @FXML
    private void login(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Check empty fields
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        String sql =
                "SELECT id, username, role FROM users " +
                        "WHERE username = ? AND password = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                // Store logged-in user information
                Session.userId = rs.getInt("id");
                Session.username = rs.getString("username");
                Session.role = rs.getString("role");

                System.out.println(
                        "LOGIN SUCCESS: "
                                + Session.username
                                + " | USER ID: "
                                + Session.userId
                                + " | ROLE: "
                                + Session.role
                );

                // Open dashboard according to role

                if (Session.role.equals("ADMIN")) {

                    loadPage(
                            event,
                            "/fxml/admin-dashboard.fxml"
                    );

                } else if (Session.role.equals("STAFF")) {

                    loadPage(
                            event,
                            "/fxml/staff-dashboard.fxml"
                    );

                } else if (Session.role.equals("CUSTOMER")) {

                    loadPage(
                            event,
                            "/fxml/customer-dashboard.fxml"
                    );

                } else {

                    showError("Invalid user role.");
                }

            } else {

                showError("Invalid username or password.");
            }

        } catch (Exception e) {

            showError("Database error occurred.");
            e.printStackTrace();
        }
    }


    // =========================
    // OPEN REGISTER PAGE
    // =========================

    @FXML
    private void openRegister(ActionEvent event) {

        loadPage(
                event,
                "/fxml/customer-register.fxml"
        );
    }


    // =========================
    // LOAD PAGE
    // =========================

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


    // =========================
    // ERROR ALERT
    // =========================

    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Login Failed");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}

