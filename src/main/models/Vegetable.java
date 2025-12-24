package main.models;

public class Vegetable extends Product {
    
    // Vegetable constructor'ı
    public Vegetable(int id, String name, double price, double stock) {
        // HATA BURADAYDI: Artık 3. parametre olarak kategoriyi gönderiyoruz
        super(id, name, "Vegetable", price, stock);
    }
}
