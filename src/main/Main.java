package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent; // <-- BURASI DEĞİŞTİ (StackPane yerine Parent)
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXML dosyasını yükle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));

        // StackPane yerine Parent kullanıyoruz ki VBox gelirse kızmasın
        Parent root = loader.load();

        primaryStage.setTitle("Login Screen");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}