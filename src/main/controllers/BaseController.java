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

    /**
     * Normalize common inline styles (from legacy FXML) into style classes defined in green-grocer-theme.css.
     * This allows us to keep FXML files untouched while providing a consistent theme.
     */
    public static void normalizeStyles(javafx.scene.Parent root) {
        try {
            normalizeNode(root);
        } catch (Exception e) {
            // Don't break navigation on errors here
            e.printStackTrace();
        }
    }

    private static void normalizeNode(javafx.scene.Node node) {
        if (node == null) return;

        String style = node.getStyle();
        if (style != null && !style.isBlank()) {
            // Buttons: map inline background colors to semantic classes
            if (node instanceof javafx.scene.control.Button) {
                String s = style.toLowerCase();
                if (s.contains("#27ae60") || s.contains("#2ecc71")) ((javafx.scene.control.Button) node).getStyleClass().add("primary-button");
                else if (s.contains("#2980b9") || s.contains("#3498db")) ((javafx.scene.control.Button) node).getStyleClass().add("secondary-button");
                else if (s.contains("#c0392b") || s.contains("#e74c3c")) ((javafx.scene.control.Button) node).getStyleClass().add("warn-button");
                else if (s.contains("#f1c40f") || s.contains("#f39c12")) ((javafx.scene.control.Button) node).getStyleClass().add("secondary-button");
            }

            // Top bars (HBox/Pane) with background color -> top-bar
            if (node instanceof javafx.scene.layout.HBox || node instanceof javafx.scene.layout.VBox || node instanceof javafx.scene.layout.BorderPane) {
                if (style.toLowerCase().contains("-fx-background-color")) {
                    node.getStyleClass().add("top-bar");
                }
            }

            // Labels with large font size -> header-label
            if (node instanceof javafx.scene.control.Label && style.contains("font-size")) {
                node.getStyleClass().add("header-label");
            }

            // Clear inline style so stylesheet rules take precedence
            node.setStyle("");
        }

        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent p = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : p.getChildrenUnmodifiable()) {
                normalizeNode(child);
            }
        }
    }

   // BaseController içindeki changeScene metodunu BUL ve bununla DEĞİŞTİR:
    
    protected void changeScene(javafx.scene.Node node, String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = (javafx.stage.Stage) node.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            // --- YENİ EKLENEN KISIM: CSS YÜKLEME ---
            String css = getClass().getResource("/green-grocer-theme.css").toExternalForm();
            scene.getStylesheets().add(css);
            // ---------------------------------------

            // Normalize inline styles from FXML to CSS classes so our theme applies
            normalizeStyles(root);
            
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