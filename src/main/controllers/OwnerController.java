package main.controllers;

import main.dao.ProductDAO;
import main.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

// BASECONTROLLER'DAN MİRAS ALDI
public class OwnerController extends BaseController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colStock;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextField priceField;
    @FXML private TextField stockField;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryType()));

        categoryBox.getItems().addAll("Fruit", "Vegetable");

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                priceField.setText(String.valueOf(newSelection.getPrice()));
                stockField.setText(String.valueOf(newSelection.getStock()));
                String cat = newSelection.getCategoryType().contains("Fruit") ? "Fruit" : "Vegetable";
                categoryBox.setValue(cat);
            }
        });

        loadData();
    }

    private void loadData() {
        productTable.setItems(ProductDAO.getAllProducts());
    }

    @FXML
    private void handleAdd() {
        try {
            String name = nameField.getText();
            String category = categoryBox.getValue();
            double price = Double.parseDouble(priceField.getText());
            double stock = Double.parseDouble(stockField.getText());

            if (name.isEmpty() || category == null) {
                showAlert("Error", "Name and Category are required!");
                return;
            }

            ProductDAO.addProduct(name, category, price, stock);
            loadData();
            clearFields();
            showAlert("Success", "Product added successfully.");

        } catch (NumberFormatException e) {
            showAlert("Error", "Price and Stock must be numbers.");
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a product to update.");
            return;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            double stock = Double.parseDouble(stockField.getText());

            ProductDAO.updateProduct(selected.getId(), price, stock);
            loadData();
            clearFields();
            showAlert("Success", "Product updated.");

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid inputs.");
        }
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a product to delete.");
            return;
        }

        ProductDAO.deleteProduct(selected.getId());
        loadData();
        clearFields();
        showAlert("Deleted", "Product removed from database.");
    }
    
    @FXML
    private void handleLogout() {
        changeScene(productTable, "/login.fxml", "Login");
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        categoryBox.getSelectionModel().clearSelection();
    }
}