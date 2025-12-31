package main.controllers;

import main.dao.OrderDAO;
import main.dao.OwnerDAO;
import main.dao.ProductDAO;
import main.models.Product;
import main.models.UserSession;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser; // Resim seçimi için
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
// --- DÜZELTİLEN İMPORTLAR ---
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.util.Map;
import java.io.File;

public class OwnerController extends BaseController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colStock;
    @FXML private TableColumn<Product, Double> colThreshold;
    @FXML private TextField nameField, priceField, stockField, thresholdField;
    @FXML private ComboBox<String> categoryBox;
    
    // Resim Seçimi
    private File selectedImageFile;
    @FXML private Label imagePathLabel;

    @FXML private ListView<String> carrierList;
    @FXML private TextField newCarrierName, newCarrierPass;

    @FXML private ListView<String> allOrdersList;

    @FXML private BarChart<String, Number> salesChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private ListView<String> messageList;
    @FXML private TextArea replyField;
    @FXML private TextField loyaltyField;

    @FXML
    public void initialize() {
        setupProductTab();
        refreshAllTabs();
    }

    private void refreshAllTabs() {
        productTable.setItems(ProductDAO.getAllProducts());
        carrierList.setItems(OwnerDAO.getAllCarriers());
        allOrdersList.getItems().clear();
        allOrdersList.getItems().add("Order Log System Active...");
        salesChart.getData().clear();
        salesChart.getData().add(OwnerDAO.getProductSalesChart());
        loadMessages();
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
                priceField.setText(String.valueOf(newVal.getPrice()));
                stockField.setText(String.valueOf(newVal.getStock()));
                thresholdField.setText(String.valueOf(newVal.getThreshold()));
                categoryBox.setValue(newVal.getCategoryType().contains("Fruit") ? "Fruit" : "Vegetable");
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
        try {
            ProductDAO.addProduct(nameField.getText(), categoryBox.getValue(), 
                Double.parseDouble(priceField.getText()), Double.parseDouble(stockField.getText()), 
                Double.parseDouble(thresholdField.getText()), selectedImageFile);
            
            refreshAllTabs();
            showAlert("Success", "Product Added!");
            
            selectedImageFile = null;
            if (imagePathLabel != null) imagePathLabel.setText("No image selected");
            
        } catch (Exception e) { showAlert("Error", "Check inputs."); }
    }
    
    @FXML
    private void handleUpdateProduct() {
        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) return;
        try {
            ProductDAO.updateProduct(p.getId(), Double.parseDouble(priceField.getText()), 
                 Double.parseDouble(stockField.getText()), Double.parseDouble(thresholdField.getText()));
            refreshAllTabs();
        } catch (Exception e) { showAlert("Error", "Invalid inputs"); }
    }

    @FXML
    private void handleDeleteProduct() {
        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p != null) { ProductDAO.deleteProduct(p.getId()); refreshAllTabs(); }
    }

    @FXML
    private void handleHireCarrier() {
        if (OwnerDAO.hireCarrier(newCarrierName.getText(), newCarrierPass.getText())) {
            showAlert("Hired", "Carrier added.");
            newCarrierName.clear(); newCarrierPass.clear();
            refreshAllTabs();
        } else { showAlert("Error", "Username taken?"); }
    }

    @FXML
    private void handleFireCarrier() {
        String selected = carrierList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            OwnerDAO.fireCarrier(selected);
            refreshAllTabs();
            showAlert("Fired", "Carrier removed.");
        }
    }

    private ObservableList<Map<String, String>> currentMessages;

    private void loadMessages() {
        currentMessages = OwnerDAO.getMessages();
        messageList.getItems().clear();
        for (Map<String, String> m : currentMessages) {
            String status = (m.get("reply") == null) ? "[NEW]" : "[REPLIED]";
            messageList.getItems().add(status + " From: " + m.get("sender") + " | " + m.get("content"));
        }
    }

    @FXML
    private void handleReply() {
        int index = messageList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            String id = currentMessages.get(index).get("id");
            OwnerDAO.replyMessage(Integer.parseInt(id), replyField.getText());
            replyField.clear();
            loadMessages();
            showAlert("Sent", "Reply sent.");
        }
    }

    @FXML
    private void handleUpdateSettings() {
        OwnerDAO.updateSetting("loyalty_discount", loyaltyField.getText());
        showAlert("Updated", "Loyalty discount updated.");
    }

    @FXML
    private void handleLogout() {
        UserSession.cleanUserSession();
        if (productTable.getScene() != null) {
            productTable.getScene().getWindow().hide();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            new Stage().setScene(new Scene(loader.load())).show();
        } catch(Exception e) { e.printStackTrace(); }
    }
}