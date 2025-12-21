package main.controllers;  // sample paketinde olduğunu belirtiyoruz

import javafx.stage.Stage;

public class CustomerController {

    public void start(Stage customerStage) {
        // Customer dashboard'ı buraya ekleyeceğiz
        System.out.println("Customer Dashboard opened!");
        customerStage.show();
    }
}
