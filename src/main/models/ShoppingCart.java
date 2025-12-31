package main.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShoppingCart {
    // Sepet listesi (Statik, yani her yerden erişilebilir)
    private static final ObservableList<Product> items = FXCollections.observableArrayList();

    public static ObservableList<Product> getItems() {
        return items;
    }

    // --- DÜZELTİLEN KISIM: MERGING (BİRLEŞTİRME) ---
    public static void addItem(Product product, double quantity) {
        // 1. Sepette bu ürün zaten var mı?
        for (Product p : items) {
            if (p.getName().equals(product.getName())) {
                // Varsa: Sadece stok miktarını (bizim için sepetteki miktar) arttır
                // Not: Product sınıfındaki 'stock' alanını geçici olarak sepet miktarı gibi kullanıyoruz.
                double currentQty = p.getStock(); 
                p.setStock(currentQty + quantity); 
                
                // Listeyi yenilemek (Tableview güncellensin diye) için ufak bir hack
                int index = items.indexOf(p);
                items.set(index, p);
                return; // Yeni satır eklemeden çık
            }
        }

        // 2. Yoksa: Yeni ürün oluştur ve ekle
        // (Orijinal nesneyi bozmamak için kopyasını oluşturuyoruz)
        // DİKKAT: Ürün fiyatı 'getEffectivePrice' ile gelmeli (Açgözlü Patron Modu)
        Product newItem = new main.models.Product(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getEffectivePrice(), // Zamlı fiyatı al!
                quantity,                    // Miktar olarak girilen değeri stock hanesine yazıyoruz
                product.getThreshold()
        );
        items.add(newItem);
    }

    public static void removeItem(Product product) {
        items.remove(product);
    }

    public static void clear() {
        items.clear();
    }

    public static double getTotalPrice() {
        double total = 0;
        for (Product p : items) {
            total += p.getPrice() * p.getStock(); // Fiyat * Miktar
        }
        return total;
    }
}