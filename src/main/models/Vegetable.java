package main.models;

public class Vegetable extends Product {
    
    // Vegetable constructor'ı
    public Vegetable(int id, String name, double price, double stock, double threshold) {
    super(id, name, "Vegetable", price, stock, threshold);
}
}
