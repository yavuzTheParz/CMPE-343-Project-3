package main.models;

public class Fruit extends Product {

    // Fruit constructor'ı
    public Fruit(int id, String name, double price, double stock) {
        // 3. parametre olarak "Fruit" gönderiyoruz
        super(id, name, "Fruit", price, stock);
    }
}
