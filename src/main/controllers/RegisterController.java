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

    @FXML
    public void initialize() {
        // Gerekirse başlangıç ayarları
    }

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

        // 2. Şifre Uzunluk Kontrolü
        if (password.length() < 8) {
            showAlert("Weak Password", "Password must be at least 8 characters long.");
            return;
        }

        // 3. Kullanıcı Adı Kontrolü
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

    // --- YENİ METOD: LOGİN EKRANINI AÇ ---
    private void returnToLogin() {
        try {
            // 1. Şu anki kayıt penceresini kapat
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

            // 2. Login ekranını yükle ve aç
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("GreenGrocer Login");
            loginStage.setScene(new Scene(root));
            
            // CSS Yükle (Görünüm bozulmasın)
            if (getClass().getResource("/styles.css") != null) {
                loginStage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            }

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
        alert.showAndWait();
    }
}