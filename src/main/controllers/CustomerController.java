package main.controllers;

import main.dao.ProductDAO;
import main.models.Product;
import main.models.ShoppingCart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.beans.property.SimpleStringProperty;
import java.util.Optional;
import java.io.IOException;

public class CustomerController extends BaseController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colStock;

    // Arama ve Filtreleme bileşenleri
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;

    // Listeleri yönetmek için değişkenler
    private ObservableList<Product> masterData = FXCollections.observableArrayList();

    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/customer_dashboard.fxml"));
        Parent root = loader.load();
        
        CustomerController controller = loader.getController();
        controller.loadProductData();

        stage.setTitle("Customer Dashboard");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void initialize() {
        // 1. Tablo Sütunlarını Bağla
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategoryType())
        );

        // 2. Filtre Kutusunu Doldur
        categoryFilter.getItems().addAll("All", "Fruit", "Vegetable");
        categoryFilter.setValue("All");

        // 3. Verileri Yükle ve Dinleyicileri (Listeners) Başlat
        loadProductData();
    }

    public void loadProductData() {
        // Veritabanından ham veriyi çek
        masterData.clear();
        masterData.addAll(ProductDAO.getAllProducts());

        // --- FİLTRELEME MANTIĞI (SİHİRLİ KISIM) ---
        
        // 1. Orijinal listeyi bir 'FilteredList' içine sar
        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Arama kutusu her değiştiğinde filtreyi güncelle
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> checkFilter(product, newValue, categoryFilter.getValue()));
        });

        // 3. Kategori kutusu her değiştiğinde filtreyi güncelle
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> checkFilter(product, searchField.getText(), newValue));
        });

        // 4. Filtrelenmiş listeyi sıralanabilir (SortedList) yap (Tıklayınca sıralama bozulmasın diye)
        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());

        // 5. Tabloya bu akıllı listeyi ver
        productTable.setItems(sortedData);
    }

    // Yardımcı Metod: Bir ürün, arama kriterlerine uyuyor mu?
    private boolean checkFilter(Product product, String searchText, String categoryChoice) {
        // 1. Kategori Kontrolü
        boolean categoryMatch = true;
        if (categoryChoice != null && !categoryChoice.equals("All")) {
            // Veritabanındaki ham kategori ismine bak ("Fruit" veya "Vegetable")
            categoryMatch = product.getCategory().equalsIgnoreCase(categoryChoice);
        }

        // 2. İsim Arama Kontrolü
        boolean searchMatch = true;
        if (searchText != null && !searchText.isEmpty()) {
            String lowerCaseFilter = searchText.toLowerCase();
            searchMatch = product.getName().toLowerCase().contains(lowerCaseFilter);
        }

        return categoryMatch && searchMatch;
    }

    // --- BUTON İŞLEVLERİ ---

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert("No Selection", "Please select a product from the table.");
            return;
        }

        if (selectedProduct.getStock() <= 0) {
            showAlert("Out of Stock", "Sorry, this product is currently unavailable.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Adding: " + selectedProduct.getName());
        dialog.setContentText("Enter quantity (kg):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(quantityStr -> {
            try {
                double quantity = Double.parseDouble(quantityStr);
                if (quantity <= 0) {
                    showAlert("Invalid Quantity", "Please enter a positive number.");
                    return;
                }
                if (quantity > selectedProduct.getStock()) {
                    showAlert("Stock Limit", "We only have " + selectedProduct.getStock() + "kg in stock.");
                    return;
                }
                
                ShoppingCart.addItem(selectedProduct, quantity);
                showAlert("Added to Cart", quantity + "kg of " + selectedProduct.getName() + " added to your cart.");

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void openCartScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cart.fxml"));
            Parent root = loader.load();
            main.controllers.CartController cartController = loader.getController();
            cartController.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("My Shopping Cart");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open cart screen.");
        }
    }

    @FXML
    private void openMyOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/my_orders.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("My Past Orders");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open orders screen.");
        }
    }

    @FXML
    private void handleLogout() {
        // BaseController'dan gelen metod
        // Not: productTable bir Node olduğu için sahne değişiminde referans olarak kullanılabilir
        changeScene(productTable, "/login.fxml", "Login");
    }
}