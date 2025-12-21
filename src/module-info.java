module project { // Buraya kendi modül adını yaz (pom.xml veya projede neyse)
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Paketleri JavaFX'e açıyoruz
    opens main to javafx.fxml;
    opens main.controllers to javafx.fxml; 
    
    exports main;
}