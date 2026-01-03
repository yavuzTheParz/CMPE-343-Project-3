package main.models;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class Order {
    private final IntegerProperty id;
    private final StringProperty username;
    private final DoubleProperty totalPrice;
    private final ObjectProperty<Timestamp> orderDate;
    private final StringProperty status;
    private final StringProperty carrier;
    private final IntegerProperty carrierRating;
    private final StringProperty carrierReview;

    public Order(int id, String username, double totalPrice, Timestamp orderDate, String status, String carrier) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.status = new SimpleStringProperty(status);
        this.carrier = new SimpleStringProperty(carrier);
        this.carrierRating = new SimpleIntegerProperty(0);
        this.carrierReview = new SimpleStringProperty("");
    }
    
    public Order(int id, String username, double totalPrice, Timestamp orderDate, String status, String carrier, int rating, String review) {
        this(id, username, totalPrice, orderDate, status, carrier);
        this.carrierRating.set(rating);
        this.carrierReview.set(review != null ? review : "");
    }

    // Getter Metodları
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public double getTotalPrice() { return totalPrice.get(); }
    public Timestamp getOrderDate() { return orderDate.get(); }
    public String getStatus() { return status.get(); }
    public String getCarrier() { return carrier.get(); }
    public int getCarrierRating() { return carrierRating.get(); }
    public String getCarrierReview() { return carrierReview.get(); }

    // Property Erişimcileri (TableView İçin)
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public ObjectProperty<Timestamp> orderDateProperty() { return orderDate; }
    public StringProperty statusProperty() { return status; }
    public IntegerProperty carrierRatingProperty() { return carrierRating; }
    public StringProperty carrierReviewProperty() { return carrierReview; }
}