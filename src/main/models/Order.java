package main.models;

import javafx.beans.property.*;

public class Order {
    private final IntegerProperty id;
    private final StringProperty username;
    private final DoubleProperty totalPrice;
    private final StringProperty date;
    private final StringProperty status;

    public Order(int id, String username, double totalPrice, String date, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
    }

    // JavaFX Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }

    // Getters
    public int getId() { return id.get(); }
    public String getStatus() { return status.get(); }
}