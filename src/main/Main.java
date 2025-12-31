package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent; // <-- BURASI DEĞİŞTİ (StackPane yerine Parent)
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

   @Override
    public void start(Stage primaryStage) {
        try {
            // Veritabanını başlat
            DatabaseAdapter.connect();
            DatabaseAdapter.createTables(); // Tablolar yoksa oluştur

            // Login ekranını yükle
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(root);
            
            // --- YENİ EKLENEN KISIM: CSS YÜKLEME ---
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            // ---------------------------------------

            primaryStage.setTitle("Greengrocer Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}