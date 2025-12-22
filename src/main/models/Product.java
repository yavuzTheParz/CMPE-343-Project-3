package main.models;

import javafx.beans.property.*;

// Abstract Base Class
public abstract class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty price;
    private final DoubleProperty stock;

    public Product(int id, String name, double price, double stock) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.stock = new SimpleDoubleProperty(stock);
    }

    // Abstract method for Polymorphism
    public abstract String getCategoryType();

    // JavaFX Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty stockProperty() { return stock; }
    
    // Standard Getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public double getStock() { return stock.get(); }
}