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

   // BaseController içindeki changeScene metodunu BUL ve bununla DEĞİŞTİR:
    
    protected void changeScene(javafx.scene.Node node, String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = (javafx.stage.Stage) node.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            // --- YENİ EKLENEN KISIM: CSS YÜKLEME ---
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            // ---------------------------------------
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Cannot load page: " + fxmlPath);
        }
    }
}