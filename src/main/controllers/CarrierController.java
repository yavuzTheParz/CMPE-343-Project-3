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
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

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
        try {
            if (UserSession.getInstance() == null || UserSession.getInstance().getUsername() == null) {
                showAlert("Session expired. Please log in again.");
                return;
            }
            String me = UserSession.getInstance().getUsername();
            availableTable.setItems(OrderDAO.getOrdersForCarrier("PENDING", null));
            currentTable.setItems(OrderDAO.getOrdersForCarrier("IN_DELIVERY", me));
            completedTable.setItems(OrderDAO.getOrdersForCarrier("COMPLETED", me));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error refreshing tables: " + e.getMessage());
        }
    }

    @FXML
    private void handleTakeOrder() {
        try {
            CarrierOrderModel selected = availableTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showAlert("Select an order from 'Available' tab."); return; }

            if (UserSession.getInstance() == null || UserSession.getInstance().getUsername() == null) {
                showAlert("Session expired. Please log in again.");
                return;
            }

            String me = UserSession.getInstance().getUsername();
            if (OrderDAO.assignOrderToCarrier(selected.getId(), me)) {
                showAlert("Order #" + selected.getId() + " assigned to you!");
                refreshAllTables();
            } else {
                // Likely another carrier took it; refresh and inform user
                refreshAllTables();
                showAlert("Could not take order. It may have been assigned to another carrier.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error taking order: " + e.getMessage());
        }
    }

    @FXML
    private void handleCompleteDelivery() {
        CarrierOrderModel selected = currentTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order from 'My Active Deliveries' tab."); return; }

        // Create dialog to get actual delivery date and time
        Dialog<ButtonType> deliveryDialog = new Dialog<>();
        deliveryDialog.setTitle("Complete Delivery");
        deliveryDialog.setHeaderText("Enter actual delivery date and time for Order #" + selected.getId());
        
        // Create date and time pickers
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> timeComboBox = new ComboBox<>();
        
        // Populate time options (00:00 to 23:00)
        for (int hour = 0; hour <= 23; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
            timeComboBox.getItems().add(String.format("%02d:30", hour));
        }
        timeComboBox.setValue(LocalTime.now().getHour() + ":00");
        
        grid.add(new Label("Delivery Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Delivery Time:"), 0, 1);
        grid.add(timeComboBox, 1, 1);
        
        deliveryDialog.getDialogPane().setContent(grid);
        deliveryDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        deliveryDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                LocalDate deliveryDate = datePicker.getValue();
                String timeStr = timeComboBox.getValue();
                
                if (deliveryDate == null || timeStr == null || timeStr.isEmpty()) {
                    showAlert("Please select both date and time.");
                    return;
                }
                
                // Parse time and create LocalDateTime
                LocalTime deliveryTime = LocalTime.parse(timeStr);
                LocalDateTime actualDeliveryDateTime = LocalDateTime.of(deliveryDate, deliveryTime);
                
                // Validate that delivery time is not in the future
                if (actualDeliveryDateTime.isAfter(LocalDateTime.now())) {
                    showAlert("Delivery time cannot be in the future. Please enter the actual past delivery time.");
                    return;
                }
                
                // Confirm the delivery
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Confirm delivery for Order #" + selected.getId() + 
                    "\nDelivered at: " + actualDeliveryDateTime + "?", 
                    ButtonType.YES, ButtonType.NO);
                
                confirm.showAndWait().ifPresent(confirmResponse -> {
                    if (confirmResponse == ButtonType.YES) {
                        if (OrderDAO.completeOrderDelivery(selected.getId(), actualDeliveryDateTime)) {
                            showAlert("Order Completed! Great job.");
                            refreshAllTables();
                        } else {
                            showAlert("Error completing order.");
                        }
                    }
                });
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}