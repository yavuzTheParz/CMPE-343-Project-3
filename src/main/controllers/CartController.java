package main.controllers;

import main.dao.OrderDAO;
import main.dao.UserDAO;
import main.dao.CouponDAO;
import main.models.Coupon;
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
import java.util.Map;

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

    /**
     * Calculate and update total and discount labels shown in the cart.
     */

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEffectivePrice()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("stock"));
        cartTable.setItems(ShoppingCart.getItems());
    }

    private void setupDeliveryInputs() {
        // Limit date picker: from today up to 2 days ahead
        deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today) || date.isAfter(today.plusDays(2)));
            }
        });
        deliveryDatePicker.setValue(LocalDate.now());

        // Time slots
        String[] slots = new String[]{"09:00", "11:00", "13:00", "15:00", "17:00", "19:00"};
        deliveryTimeBox.getItems().addAll(slots);

        // Helper to refresh time slots when date changes
        java.util.function.Consumer<java.time.LocalDate> refreshSlots = (selectedDate) -> {
            deliveryTimeBox.getItems().clear();
            java.time.LocalTime now = java.time.LocalTime.now();
            java.time.LocalDate today = java.time.LocalDate.now();
            for (String s : slots) {
                java.time.LocalTime slotTime = java.time.LocalTime.parse(s);
                if (selectedDate.equals(today)) {
                    if (!now.isAfter(slotTime)) { // include slot if now <= slotTime
                        deliveryTimeBox.getItems().add(s);
                    }
                } else {
                    deliveryTimeBox.getItems().add(s);
                }
            }
            if (deliveryTimeBox.getItems().isEmpty()) {
                // no slots left today, move to next day first slot
                java.time.LocalDate next = today.plusDays(1);
                deliveryDatePicker.setValue(next);
                deliveryTimeBox.getItems().add(slots[0]);
                deliveryTimeBox.setValue(slots[0]);
            } else if (!deliveryTimeBox.getItems().isEmpty()) {
                // Safe access - only if items exist
                deliveryTimeBox.setValue(deliveryTimeBox.getItems().get(0));
            }
        };

        // initialize defaults
        java.time.LocalDate initDate = java.time.LocalDate.now();
        refreshSlots.accept(initDate);
        deliveryDatePicker.setValue(initDate);

        // when date changes, refresh available time slots
        deliveryDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) refreshSlots.accept(newDate);
        });
    }

    private void calculateTotals() {
        double rawTotal = ShoppingCart.getTotalPrice();
        String username = (UserSession.getInstance() != null) ? UserSession.getInstance().getUsername() : "guest";
        
        // --- Loyalty discount logic ---
        // If the customer has completed 3 or more orders, they earn a 10% discount
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
        // 1. Is the cart empty?
        if (ShoppingCart.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Add products first.");
            return;
        }

        // 2. Check if user has an address
        String username = (UserSession.getInstance() != null) ? UserSession.getInstance().getUsername() : null;
        if (username != null) {
            Map<String, String> userDetails = UserDAO.getUserDetails(username);
            String address = userDetails.getOrDefault("address", "").trim();
            if (address.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Address Required", 
                    "Please add your delivery address in your Profile before placing an order.\n\nGo to Profile → Update your address → Then come back to checkout.");
                return;
            }
        }

        // 3. Minimum Tutar Kontrolü
        if (finalPrice < MIN_CART_VALUE) {
            showAlert(Alert.AlertType.WARNING, "Minimum Order", "Minimum cart value must be " + MIN_CART_VALUE + " TL.");
            return;
        }

        // 4. Date selection validation
        if (deliveryDatePicker.getValue() == null || deliveryTimeBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Delivery Date", "Please select a delivery date and time.");
            return;
        }
        
        // Combine date and time into a LocalDateTime
        LocalTime time = LocalTime.parse(deliveryTimeBox.getValue());
        LocalDateTime deliveryDateTime = LocalDateTime.of(deliveryDatePicker.getValue(), time);
        
        // 48-hour rule validation (extra safety)
        if (deliveryDateTime.isAfter(LocalDateTime.now().plusHours(48))) {
            showAlert(Alert.AlertType.WARNING, "Date Error", "Delivery must be within 48 hours.");
            return;
        }

        // Do not allow selecting a past time
        if (deliveryDateTime.isBefore(LocalDateTime.now())) {
            showAlert(Alert.AlertType.WARNING, "Date Error", "Selected delivery time is in the past. Choose a future time.");
            return;
        }

        // Prompt for optional coupon code
        double rawTotal = ShoppingCart.getTotalPrice();
        TextInputDialog couponDialog = new TextInputDialog();
        couponDialog.setTitle("Coupon");
        couponDialog.setHeaderText("Enter coupon code (optional)");
        couponDialog.setContentText("Code:");
        String couponCode = null;
        Coupon appliedCoupon = null;
        String couponInput = couponDialog.showAndWait().orElse("").trim();
        if (!couponInput.isEmpty()) {
            Coupon c = CouponDAO.validateCouponForCustomer(couponInput, (UserSession.getInstance()!=null?UserSession.getInstance().getUsername():"guest"), rawTotal);
            if (c == null) {
                showAlert(Alert.AlertType.WARNING, "Coupon", "Invalid or ineligible coupon.");
            } else {
                appliedCoupon = c;
                couponCode = couponInput;
            }
        }

        // 4. ÖZET EKRANI (Summary)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Total: " + finalPrice + " TL\nDelivery: " + deliveryDateTime + "\nConfirm Order?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            // Reuse the username variable declared at the beginning
            if (username == null) {
                username = UserSession.getInstance().getUsername();
            }
            
            // Generate Professional Invoice
            double couponDiscount = 0.0;
            if (appliedCoupon != null) {
                couponDiscount = rawTotal * (appliedCoupon.getDiscountPercent() / 100.0) + appliedCoupon.getFixedAmount();
                if (couponDiscount < 0) couponDiscount = 0;
                // apply coupon to finalPrice locally
                finalPrice = rawTotal - discountAmount - couponDiscount;
            }

            // Build professional invoice (optimized for PDF width)
            StringBuilder invoice = new StringBuilder();
            invoice.append("================================================\n");
            invoice.append("            GREENGROCER INVOICE\n");
            invoice.append("          Fresh Produce Marketplace\n");
            invoice.append("================================================\n\n");
            
            invoice.append("Invoice Date:   ").append(LocalDate.now()).append("\n");
            invoice.append("Delivery Date:  ").append(deliveryDateTime.toLocalDate())
                   .append(" at ").append(deliveryDateTime.toLocalTime()).append("\n");
            invoice.append("Customer:       ").append(username).append("\n");
            invoice.append("Status:         PENDING\n\n");
            
            invoice.append("------------------------------------------------\n");
            invoice.append("                 ORDER ITEMS\n");
            invoice.append("------------------------------------------------\n\n");
            
            int itemNo = 1;
            for (Product item : ShoppingCart.getItems()) {
                String itemName = item.getName();
                if (itemName.length() > 25) {
                    itemName = itemName.substring(0, 22) + "...";
                }
                invoice.append(String.format("%-3d %-25s", itemNo++, itemName));
                invoice.append(String.format("%.2f x %.2f = %.2f TL\n", 
                    item.getStock(), item.getPrice(), item.getPrice() * item.getStock()));
            }
            
            invoice.append("\n------------------------------------------------\n");
            invoice.append("               PRICE BREAKDOWN\n");
            invoice.append("------------------------------------------------\n\n");
            
            invoice.append(String.format("Subtotal:                    %10.2f TL\n", rawTotal));
            
            if (discountAmount > 0) {
                invoice.append(String.format("Loyalty Discount:           -%10.2f TL\n", discountAmount));
            }
            
            if (couponDiscount > 0) {
                invoice.append(String.format("Coupon (%s):            -%10.2f TL\n", 
                    couponCode != null ? couponCode : "N/A", couponDiscount));
            }
            
            invoice.append("\n================================================\n");
            invoice.append(String.format("TOTAL AMOUNT:                    %10.2f TL\n", finalPrice));
            invoice.append("================================================\n\n");
            
            invoice.append("Thank you for shopping with GreenGrocer!\n");
            invoice.append("Support: support@greengrocer.com\n");

            String invoiceText = invoice.toString();

            int orderId = OrderDAO.placeOrderWithDetails(
                username, finalPrice, discountAmount + couponDiscount, ShoppingCart.getItems(), deliveryDateTime, invoiceText, couponCode
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