package main.controllers;

import main.dao.OrderDAO;
import main.models.CarrierOrderModel; // YENİ IMPORT
import main.models.UserSession;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;

public class CarrierController extends BaseController {

    @FXML private Label welcomeLabel;

    @FXML private TableView<CarrierOrderModel> availableTable;
    @FXML private TableView<CarrierOrderModel> currentTable;
    @FXML private TableView<CarrierOrderModel> completedTable;

    @FXML private TableColumn<CarrierOrderModel, Integer> colAvId;
    @FXML private TableColumn<CarrierOrderModel, String> colAvCustomer;
    @FXML private TableColumn<CarrierOrderModel, String> colAvAddress;
    @FXML private TableColumn<CarrierOrderModel, String> colAvProducts;
    @FXML private TableColumn<CarrierOrderModel, Double> colAvTotal;
    @FXML private TableColumn<CarrierOrderModel, String> colAvDate;

    @FXML private TableColumn<CarrierOrderModel, Integer> colCurId;
    @FXML private TableColumn<CarrierOrderModel, String> colCurCustomer;
    @FXML private TableColumn<CarrierOrderModel, String> colCurAddress;
    @FXML private TableColumn<CarrierOrderModel, String> colCurProducts;
    @FXML private TableColumn<CarrierOrderModel, Double> colCurTotal;

    @FXML private TableColumn<CarrierOrderModel, Integer> colComId;
    @FXML private TableColumn<CarrierOrderModel, String> colComCustomer;
    @FXML private TableColumn<CarrierOrderModel, Double> colComTotal;
    @FXML private TableColumn<CarrierOrderModel, String> colComDate;

    @FXML
    public void initialize() {
        if (UserSession.getInstance() != null) {
            welcomeLabel.setText("Carrier: " + UserSession.getInstance().getUsername());
        }
        setupTables();
        refreshAllTables();
    }

    private void setupTables() {
        colAvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAvCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAvAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAvProducts.setCellValueFactory(new PropertyValueFactory<>("products"));
        colAvTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colAvDate.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        colCurId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCurCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCurAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCurProducts.setCellValueFactory(new PropertyValueFactory<>("products"));
        colCurTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        colComId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colComCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colComTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colComDate.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
    }

    private void refreshAllTables() {
        String me = UserSession.getInstance().getUsername();
        availableTable.setItems(OrderDAO.getOrdersForCarrier("PENDING", null));
        currentTable.setItems(OrderDAO.getOrdersForCarrier("IN_DELIVERY", me));
        completedTable.setItems(OrderDAO.getOrdersForCarrier("COMPLETED", me));
    }

    @FXML
    private void handleTakeOrder() {
        CarrierOrderModel selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order from 'Available' tab."); return; }

        String me = UserSession.getInstance().getUsername();
        if (OrderDAO.assignOrderToCarrier(selected.getId(), me)) {
            showAlert("Order #" + selected.getId() + " assigned to you!");
            refreshAllTables();
        } else {
            showAlert("Could not take order.");
        }
    }

    @FXML
    private void handleCompleteDelivery() {
        CarrierOrderModel selected = currentTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order from 'My Active Deliveries' tab."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Confirm delivery for Order #" + selected.getId() + "?", ButtonType.YES, ButtonType.NO);
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (OrderDAO.completeOrderDelivery(selected.getId())) {
                    showAlert("Order Completed! Great job.");
                    refreshAllTables();
                } else {
                    showAlert("Error completing order.");
                }
            }
        });
    }

    @FXML
    private void handleLogout() {
        UserSession.cleanUserSession();
        availableTable.getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}