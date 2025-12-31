package main.controllers;

import main.dao.OrderDAO;
import main.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyOrdersController {

    @FXML private ListView<String> ordersList;

    @FXML
    public void initialize() {
        refreshList();
    }

    private void refreshList() {
        if (UserSession.getInstance() != null) {
            String username = UserSession.getInstance().getUsername();
            ordersList.setItems(OrderDAO.getOrdersByUser(username));
        }
    }

    @FXML
    private void handleCancelOrder() {
        String selected = ordersList.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order to cancel."); return; }

        int id = parseId(selected);
        if (OrderDAO.cancelOrder(id)) {
            showAlert("Order #" + id + " has been CANCELLED.");
            refreshList();
        } else {
            showAlert("Cannot cancel. Order might be already delivered or too late.");
        }
    }

    @FXML
    private void handleRateCarrier() {
        String selected = ordersList.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a completed order to rate."); return; }
        int id = parseId(selected);
        
        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Rate Carrier");
        dialog.setHeaderText("Rate Order #" + id);
        dialog.setContentText("Enter 1-5 stars:");
        dialog.showAndWait().ifPresent(r -> {
            try {
                OrderDAO.rateOrder(id, Integer.parseInt(r));
                showAlert("Rated successfully!");
            } catch(Exception e) { showAlert("Invalid number."); }
        });
    }

    // --- YENİ EKLENEN: FATURA İNDİRME ---
    @FXML
    private void handleDownloadInvoice() {
        String selected = ordersList.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order to view invoice."); return; }

        int orderId = parseId(selected);
        
        // 1. Veritabanından Fatura Metnini Çek
        String invoiceText = OrderDAO.getInvoiceText(orderId);
        
        if (invoiceText == null || invoiceText.isEmpty()) {
            showAlert("No invoice found for this order.");
            return;
        }

        // 2. Kullanıcıya "Nereye Kaydedeyim?" diye sor
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Invoice");
        fileChooser.setInitialFileName("Invoice_Order_" + orderId + ".txt"); // Hocaya PDF desek de TXT kaydedelim garanti olsun
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File file = fileChooser.showSaveDialog(ordersList.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(invoiceText);
                showAlert("Invoice saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error saving file.");
            }
        }
    }
    // ------------------------------------

    private int parseId(String row) {
        try { return Integer.parseInt(row.split("\\|")[0].replace("ID:", "").trim()); } 
        catch (Exception e) { return -1; }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}