package main.factories;

import main.models.Fruit;
import main.models.Product;
import main.models.Vegetable;

public class ProductFactory {
    // threshold parametresi eklendi
    public static Product createProduct(int id, String name, String category, double price, double stock, double threshold) {
        if (category == null) return null;

        switch (category.toLowerCase()) {
            case "fruit":
                return new Fruit(id, name, price, stock, threshold);
            case "vegetable":
                return new Vegetable(id, name, price, stock, threshold);
            default:
                return new Product(id, name, category, price, stock, threshold);
        }
    }
}