package controller;

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

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void login(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {

            if (username.equals("aaa") && password.equals("aaa")) {
                loadPage(event, "/fxml/admin-dashboard.fxml");
            }

            else if (username.equals("bbb") && password.equals("bbb")) {
                loadPage(event, "/fxml/staff-dashboard.fxml");
            }

            else if (username.equals("ccc") && password.equals("ccc")) {
                loadPage(event, "/fxml/customer-dashboard.fxml");
            }

            else {
                showError("Invalid username or password!");
            }

        } catch (IOException e) {
            showError("Could not load page.");
            e.printStackTrace();
        }
    }

    private void loadPage(ActionEvent event, String fxmlPath) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(root));

        stage.setWidth(1920);
        stage.setHeight(1080);

        stage.setMaximized(true);

        stage.show();
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}