package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent; // <-- BURASI DEĞİŞTİ (StackPane yerine Parent)
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;


public class Main extends Application {

   @Override
    public void start(Stage primaryStage) {
        try {
            // Set up global exception handler to prevent crashes
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                System.err.println("CRITICAL ERROR in thread " + thread.getName() + ":");
                throwable.printStackTrace();
                // Log but don't crash - show error dialog instead
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Application Error");
                    alert.setHeaderText("An unexpected error occurred");
                    alert.setContentText("Error: " + throwable.getMessage() + "\n\nThe application will continue running.");
                    alert.showAndWait();
                });
            });

            // Veritabanını başlat
            DatabaseAdapter.connect();
            DatabaseAdapter.createTables(); // Tablolar yoksa oluştur

            // Login ekranını yükle
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            // Normalize inline styles from FXML so stylesheet rules take effect
            main.controllers.BaseController.normalizeStyles(root);
            Scene scene = new Scene(root);

            // --- Load theme stylesheet if available, otherwise fallback ---
            URL themeCss = getClass().getResource("/green-grocer-theme.css");
            if (themeCss != null) {
                scene.getStylesheets().add(themeCss.toExternalForm());
            } else {
                URL defaultCss = getClass().getResource("/styles.css");
                if (defaultCss != null) {
                    scene.getStylesheets().add(defaultCss.toExternalForm());
                }
            }
            // -------------------------------------------------------------

            primaryStage.setTitle("Greengrocer Management System");
            primaryStage.setScene(scene);
            primaryStage.setWidth(960);
            primaryStage.setHeight(540);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            // Show error dialog instead of crashing
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}