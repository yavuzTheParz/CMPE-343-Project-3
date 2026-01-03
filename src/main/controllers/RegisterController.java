package main.controllers;

import main.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;

    /**
     * Initialize UI hints for the register form.
     */
    @FXML
    public void initialize() {
        // Inline UI hints: prompt and tooltip for password strength guidance
        if (passwordField != null) {
            passwordField.setPromptText("Min 8 chars, upper+lower+digit+special");
            Tooltip t = new Tooltip("Password requirements:\n- At least 8 characters\n- Uppercase and lowercase letters\n- At least one digit\n- At least one special character\n- Must not contain the username");
            t.setWrapText(true);
            passwordField.setTooltip(t);
        }
        if (usernameField != null) {
            usernameField.setPromptText("Choose a unique username");
        }
        if (emailField != null) {
            emailField.setPromptText("you@example.com");
        }
    }

    /**
     * Handle registration action from the UI. Validates input and calls
     * {@link main.dao.UserDAO#registerUser} when checks pass.
     */
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String email = emailField.getText().trim();

        // 1. Boş Alan Kontrolü
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        // 2. Email basic check
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return;
        }

        // 3. Strong password policy
        if (!isStrongPassword(password, username)) {
            showAlert("Weak Password", "Password must be at least 8 characters, include uppercase, lowercase, digits and special characters, and must not contain the username.");
            return;
        }

        // 4. Username check
        if (UserDAO.isUsernameTaken(username)) {
            showAlert("Username Taken", "This username is already in use.");
            return;
        }

        // Rol varsayılan olarak 'customer'
        boolean success = UserDAO.registerUser(username, password, email, "customer");

        if (success) {
            showAlert("Success", "Registration successful! Redirecting to Login...");
            returnToLogin(); // --- YENİ EKLENEN YÖNLENDİRME ---
        } else {
            showAlert("Error", "Registration failed due to a database error.");
        }
    }

    private boolean isStrongPassword(String password, String username) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        if (username != null && !username.isEmpty() && password.toLowerCase().contains(username.toLowerCase())) return false;
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+[]{}|;:'\",.<>/?`~-=".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // --- HANDLE BACK TO LOGIN ---
    @FXML
    private void handleBackToLogin() {
        returnToLogin();
    }

    // --- YENİ METOD: LOGİN EKRANINI AÇ ---
    private void returnToLogin() {
        try {
            // 1. Close the current registration window
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

            // 2. Login ekranını yükle ve aç
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            // Normalize inline FXML styles into CSS classes
            main.controllers.BaseController.normalizeStyles(root);
            Stage loginStage = new Stage();
            loginStage.setTitle("GreenGrocer Login");
            Scene scene = new Scene(root);
            if (getClass().getResource("/green-grocer-theme.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/green-grocer-theme.css").toExternalForm());
            }
            loginStage.setScene(scene);
            loginStage.setWidth(960);
            loginStage.setHeight(540);
            loginStage.centerOnScreen();
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not return to login screen.");
        }
    }
    // --------------------------------------

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (title.contains("Error") || title.contains("Taken") || title.contains("Weak")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Ensure the dialog is wide enough for long messages
        alert.getDialogPane().setMinWidth(500);
        alert.getDialogPane().setPrefWidth(500);
        
        alert.showAndWait();
    }
}