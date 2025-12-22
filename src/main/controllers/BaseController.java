package main.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

// Abstract yapıyoruz ki kimse direkt "new BaseController()" diyemesin.
public abstract class BaseController {

    // 1. Ortak Uyarı Metodu (Protected: Sadece miras alanlar kullansın)
    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 2. Ortak Sahne Değiştirme Metodu (Logout vb. için)
    protected void changeScene(Node sourceNode, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            if (title != null) stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load: " + fxmlPath);
        }
    }
}