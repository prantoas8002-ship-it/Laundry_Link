package controller;

import database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;


    // ==========================================
    // INITIALIZE
    // ==========================================

    @FXML
    public void initialize() {

        loadCustomerProfile();
    }


    // ==========================================
    // LOAD CUSTOMER PROFILE
    // ==========================================

    private void loadCustomerProfile() {

        String sql = """
                SELECT
                    c.name,
                    c.phone,
                    c.email,
                    c.address,
                    u.username,
                    u.role
                FROM customers c
                JOIN users u
                    ON c.user_id = u.id
                WHERE c.user_id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, Session.userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                nameLabel.setText(
                        getSafeValue(rs.getString("name"))
                );

                phoneLabel.setText(
                        getSafeValue(rs.getString("phone"))
                );

                emailLabel.setText(
                        getSafeValue(rs.getString("email"))
                );

                addressLabel.setText(
                        getSafeValue(rs.getString("address"))
                );

                usernameLabel.setText(
                        getSafeValue(rs.getString("username"))
                );

                roleLabel.setText(
                        getSafeValue(rs.getString("role"))
                );

            } else {

                showError(
                        "Customer profile not found."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load customer profile."
            );
        }
    }


    // ==========================================
    // HANDLE NULL VALUES
    // ==========================================

    private String getSafeValue(String value) {

        if (value == null || value.isBlank()) {
            return "Not provided";
        }

        return value;
    }


    // ==========================================
    // BACK TO DASHBOARD
    // ==========================================

    @FXML
    private void goDashboard(ActionEvent event) {

        loadPage(
                event,
                "/fxml/customer-dashboard.fxml"
        );
    }


    // ==========================================
    // LOGOUT
    // ==========================================

    @FXML
    private void logout(ActionEvent event) {

        Session.userId = 0;
        Session.username = null;
        Session.role = null;

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


    // ==========================================
    // ERROR ALERT
    // ==========================================

    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("LaundryLink");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}