package main.controllers;

import main.dao.UserDAO;
import main.models.UserRole; // Enum
import main.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController { // İstersek buna da 'extends BaseController' diyebiliriz ama şart değil

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        UserRole role = UserDAO.validateUser(username, password); // Enum dönüyor

        if (role != null) {
            SessionManager.setCurrentUsername(username);
            System.out.println("Login Success! Role: " + role);

            // GÜVENLİ SWITCH-CASE (Enum ile)
            switch (role) {
                case OWNER:
                    openOwnerDashboard();
                    break;
                case CUSTOMER:
                    openCustomerDashboard();
                    break;
                case CARRIER:
                    openCarrierDashboard();
                    break;
                default:
                    errorLabel.setText("Access Denied: Unknown Role");
            }
        } else {
            errorLabel.setText("Invalid credentials!");
        }
    }

    private void openOwnerDashboard() {
        navigate("/owner_dashboard.fxml", "Owner Dashboard");
    }

    private void openCarrierDashboard() {
        navigate("/carrier_dashboard.fxml", "Carrier Dashboard");
    }

    private void openCustomerDashboard() {
        try {
            Stage customerStage = new Stage();
            new CustomerController().start(customerStage);
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading Customer screen.");
        }
    }

    // Helper for local navigation
    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading screen: " + fxml);
        }
    }
}