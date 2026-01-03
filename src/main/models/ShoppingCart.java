package main.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * In-memory shopping cart used during a user's session. Holds products and
 * provides helper methods for total calculation and item management.
 */
public class ShoppingCart {
    // Cart list (static - accessible application-wide)
    private static final ObservableList<Product> items = FXCollections.observableArrayList();

    public static ObservableList<Product> getItems() {
        return items;
    }

    // Merge behavior: if the same product is already in the cart, increment quantity
    public static void addItem(Product product, double quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        // 1. Check if the product already exists in the cart
        for (Product p : items) {
            if (p.getName().equals(product.getName())) {
                // If present: increase the stored quantity (we reuse Product.stock as cart qty)
                double currentQty = p.getStock(); 
                p.setStock(currentQty + quantity); 
                
                // Listeyi yenilemek (Tableview güncellensin diye) için ufak bir hack
                int index = items.indexOf(p);
                items.set(index, p);
                return; // Yeni satır eklemeden çık
            }
        }

        // 2. If not present: create a new cart item (copy) and add it
        // Note: use getEffectivePrice() to reflect current effective price
        Product newItem = new main.models.Product(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getEffectivePrice(), // use effective price (may reflect threshold adjustments)
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