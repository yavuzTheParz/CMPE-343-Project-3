package main.models;

import javafx.beans.property.*;

public class CartItem {
    private final Product product;
    private final DoubleProperty quantity;
    private final DoubleProperty totalPrice;

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = new SimpleDoubleProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(product.getPrice() * quantity);
    }

    // Getters
    public Product getProduct() { return product; }
    
    public double getQuantity() { return quantity.get(); }
    public void setQuantity(double qty) { 
        this.quantity.set(qty);
        this.totalPrice.set(qty * product.getPrice());
    }

    public double getTotalPrice() { return totalPrice.get(); }

    // Properties for TableView
    public StringProperty productNameProperty() { return product.nameProperty(); }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
}