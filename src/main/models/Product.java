package main.models;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final DoubleProperty price;
    private final DoubleProperty stock;
    private final DoubleProperty threshold; // <-- YENİ ÖZELLİK

    // Constructor Güncellendi
    public Product(int id, String name, String category, double price, double stock, double threshold) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.price = new SimpleDoubleProperty(price);
        this.stock = new SimpleDoubleProperty(stock);
        this.threshold = new SimpleDoubleProperty(threshold);
    }

    // --- KRİTİK İŞ MANTIĞI: MÜŞTERİ HANGİ FİYATI GÖRÜR? ---
    // Eğer stok, eşik değerinin altındaysa fiyat 2 katına çıkar! [cite: 55, 56]
    public double getEffectivePrice() {
        if (getStock() <= getThreshold() && getThreshold() > 0) {
            return getPrice() * 2;
        }
        return getPrice();
    }
    // --------------------------------------------------------

    public String getCategoryType() {
        String rawCategory = category.get();
        if (rawCategory == null) return "";
        if (rawCategory.equalsIgnoreCase("Fruit")) return "Fruit 🍎";
        else if (rawCategory.equalsIgnoreCase("Vegetable")) return "Vegetable 🥕";
        else return rawCategory;
    }
    
    // Normal Getter (Patron ham fiyatı görür)
    public String getCategory() { return category.get(); }
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public double getStock() { return stock.get(); }
    public double getThreshold() { return threshold.get(); } // <-- YENİ GETTER

    // Property Erişimcileri
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty stockProperty() { return stock; }
}