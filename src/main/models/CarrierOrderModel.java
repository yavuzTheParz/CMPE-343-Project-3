package main.models;

public class CarrierOrderModel {
    private int id;
    private String customerName;
    private String address;
    private String products;
    private double totalPrice;
    private String deliveryDate;

    public CarrierOrderModel(int id, String customerName, String address, String products, double totalPrice, String deliveryDate) {
        this.id = id;
        this.customerName = customerName;
        this.address = address;
        this.products = products;
        this.totalPrice = totalPrice;
        this.deliveryDate = deliveryDate;
    }

    // Getter Metodları (TableView Bunlara İhtiyaç Duyar)
    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getAddress() { return address; }
    public String getProducts() { return products; }
    public double getTotalPrice() { return totalPrice; }
    public String getDeliveryDate() { return deliveryDate; }
}