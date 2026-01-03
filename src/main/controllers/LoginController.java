package main.controllers;

import main.DatabaseAdapter;
import main.models.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // FXML'de varsa kullanılır, yoksa null kalır sorun değil

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        String role = DatabaseAdapter.validateLogin(username, password);

        if (role != null) {
            UserSession.setSession(username, role);

            if (role.equals("owner")) {
                changeScene("/owner_dashboard.fxml", "Owner Dashboard");
            } else if (role.equals("customer")) {
                changeScene("/customer_dashboard.fxml", "Group28 GreenGrocer");
            } else if (role.equals("carrier")) {
                changeScene("/carrier_dashboard.fxml", "Carrier Dashboard");
            }
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    // FXML ile uyumlu olması için isimlendirmeyi standartlaştırdık
    @FXML
    private void handleRegister() {
        changeScene("/register.fxml", "Register New User");
    }

    private void changeScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            // Normalize inline FXML styles so stylesheet rules apply
            main.controllers.BaseController.normalizeStyles(root);
            Scene scene = new Scene(root);

            if (getClass().getResource("/green-grocer-theme.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/green-grocer-theme.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Group28 GreenGrocer - " + title);
            stage.setWidth(960);
            stage.setHeight(540);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + fxmlPath + "\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error loading scene: " + fxmlPath);
            e.printStackTrace();
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}