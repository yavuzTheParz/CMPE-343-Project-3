package main.controllers;

import main.dao.ProductDAO;
import main.models.Product;
import main.models.ShoppingCart;
import main.models.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.control.*; 
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;
import java.io.IOException;

public class CustomerController extends BaseController {

    @FXML private Label welcomeLabel;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterBox;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Product> colImage;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colStock;
    @FXML private TableColumn<Product, Void> colAction;

    private ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (UserSession.getInstance() != null) {
            welcomeLabel.setText("Welcome, " + UserSession.getInstance().getUsername());
        }
        setupTableColumns();
        setupFilters();
        addButtonToTable();
        loadProductData();
    }

    private void setupTableColumns() {
        colImage.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        colImage.setCellFactory(column -> new TableCell<Product, Product>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getImage() == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item.getImage());
                    imageView.setFitWidth(50); 
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEffectivePrice()));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryType()));
    }

    private void setupFilters() {
        categoryFilterBox.getItems().addAll("All", "Fruit", "Vegetable");
        categoryFilterBox.setValue("All");

        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> createPredicate(product, newValue, categoryFilterBox.getValue()));
        });

        categoryFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> createPredicate(product, searchField.getText(), newValue));
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);
        
        productTable.getSortOrder().add(colName);
        colName.setSortType(TableColumn.SortType.ASCENDING);
        productTable.sort();
    }

    private boolean createPredicate(Product product, String searchText, String selectedCategory) {
        boolean matchesSearch = true;
        boolean matchesCategory = true;
        if (searchText != null && !searchText.isEmpty()) {
            matchesSearch = product.getName().toLowerCase().contains(searchText.toLowerCase());
        }
        if (selectedCategory != null && !selectedCategory.equals("All")) {
            matchesCategory = product.getCategory().equalsIgnoreCase(selectedCategory);
        }
        return matchesSearch && matchesCategory;
    }

    public void loadProductData() {
        masterData.clear();
        masterData.addAll(ProductDAO.getAllProducts());
    }

    private void addButtonToTable() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Add ➕");
                    {
                        btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                        btn.setOnAction(event -> {
                            Product product = getTableView().getItems().get(getIndex());
                            handleAddToCart(product);
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { setGraphic(null); } else { setGraphic(btn); }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void handleAddToCart(Product product) {
        if (product.getStock() <= 0) {
            showAlert("Out of Stock", "Sorry, this product is currently unavailable.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + product.getName() + " to your cart");
        dialog.setContentText("Enter quantity (kg):");
        dialog.showAndWait().ifPresent(input -> {
            try {
                double quantity = Double.parseDouble(input);
                if (quantity <= 0) showAlert("Invalid Quantity", "Positive number required.");
                else if (quantity > product.getStock()) showAlert("Insufficient Stock", "Only " + product.getStock() + " kg available.");
                else {
                    ShoppingCart.addItem(product, quantity);
                    showAlert("Added", quantity + " kg added.");
                }
            } catch (NumberFormatException e) { showAlert("Invalid Input", "Enter a valid number."); }
        });
    }

    @FXML private void openCart() { openScreen("/cart.fxml", "My Shopping Cart"); }
    @FXML private void openMyOrders() { openScreen("/my_orders.fxml", "My Past Orders"); }
    @FXML private void openProfile() { openScreen("/profile.fxml", "My Profile"); }

    private void openScreen(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            if (fxml.contains("cart")) {
                CartController cc = loader.getController();
                cc.setParentController(this);
            }
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            if (getClass().getResource("/styles.css") != null)
                stage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); showAlert("Error", "Could not open screen: " + fxml); }
    }

    @FXML
    private void handleLogout() {
        if (UserSession.getInstance() != null) UserSession.cleanUserSession();
        if (welcomeLabel.getScene() != null) welcomeLabel.getScene().getWindow().hide();
        
        // --- DÜZELTİLEN KISIM ---
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage(); // Önce Stage oluştur
            stage.setScene(new Scene(root)); // Sonra Sahneyi ata
            stage.show(); // En son göster
        } catch(Exception e) { e.printStackTrace(); }
        // ------------------------
    }
}