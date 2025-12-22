package main.models;

public class Vegetable extends Product {
    // Özel alanlar eklenebilir
    public Vegetable(int id, String name, double price, double stock) {
        super(id, name, price, stock); // Freshness parametresi kalktı
    }

    @Override
    public String getCategoryType() {
        return "Vegetable 🥦";
    }
}