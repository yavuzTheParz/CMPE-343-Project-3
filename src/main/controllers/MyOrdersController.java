package main.controllers;

import main.dao.OrderDAO;
import main.models.Order;
import main.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class MyOrdersController extends BaseController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colStatus;

    @FXML
    public void initialize() {
        // Tablo Sütunlarını Bağla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Verileri Yükle
        loadMyOrders();
    }

    private void loadMyOrders() {
        String currentUser = SessionManager.getCurrentUsername();
        if (currentUser != null) {
            ordersTable.setItems(OrderDAO.getOrdersByUsername(currentUser));
        }
    }

    @FXML
    private void handleRefresh() {
        loadMyOrders();
        // Kullanıcıya küçük bir geri bildirim verelim (Alert değil, konsol veya status bar olabilir ama şimdilik boşver)
        System.out.println("Orders refreshed.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        stage.close();
    }
}