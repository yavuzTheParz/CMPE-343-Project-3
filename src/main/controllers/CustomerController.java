package main.controllers;

import main.dao.ProductDAO;
import main.models.Product;
import main.models.ShoppingCart;
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

// BASECONTROLLER'DAN MİRAS ALDI
public class CustomerController extends BaseController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colStock;

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
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        colCategory.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategoryType())
        );
    }

    public void loadProductData() {
        productTable.setItems(ProductDAO.getAllProducts());
    }

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert("No Selection", "Please select a product from the table.");
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
}