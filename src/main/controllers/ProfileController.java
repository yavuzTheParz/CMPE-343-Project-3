package main.controllers;

import main.dao.UserDAO;
import main.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Map;

/**
 * Controller that manages viewing and editing the current user's profile.
 */
public class ProfileController {

    @FXML private TextField usernameField; // Read-only
    @FXML private TextField emailField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        try {
            if (UserSession.getInstance() != null && UserSession.getInstance().getUsername() != null) {
                String username = UserSession.getInstance().getUsername();
                usernameField.setText(username);
                usernameField.setEditable(false); // Username is not editable

                // Veritabanından diğer bilgileri çek
                Map<String, String> details = UserDAO.getUserDetails(username);
                if (details != null) {
                    emailField.setText(details.getOrDefault("email", ""));
                    addressField.setText(details.getOrDefault("address", ""));
                    phoneField.setText(details.getOrDefault("phone", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load user data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();
        // Basic email validation
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (UserDAO.updateUserProfile(username, email, address, phone)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update profile.");
        }
    }

    @FXML
    private void handleClose() {
        usernameField.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}