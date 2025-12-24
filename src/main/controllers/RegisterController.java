package main.controllers;

import main.dao.UserDAO;
import main.models.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

// BaseController'dan miras alıyoruz (navigate yeteneği için)
public class RegisterController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleBox; // Kullanıcı rolünü seçecek

    @FXML
    public void initialize() {
        // Rol kutusunu doldur (Owner hariç, onu SQL'den elle ekleriz ki herkes patron olamasın)
        roleBox.getItems().addAll("Customer", "Carrier");
        roleBox.setValue("Customer"); // Varsayılan
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String roleStr = roleBox.getValue();

        // 1. Boş Alan Kontrolü
        if (user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return;
        }

        // 2. Şifre Uyuşmazlığı
        if (!pass.equals(confirm)) {
            showAlert("Error", "Passwords do not match!");
            return;
        }

        // 3. Kullanıcı Adı Kontrolü
        if (UserDAO.isUsernameTaken(user)) {
            showAlert("Error", "Username is already taken. Choose another.");
            return;
        }

        // 4. Kayıt İşlemi
        UserRole role = UserRole.fromString(roleStr);
        boolean success = UserDAO.registerUser(user, pass, email, role);

        if (success) {
            showAlert("Success", "Registration successful! Please login.");
            goBackToLogin();
        } else {
            showAlert("Error", "Registration failed. Database error.");
        }
    }

    @FXML
    private void handleBack() {
        goBackToLogin();
    }

    private void goBackToLogin() {
        changeScene(usernameField, "/login.fxml", "Login");
    }
}