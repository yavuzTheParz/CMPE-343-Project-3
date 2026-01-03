package main.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Rating {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty carrier = new SimpleStringProperty();
    private final StringProperty customer = new SimpleStringProperty();
    private final IntegerProperty rating = new SimpleIntegerProperty();
    private final StringProperty review = new SimpleStringProperty();

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getCarrier() { return carrier.get(); }
    public void setCarrier(String value) { carrier.set(value); }
    public StringProperty carrierProperty() { return carrier; }

    public String getCustomer() { return customer.get(); }
    public void setCustomer(String value) { customer.set(value); }
    public StringProperty customerProperty() { return customer; }

    public int getRating() { return rating.get(); }
    public void setRating(int value) { rating.set(value); }
    public IntegerProperty ratingProperty() { return rating; }

    public String getReview() { return review.get(); }
    public void setReview(String value) { review.set(value); }
    public StringProperty reviewProperty() { return review; }
}
