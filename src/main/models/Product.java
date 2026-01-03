package main.models;

import javafx.scene.image.Image;

/**
 * Simple domain model representing a product with price, stock and image.
 */
public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private double stock;
    private double threshold;
    private Image image;

    public Product(int id, String name, String category, double price, double stock, double threshold) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
    }

    // Setter for stock (also used to store cart quantity in some places)
    public void setStock(double stock) {
        this.stock = stock;
    }

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }

    public double getEffectivePrice() {
        if (this.stock <= this.threshold) return this.price * 2;
        return this.price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public double getStock() { return stock; }
    public double getThreshold() { return threshold; }
    public String getCategoryType() { return category; }
}