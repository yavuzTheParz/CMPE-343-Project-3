package main.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShoppingCart {
    // Sepet listesi (Uygulama boyunca tek ve ortak)
    private static final ObservableList<CartItem> items = FXCollections.observableArrayList();

    public static ObservableList<CartItem> getItems() {
        return items;
    }

    public static void addItem(Product product, double quantity) {
        // Önce sepette bu ürün var mı bakalım
        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                // Varsa miktarını arttır (Merge logic [cite: 110])
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Yoksa yeni ekle
        items.add(new CartItem(product, quantity));
    }

    public static double getTotalAmount() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public static void clear() {
        items.clear();
    }
}