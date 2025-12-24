package main.models;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty category; // Veritabanındaki ham hali ("Fruit", "Vegetable")
    private final DoubleProperty price;
    private final DoubleProperty stock;

    public Product(int id, String name, String category, double price, double stock) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.price = new SimpleDoubleProperty(price);
        this.stock = new SimpleDoubleProperty(stock);
    }

    // Tabloda görünen EMOJİLİ hali (CustomerController bunu sütunda gösterir)
    public String getCategoryType() {
        String rawCategory = category.get();
        if (rawCategory == null) return "";

        if (rawCategory.equalsIgnoreCase("Fruit")) {
            return "Fruit 🍎";
        } else if (rawCategory.equalsIgnoreCase("Vegetable")) {
            return "Vegetable 🥕"; 
        } else {
            return rawCategory;
        }
    }

    // --- HATAYI ÇÖZEN KISIM BURASI ---
    // Filtreleme için HAM hali (CustomerController bunu filtrelerken kullanır)
    public String getCategory() { 
        return category.get(); 
    }
    // ---------------------------------

    // Diğer Getter Metodları
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public double getStock() { return stock.get(); }

    // Property Erişimcileri (JavaFX için)
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty stockProperty() { return stock; }
}