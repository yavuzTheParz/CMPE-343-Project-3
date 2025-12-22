package main.models;

public class Fruit extends Product {
    // Özel alanlar eklenebilir
    public Fruit(int id, String name, double price, double stock) {
        super(id, name, price, stock); // Freshness parametresi kalktı
    }

    @Override
    public String getCategoryType() {
        return "Fruit 🍎";
    }
}