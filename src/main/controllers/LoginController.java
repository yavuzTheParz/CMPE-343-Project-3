package main.controllers; // ARTIK SAMPLE DEĞİL

import main.dao.UserDAO;
import main.controllers.CustomerController;  // doğru paket adıyla import ediyoruz

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            // Kullanıcı adı veya şifre boşsa hata göster
            showAlert("Error", "Please enter both username and password.");
        } else {
            // UserDAO ile kullanıcıyı kontrol et
            if (UserDAO.userExists(username, password)) {
                // Kullanıcı doğrulaması başarılı
                openCustomerDashboard();
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        }
    }

    // Customer ekranını aç
    private void openCustomerDashboard() {
        try {
            // Yeni stage oluştur
            Stage customerStage = new Stage();
            CustomerController customerController = new CustomerController();
            customerController.start(customerStage); // Customer ekranını başlat
            // Mevcut stage'i kapat
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the customer dashboard.");
        }
    }

    // Basit alert gösterim metodu
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
