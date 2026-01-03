package main.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MessageThread {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty customer = new SimpleStringProperty();
    private final StringProperty owner = new SimpleStringProperty();

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getCustomer() { return customer.get(); }
    public void setCustomer(String value) { customer.set(value); }
    public StringProperty customerProperty() { return customer; }

    public String getOwner() { return owner.get(); }
    public void setOwner(String value) { owner.set(value); }
    public StringProperty ownerProperty() { return owner; }
}
