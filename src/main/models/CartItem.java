package main.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CartItem {
    private final Product product;
    private final DoubleProperty quantity;

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = new SimpleDoubleProperty(quantity);
    }

    public Product getProduct() {
        return product;
    }

    public double getQuantity() {
        return quantity.get();
    }

    public void setQuantity(double quantity) {
        this.quantity.set(quantity);
    }

    public DoubleProperty quantityProperty() {
        return quantity;
    }

    public StringProperty productNameProperty() {
        // HATA BURADAYDI: Product sınıfında property yok, biz oluşturup veriyoruz.
        return new SimpleStringProperty(product.getName());
    }

    public DoubleProperty priceProperty() {
        return new SimpleDoubleProperty(product.getEffectivePrice());
    }

    public DoubleProperty totalProperty() {
        return new SimpleDoubleProperty(product.getEffectivePrice() * getQuantity());
    }
}