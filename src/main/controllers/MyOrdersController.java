package main.controllers;

import main.dao.OrderDAO;
import main.models.Order;
import main.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class MyOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, Timestamp> colDate;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colCarrier;
    @FXML private TableColumn<Order, String> colCompletion;

    @FXML
    public void initialize() {
        setupTable();
        refreshList();
    }
    
    private void setupTable() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCarrier.setCellValueFactory(new PropertyValueFactory<>("carrier"));
        
        // Format date column
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDate.setCellFactory(col -> new TableCell<Order, Timestamp>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                    setText(sdf.format(item));
                }
            }
        });
        
        // Completion time - get from database
        colCompletion.setCellValueFactory(cellData -> {
            int orderId = cellData.getValue().getId();
            String status = cellData.getValue().getStatus();
            if ("completed".equalsIgnoreCase(status) || "delivered".equalsIgnoreCase(status)) {
                Timestamp completionTime = OrderDAO.getCompletionTime(orderId);
                if (completionTime != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                    return new javafx.beans.property.SimpleStringProperty(sdf.format(completionTime));
                }
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
    }

    private void refreshList() {
        if (UserSession.getInstance() != null) {
            String username = UserSession.getInstance().getUsername();
            ObservableList<Order> orders = OrderDAO.getOrderObjectsByUser(username);
            ordersTable.setItems(orders);
        }
    }

    @FXML
    private void handleCancelOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order to cancel."); return; }

        int id = selected.getId();
        if (OrderDAO.cancelOrder(id)) {
            showAlert("Order #" + id + " has been CANCELLED.");
            refreshList();
        } else {
            showAlert("Cannot cancel. Order might be already delivered or too late.");
        }
    }

    @FXML
    private void handleRateCarrier() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a completed order to rate."); return; }
        
        if (!"completed".equalsIgnoreCase(selected.getStatus()) && !"delivered".equalsIgnoreCase(selected.getStatus())) {
            showAlert("You can only rate completed orders.");
            return;
        }
        
        if (selected.getCarrier() == null || selected.getCarrier().isEmpty()) {
            showAlert("No carrier assigned to this order.");
            return;
        }
        
        int orderId = selected.getId();
        String carrierName = selected.getCarrier();
        String customerName = UserSession.getInstance().getUsername();
        
        // Create custom rating dialog with stars
        Dialog<javafx.util.Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Rate Carrier");
        dialog.setHeaderText("Rate Order #" + orderId + " - Carrier: " + carrierName);
        
        // Set button types
        ButtonType submitButton = new ButtonType("Submit Rating", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);
        
        // Create rating UI
        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-padding: 20;");
        
        Label starLabel = new Label("Click to rate:");
        starLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox starBox = new HBox(10);
        starBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        final int[] selectedRating = {0};
        Label[] stars = new Label[5];
        
        for (int i = 0; i < 5; i++) {
            final int starIndex = i;
            stars[i] = new Label("☆");
            stars[i].setStyle("-fx-font-size: 36px; -fx-cursor: hand; -fx-text-fill: #FCD34D;");
            stars[i].setOnMouseEntered(e -> {
                for (int j = 0; j <= starIndex; j++) {
                    stars[j].setText("★");
                }
            });
            stars[i].setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < selectedRating[0] ? "★" : "☆");
                }
            });
            stars[i].setOnMouseClicked(e -> {
                selectedRating[0] = starIndex + 1;
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < selectedRating[0] ? "★" : "☆");
                }
            });
            starBox.getChildren().add(stars[i]);
        }
        
        Label commentLabel = new Label("Your review (optional):");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea commentField = new TextArea();
        commentField.setPromptText("Share your experience...");
        commentField.setPrefRowCount(4);
        commentField.setWrapText(true);
        commentField.setStyle("-fx-font-size: 13px;");
        
        content.getChildren().addAll(starLabel, starBox, commentLabel, commentField);
        dialog.getDialogPane().setContent(content);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return new javafx.util.Pair<>(selectedRating[0], commentField.getText());
            }
            return null;
        });
        
        // Show and process
        java.util.Optional<javafx.util.Pair<Integer, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            int rating = pair.getKey();
            String comment = pair.getValue();
            
            if (rating < 1 || rating > 5) {
                showAlert("Please select at least 1 star.");
                return;
            }
            
            boolean success = OrderDAO.rateOrderWithComment(orderId, carrierName, customerName, rating, comment);
            if (success) {
                showAlert("Rating submitted successfully! Thank you for your feedback.");
            } else {
                showAlert("Failed to submit rating. Please try again.");
            }
        });
    }

    @FXML
    private void handleDownloadInvoice() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select an order to view invoice."); return; }

        int orderId = selected.getId();
        
        // Prefer PDF if available
        byte[] pdf = OrderDAO.getInvoicePdf(orderId);
        if (pdf != null && pdf.length > 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice PDF");
            fileChooser.setInitialFileName("Invoice_Order_" + orderId + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(ordersTable.getScene().getWindow());
            if (file != null) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(pdf);
                    showAlert("Invoice PDF saved to: " + file.getAbsolutePath());
                } catch (IOException e) { e.printStackTrace(); showAlert("Error saving PDF file."); }
            }
            return;
        }

        // Fallback: save invoice text
        String invoiceText = OrderDAO.getInvoiceText(orderId);
        if (invoiceText == null || invoiceText.isEmpty()) {
            showAlert("No invoice found for this order.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Invoice");
        fileChooser.setInitialFileName("Invoice_Order_" + orderId + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(ordersTable.getScene().getWindow());

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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}