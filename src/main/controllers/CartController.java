package main.controllers;

import main.models.CartItem;
import main.models.ShoppingCart;
import main.dao.OrderDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

// BASECONTROLLER'DAN MİRAS ALDI
public class CartController extends BaseController {

    private CustomerController parentController;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colName;
    @FXML private TableColumn<CartItem, Double> colQuantity;
    @FXML private TableColumn<CartItem, Double> colTotal;
    @FXML private Label totalLabel;

    public void setParentController(CustomerController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        cartTable.setItems(ShoppingCart.getItems());
        updateTotal();
    }

    private void updateTotal() {
        double total = ShoppingCart.getTotalAmount();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    @FXML
    private void handleCheckout() {
        if (ShoppingCart.getItems().isEmpty()) {
            showAlert("Cart Empty", "Your cart is empty!");
            return;
        }

        boolean success = OrderDAO.placeOrder(ShoppingCart.getItems(), ShoppingCart.getTotalAmount());

        if (success) {
            showAlert("Order Received!", "Your order has been placed successfully.");
            
            ShoppingCart.clear();
            updateTotal();
            
            if (parentController != null) {
                parentController.loadProductData(); 
            }
            
        } else {
            showAlert("Transaction Failed", "Purchase failed! Possible reasons:\n1. Not enough stock.\n2. Connection error.");
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) totalLabel.getScene().getWindow();
        stage.close();
    }
}