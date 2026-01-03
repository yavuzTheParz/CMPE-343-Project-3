package main.controllers;

import main.dao.OrderDAO;
import main.dao.OwnerDAO;
import main.dao.ProductDAO;
import main.dao.MessageDAO;
import main.dao.RatingDAO;
import main.dao.CouponDAO;
import main.models.Coupon;
import main.models.Product;
import main.models.Order;
import main.models.UserSession;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*; // JavaFX Kontrolleri (Swing değil!)
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import java.util.Map;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import main.models.MessageThread;
import main.models.Message;
import main.models.Rating;
import java.util.List;

// BUNLAR KALSIN VEYA EKLENSİN (Doğru olanlar bunlar) ✅
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.File;

public class OwnerController extends BaseController {

    /**
     * Controller for owner (admin) UI: manage products, carriers, orders,
     * messages and system settings.
     */

    // --- PRODUCT TAB ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colStock;
    @FXML private TableColumn<Product, Double> colThreshold;
    @FXML private TextField nameField, priceField, stockField, thresholdField;
    @FXML private ComboBox<String> categoryBox;
    
    private File selectedImageFile;
    @FXML private Label imagePathLabel;

    // --- CARRIER TAB ---
    @FXML private ListView<String> carrierList;
    @FXML private TextField newCarrierName, newCarrierPass;

    // --- ORDERS & RATINGS TAB ---
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colOrderNum;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colOrderDate;
    @FXML private TableColumn<Order, Double> colTotalPrice;
    @FXML private TableColumn<Order, String> colOrderStatus;
    @FXML private TableColumn<Order, String> colOrderCarrier;
    @FXML private TableColumn<Order, String> colRating;
    @FXML private TableColumn<Order, String> colReview;

    // --- REPORTS TAB ---
    @FXML private BarChart<String, Number> salesChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label totalRevenueLabel;
    @FXML private Label completedOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label activeCarriersLabel;
    @FXML private ListView<String> topCarriersListView;

    // --- MESSAGES & SETTINGS ---
    @FXML private ListView<String> messageList;
    @FXML private VBox chatMessagesBox;
    @FXML private TextArea replyField;
    @FXML private TextField loyaltyField;

    @FXML
    public void initialize() {
        setupProductTab();
        setupOrdersTab();
        refreshAllTabs();
        // load loyalty setting if present
        try {
            String val = OwnerDAO.getSetting("loyalty_discount");
            if (val != null) loyaltyField.setText(val);
        } catch (Exception e) { /* ignore */ }
    }

    private void refreshAllTabs() {
        productTable.setItems(ProductDAO.getAllProducts());
        carrierList.setItems(OwnerDAO.getAllCarriers());
        loadAllOrders(); // Load orders into both table and list
        salesChart.getData().clear();
        salesChart.getData().add(OwnerDAO.getProductSalesChart());
        updateAnalytics(); // Update stats and top carriers
        loadMessages();
    }
    
