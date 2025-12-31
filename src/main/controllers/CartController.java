package main.controllers;

import main.dao.OrderDAO;
import main.models.Product;
import main.models.ShoppingCart;
import main.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CartController {

    @FXML private TableView<Product> cartTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colQuantity; 
    @FXML private Label totalLabel;
    @FXML private Label discountLabel; // Yeni İndirim Etiketi

    // --- YENİ ALANLAR ---
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<String> deliveryTimeBox;
    // --------------------

    private CustomerController parentController;
    private double finalPrice = 0.0;
    private double discountAmount = 0.0;
    private final double MIN_CART_VALUE = 50.0; // Minimum Tutar

    public void setParentController(CustomerController controller) {
        this.parentController = controller;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupDeliveryInputs();
        calculateTotals();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEffectivePrice()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("stock"));
        cartTable.setItems(ShoppingCart.getItems());
    }

    private void setupDeliveryInputs() {
        // Tarih seçiciye kısıtlama: Bugünden itibaren en fazla 2 gün
        deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today) || date.isAfter(today.plusDays(2)));
            }
        });
        deliveryDatePicker.setValue(LocalDate.now());

        // Saat dilimleri
        deliveryTimeBox.getItems().addAll("09:00", "11:00", "13:00", "15:00", "17:00", "19:00");
        deliveryTimeBox.setValue("13:00");
    }

    private void calculateTotals() {
        double rawTotal = ShoppingCart.getTotalPrice();
        String username = (UserSession.getInstance() != null) ? UserSession.getInstance().getUsername() : "guest";
        
        // --- SADAKAT İNDİRİMİ MANTIĞI ---
        // Eğer müşterinin 3'ten fazla tamamlanmış siparişi varsa %10 İndirim
        int pastOrders = OrderDAO.getCompletedOrderCount(username);
        boolean eligibleForDiscount = pastOrders > 0 && (pastOrders % 3 == 0); // Her 3. siparişten sonra

        if (eligibleForDiscount) {
            discountAmount = rawTotal * 0.10;
            discountLabel.setText("Loyalty Discount (10%): -" + String.format("%.2f", discountAmount) + " TL");
        } else {
            discountAmount = 0.0;
            discountLabel.setText("No discount available (Need 3+ orders)");
        }

        finalPrice = rawTotal - discountAmount;
        totalLabel.setText("Total To Pay: " + String.format("%.2f", finalPrice) + " TL");
    }

    @FXML
    private void handleRemove() {
        Product selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ShoppingCart.removeItem(selected);
            calculateTotals();
        }
    }

    @FXML
    private void handleCheckout() {
        // 1. Sepet Boş mu?
        if (ShoppingCart.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Add products first.");
            return;
        }

        // 2. Minimum Tutar Kontrolü
        if (finalPrice < MIN_CART_VALUE) {
            showAlert(Alert.AlertType.WARNING, "Minimum Order", "Minimum cart value must be " + MIN_CART_VALUE + " TL.");
            return;
        }

        // 3. Tarih Kontrolü
        if (deliveryDatePicker.getValue() == null || deliveryTimeBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Delivery Date", "Please select a delivery date and time.");
            return;
        }
        
        // Tarih ve Saati Birleştir
        LocalTime time = LocalTime.parse(deliveryTimeBox.getValue());
        LocalDateTime deliveryDateTime = LocalDateTime.of(deliveryDatePicker.getValue(), time);
        
        // 48 Saat Kuralı Kontrolü (Ekstra Güvenlik)
        if (deliveryDateTime.isAfter(LocalDateTime.now().plusHours(48))) {
            showAlert(Alert.AlertType.WARNING, "Date Error", "Delivery must be within 48 hours.");
            return;
        }

        // 4. ÖZET EKRANI (Summary)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Total: " + finalPrice + " TL\nDelivery: " + deliveryDateTime + "\nConfirm Order?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String username = UserSession.getInstance().getUsername();
            
            // Fatura Metni Oluştur (Basit PDF simülasyonu)
            String invoiceText = "INVOICE\nCustomer: " + username + "\nDate: " + LocalDate.now() + 
                                 "\nItems: " + ShoppingCart.getItems().size() + 
                                 "\nTotal Paid: " + finalPrice + " TL";

            int orderId = OrderDAO.placeOrderWithDetails(
                username, finalPrice, discountAmount, ShoppingCart.getItems(), deliveryDateTime, invoiceText
            );

            if (orderId != -1) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order Placed! ID: #" + orderId + "\nInvoice generated.");
                ShoppingCart.clear();
                if (parentController != null) parentController.loadProductData();
                cartTable.getScene().getWindow().hide();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Transaction Failed.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}