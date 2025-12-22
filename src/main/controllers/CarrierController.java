package main.controllers;

import main.dao.OrderDAO;
import main.models.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

// BASECONTROLLER'DAN MİRAS ALDI
public class CarrierController extends BaseController {

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colUser;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colStatus;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadOrders();
    }

    private void loadOrders() {
        orderTable.setItems(OrderDAO.getPendingOrders());
    }

    @FXML
    private void handleDeliver() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an order to deliver."); // Atadan gelen metod
            return;
        }

        OrderDAO.updateOrderStatus(selected.getId(), "DELIVERED");
        
        showAlert("Success", "Order #" + selected.getId() + " marked as DELIVERED!");
        loadOrders();
    }

    @FXML
    private void handleLogout() {
        // Atadan gelen changeScene metodu
        changeScene(orderTable, "/login.fxml", "Login");
    }
}