    private void updateAnalytics() {
        // Calculate statistics
        ObservableList<Order> allOrders = OrderDAO.getAllOrders();
        
        // Total Revenue
        double totalRevenue = allOrders.stream()
            .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) || "DELIVERED".equalsIgnoreCase(o.getStatus()))
            .mapToDouble(Order::getTotalPrice)
            .sum();
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
        
        // Completed Orders Count
        long completedCount = allOrders.stream()
            .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) || "DELIVERED".equalsIgnoreCase(o.getStatus()))
            .count();
        completedOrdersLabel.setText(String.valueOf(completedCount));
        
        // Pending Orders Count
        long pendingCount = allOrders.stream()
            .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()) || "IN_DELIVERY".equalsIgnoreCase(o.getStatus()))
            .count();
        pendingOrdersLabel.setText(String.valueOf(pendingCount));
        
        // Active Carriers Count
        int carrierCount = OwnerDAO.getAllCarriers().size();
        activeCarriersLabel.setText(String.valueOf(carrierCount));
        
        // Top Carriers by Rating
        loadTopCarriers();
    }
    
    private void loadTopCarriers() {
        if (topCarriersListView == null) return;
        
        ObservableList<String> carriers = OwnerDAO.getAllCarriers();
        Map<String, Double> carrierRatings = new java.util.HashMap<>();
        Map<String, Integer> carrierCounts = new java.util.HashMap<>();
        
        // Calculate average ratings for each carrier
        for (String carrier : carriers) {
            ObservableList<Order> carrierOrders = OrderDAO.getAllOrders().filtered(
                o -> carrier.equals(o.getCarrier()) && o.getCarrierRating() > 0
            );
            
            if (!carrierOrders.isEmpty()) {
                double avgRating = carrierOrders.stream()
                    .mapToInt(Order::getCarrierRating)
                    .average()
                    .orElse(0.0);
                carrierRatings.put(carrier, avgRating);
                carrierCounts.put(carrier, carrierOrders.size());
            }
        }
        
        // Sort by rating and create display list
        ObservableList<String> topCarriers = FXCollections.observableArrayList();
        carrierRatings.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(5)
            .forEach(entry -> {
                String carrier = entry.getKey();
                double rating = entry.getValue();
                int count = carrierCounts.get(carrier);
                String stars = "★".repeat((int)Math.round(rating)) + "☆".repeat(5 - (int)Math.round(rating));
                topCarriers.add(String.format("%s | %s (%.1f) | %d deliveries", 
                    carrier, stars, rating, count));
            });
        
        if (topCarriers.isEmpty()) {
            topCarriers.add("No rated carriers yet");
        }
        
        topCarriersListView.setItems(topCarriers);
    }

    private void setupOrdersTab() {
        if (ordersTable == null) return; // Tab not loaded yet
        
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("username"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colOrderCarrier.setCellValueFactory(new PropertyValueFactory<>("carrier"));
        
        colOrderDate.setCellValueFactory(cellData -> {
            try {
                Timestamp ts = cellData.getValue().getOrderDate();
                if (ts != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    return new SimpleStringProperty(sdf.format(ts));
                }
            } catch (Exception e) { }
            return new SimpleStringProperty("N/A");
        });
        
        colRating.setCellValueFactory(cellData -> {
            int rating = cellData.getValue().getCarrierRating();
            if (rating > 0) {
                String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
                return new SimpleStringProperty(stars + " (" + rating + ")");
            }
            return new SimpleStringProperty("-");
        });
        
        colReview.setCellValueFactory(cellData -> {
            String review = cellData.getValue().getCarrierReview();
            if (review != null && !review.isEmpty()) {
                return new SimpleStringProperty(review.length() > 50 ? review.substring(0, 47) + "..." : review);
            }
            return new SimpleStringProperty("No review");
        });
        
        loadAllOrders();
    }
    
    private void loadAllOrders() {
        if (ordersTable != null) {
            ordersTable.setItems(OrderDAO.getAllOrders());
        }
    }

    private void setupProductTab() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price")); 
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoryType()));
        
        categoryBox.getItems().addAll("Fruit", "Vegetable");
        
        productTable.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                priceField.setText(String.format("%.2f", newVal.getPrice()));
                stockField.setText(String.format("%.2f", newVal.getStock()));
                thresholdField.setText(String.format("%.2f", newVal.getThreshold()));
                categoryBox.setValue(newVal.getCategoryType());
                
                selectedImageFile = null;
                if (imagePathLabel != null) imagePathLabel.setText("No new image selected");
            }
        });
    }
    
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(productTable.getScene().getWindow());
        
        if (selectedImageFile != null) {
            if (imagePathLabel != null) imagePathLabel.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void handleAddProduct() {
        if (selectedImageFile == null) {
            showAlert("Error", "You MUST select an image to add a product!");
            return;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            double stock = Double.parseDouble(stockField.getText());
            double threshold = Double.parseDouble(thresholdField.getText());
            if (price <= 0) { showAlert("Error", "Price must be positive."); return; }
            if (stock < 0) { showAlert("Error", "Stock cannot be negative."); return; }
            if (threshold <= 0) { showAlert("Error", "Threshold must be positive."); return; }

            ProductDAO.addProduct(
                nameField.getText(), 
                categoryBox.getValue(), 
                price,
                stock,
                threshold,
                selectedImageFile
            );
            
            refreshAllTabs();
            showAlert("Success", "Product Added!");
            selectedImageFile = null;
            if (imagePathLabel != null) imagePathLabel.setText("No image selected");
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Check your inputs! Make sure price, stock and threshold are numbers.");
        } catch (Exception e) {
            showAlert("Error", "Failed to add product: " + e.getMessage());
        }
    }
    
    // OwnerController.java içindeki handleUpdateProduct metodunu bununla değiştirin:
@FXML
private void handleUpdateProduct() {
    Product p = productTable.getSelectionModel().getSelectedItem();
    if (p == null) {
        showAlert("Warning", "Please select a product from the table first.");
        return;
    }

    try {
        String name = nameField.getText();
        String category = categoryBox.getValue();
        double price = Double.parseDouble(priceField.getText());
        double stock = Double.parseDouble(stockField.getText());
        double threshold = Double.parseDouble(thresholdField.getText());
        if (name == null || name.trim().isEmpty()) { showAlert("Error", "Name cannot be empty."); return; }
        if (category == null || category.trim().isEmpty()) { showAlert("Error", "Category required."); return; }
        if (price <= 0) { showAlert("Error", "Price must be positive."); return; }
        if (stock < 0) { showAlert("Error", "Stock cannot be negative."); return; }
        if (threshold <= 0) { showAlert("Error", "Threshold must be positive."); return; }

        ProductDAO.updateProduct(
            p.getId(),
            name,
            category,
            price,
            stock,
            threshold,
            selectedImageFile // may be null
        );

        refreshAllTabs();
        showAlert("Success", "Product Updated!");
        selectedImageFile = null;
        if (imagePathLabel != null) imagePathLabel.setText("No new image selected");

    } catch (NumberFormatException e) {
        showAlert("Error", "Invalid numeric values! Please check price and stock.");
    } catch (Exception e) {
        showAlert("Error", "Update failed: " + e.getMessage());
    }
}

    @FXML
    private void handleDeleteProduct() {
        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p != null) { 
            ProductDAO.deleteProduct(p.getId()); 
            refreshAllTabs(); 
            showAlert("Deleted", "Product removed successfully.");
        }
    }

    @FXML
    private void handleHireCarrier() {
        if (OwnerDAO.hireCarrier(newCarrierName.getText(), newCarrierPass.getText())) {
            showAlert("Hired", "New carrier added.");
            newCarrierName.clear(); newCarrierPass.clear();
            refreshAllTabs();
        } else { showAlert("Error", "Username might be taken."); }
    }

    @FXML
    private void handleFireCarrier() {
        String selected = carrierList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Fire Carrier");
            confirmAlert.setHeaderText("Are you sure?");
            confirmAlert.setContentText("Do you really want to fire " + selected + "?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    OwnerDAO.fireCarrier(selected);
                    refreshAllTabs();
                    showAlert("Fired", "Carrier has been fired.");
                }
            });
        }
    }

    private ObservableList<Map<String, String>> currentMessages;
    private List<MessageThread> currentThreads;

    private void loadMessages() {
        String owner = "admin";
        currentThreads = MessageDAO.getThreadsForOwner(owner);
        messageList.getItems().clear();
        for (MessageThread t : currentThreads) {
            List<Message> msgs = MessageDAO.getMessagesForThread(t.getId());
            String last = msgs.isEmpty() ? "(no messages)" : msgs.get(msgs.size()-1).getContent();
            messageList.getItems().add("From: " + t.getCustomer() + " | " + last);
        }
    }

    @FXML
    private void handleReply() {
        int index = messageList.getSelectionModel().getSelectedIndex();
        if (index >= 0 && currentThreads != null && index < currentThreads.size()) {
            MessageThread thread = currentThreads.get(index);
            Message m = new Message();
            m.setThreadId(thread.getId());
            m.setSender("admin");
            m.setContent(replyField.getText());
            m.setRead(false);
            boolean ok = MessageDAO.addMessage(m);
            replyField.clear();
            if (ok) {
                // Refresh the chat view to show the new message immediately
                handleOpenThread();
                loadMessages();
            } else {
                showAlert("Error", "Reply failed.");
            }
        }
    }

    @FXML
    private void handleOpenThread() {
        int index = messageList.getSelectionModel().getSelectedIndex();
        if (index >= 0 && currentThreads != null && index < currentThreads.size()) {
            MessageThread thread = currentThreads.get(index);
            List<Message> msgs = MessageDAO.getMessagesForThread(thread.getId());
            
            // Clear and populate the chat messages box
            if (chatMessagesBox != null) {
                chatMessagesBox.getChildren().clear();
                
                for (Message m : msgs) {
                    // Create message bubble
                    VBox messageBubble = new VBox(5);
                    messageBubble.setStyle("-fx-padding: 12; -fx-background-color: " + 
                        (m.getSender().equals(thread.getCustomer()) ? "#E8EAF6" : "#C5E1A5") + 
                        "; -fx-background-radius: 15; -fx-max-width: 400;");
                    
                    Label senderLabel = new Label(m.getSender());
                    senderLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #455A64;");
                    
                    Label contentLabel = new Label(m.getContent());
                    contentLabel.setWrapText(true);
                    contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #263238;");
                    
                    messageBubble.getChildren().addAll(senderLabel, contentLabel);
                    
                    // Align based on sender
                    HBox messageRow = new HBox();
                    if (m.getSender().equals(thread.getCustomer())) {
                        messageRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    } else {
                        messageRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    }
                    messageRow.getChildren().add(messageBubble);
                    
                    chatMessagesBox.getChildren().add(messageRow);
                }
            }
        }
    }

    @FXML
    private void handleMarkRead() {
        // Not implemented: could update is_read flags. For now reload to reflect latest.
        loadMessages();
        showAlert("Done", "Refreshed messages.");
    }

    @FXML
    private void handleShowRatings() {
        String selected = carrierList.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("No Carrier", "Select a carrier first."); return; }
        
        ObservableList<String> ratings = OrderDAO.getCarrierRatings(selected);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ratings for " + selected);
        alert.setHeaderText("Carrier Performance Reviews");
        
        if (ratings.isEmpty()) {
            alert.setContentText("This carrier has no ratings yet.\n\nRatings will appear here after customers rate completed deliveries.");
            alert.showAndWait();
            return;
        }
        
        ListView<String> ratingList = new ListView<>(ratings);
        ratingList.setPrefHeight(400);
        ratingList.setPrefWidth(650);
        ratingList.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        
        alert.getDialogPane().setContent(ratingList);
        alert.getDialogPane().setPrefWidth(700);
        alert.showAndWait();
    }

    @FXML
    private void handleManageCoupons() {
        List<Coupon> list = CouponDAO.getAllCoupons();
        StringBuilder sb = new StringBuilder();
        for (Coupon c : list) {
            sb.append(c.getCode()).append(" | %:").append(c.getDiscountPercent()).append(" | fix:").append(c.getFixedAmount())
              .append(" | min:").append(c.getMinCartValue()).append(" | used:").append(c.getUsedCount()).append("/" ).append(c.getUsageLimit()).append(" | active:").append(c.isActive()).append("\n");
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Coupons");
        a.setHeaderText("Existing coupons");
        TextArea ta = new TextArea(sb.toString()); ta.setEditable(false); ta.setWrapText(true);
        a.getDialogPane().setContent(ta);
        a.showAndWait();
    }

    @FXML
    private void handleCreateCoupon() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Create Coupon");
        d.setHeaderText("Enter coupon code:");
        d.setContentText("Code:");
        d.showAndWait().ifPresent(code -> {
            try {
                TextInputDialog pd = new TextInputDialog("0"); pd.setHeaderText("Discount percent (0-100)"); pd.setTitle("Percent"); pd.setContentText("Percent:");
                double pct = Double.parseDouble(pd.showAndWait().orElse("0"));
                TextInputDialog fd = new TextInputDialog("0"); fd.setHeaderText("Fixed amount (TL)"); fd.setTitle("Fixed"); fd.setContentText("Fixed:");
                double fix = Double.parseDouble(fd.showAndWait().orElse("0"));
                TextInputDialog mv = new TextInputDialog("0"); mv.setHeaderText("Minimum cart value"); mv.setTitle("Min"); mv.setContentText("Min:");
                double min = Double.parseDouble(mv.showAndWait().orElse("0"));
                TextInputDialog ul = new TextInputDialog("0"); ul.setHeaderText("Usage limit (0 = unlimited)"); ul.setTitle("Usage"); ul.setContentText("Limit:");
                int limit = Integer.parseInt(ul.showAndWait().orElse("0"));

                Coupon c = new Coupon();
                c.setCode(code);
                c.setDiscountPercent(pct);
                c.setFixedAmount(fix);
                c.setMinCartValue(min);
                c.setUsageLimit(limit);
                c.setActive(true);
                boolean ok = CouponDAO.createCoupon(c);
                if (ok) { showAlert("Success", "Coupon created: " + code); } else { showAlert("Error", "Failed to create coupon."); }
            } catch (NumberFormatException e) { showAlert("Error", "Invalid numeric input for coupon fields."); }
        });
    }

    @FXML
    private void handleUpdateSettings() {
        OwnerDAO.updateSetting("loyalty_discount", loyaltyField.getText());
        showAlert("Updated", "Settings saved.");
    }

    @FXML
    private void handleLogout() {
        UserSession.cleanUserSession();
        if (productTable.getScene() != null) {
            productTable.getScene().getWindow().hide();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            // Normalize inline FXML styles so our stylesheet can style the scene
            main.controllers.BaseController.normalizeStyles(root);
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            if (getClass().getResource("/green-grocer-theme.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/green-grocer-theme.css").toExternalForm());
            }
            stage.setTitle("GreenGrocer Login");
            stage.setScene(scene);
            stage.setWidth(960);
            stage.setHeight(540);
            stage.centerOnScreen();
            stage.show();
        } catch(Exception e) { e.printStackTrace(); }
    }
    
    @FXML
    private void handleRefreshOrders() {
        loadAllOrders();
        showAlert("Refreshed", "Orders list has been updated.");
    }
    
    @FXML
    private void handleViewAllRatings() {
        StringBuilder report = new StringBuilder();
        report.append("CARRIER RATINGS SUMMARY\n");
        report.append("=".repeat(60)).append("\n\n");
        
        ObservableList<String> carriers = OwnerDAO.getAllCarriers();
        if (carriers.isEmpty()) {
            showAlert("No Carriers", "No carriers have been hired yet.");
            return;
        }
        
        boolean hasAnyRatings = false;
        for (String carrier : carriers) {
            ObservableList<String> ratings = OrderDAO.getCarrierRatings(carrier);
            if (!ratings.isEmpty()) {
                hasAnyRatings = true;
                report.append("📦 ").append(carrier).append(" (").append(ratings.size()).append(" ratings)\n");
                report.append("-".repeat(60)).append("\n");
                for (String rating : ratings) {
                    report.append("  ").append(rating).append("\n");
                }
                report.append("\n");
            }
        }
        
        if (!hasAnyRatings) {
            showAlert("No Ratings", "No carriers have been rated yet.\n\nRatings will appear after customers rate completed deliveries.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("All Carrier Ratings");
        alert.setHeaderText("Performance Reviews for All Carriers");
        
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(500);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(750);
        alert.showAndWait();
    }
}