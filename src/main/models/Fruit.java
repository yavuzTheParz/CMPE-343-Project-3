package main.models;

public class Fruit extends Product {

    // Fruit constructor'ı
    public Fruit(int id, String name, double price, double stock, double threshold) {
    super(id, name, "Fruit", price, stock, threshold);
}
}
