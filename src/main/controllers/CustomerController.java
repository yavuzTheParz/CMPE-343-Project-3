package main.controllers;

import main.dao.ProductDAO;
import main.dao.OwnerDAO;
import main.dao.RatingDAO;
import main.dao.MessageDAO;
import main.models.Rating;
import main.models.Message;
import main.models.MessageThread;
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
import javafx.geometry.Pos;
import javafx.scene.control.*; 
import javafx.scene.control.Spinner;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import java.util.List;
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
        try {
            if (UserSession.getInstance() != null) {
                welcomeLabel.setText("👤 " + UserSession.getInstance().getUsername());
            }
            
            setupTableColumns();
            setupFilters();
            addButtonToTable();
            loadProductData();
        } catch (Exception e) {
            System.err.println("Error initializing CustomerController: " + e.getMessage());
            e.printStackTrace();
            // Show error but don't crash
            if (welcomeLabel != null) {
                welcomeLabel.setText("⚠️ Error loading");
            }
        }
    }

    private void setupTableColumns() {
        colImage.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        colImage.setCellFactory(column -> new TableCell<Product, Product>() {
            private final ImageView imageView = new ImageView();
            {
                setAlignment(Pos.CENTER);
            }
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
            matchesCategory = product.getCategoryType().equalsIgnoreCase(selectedCategory);
        }
        return matchesSearch && matchesCategory;
    }

    public void loadProductData() {
        try {
            masterData.clear();
            List<Product> allProducts = ProductDAO.getAllProducts();
            if (allProducts != null) {
                // Do not display products with zero or negative stock
                for (Product p : allProducts) {
                    if (p != null && p.getStock() > 0) {
                        masterData.add(p);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading product data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addButtonToTable() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Add to Cart");
                    {
                        btn.getStyleClass().add("primary-button");
                        btn.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
                        btn.setMaxWidth(Double.MAX_VALUE);
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
                else {
                    // Check existing quantity in cart for this product
                    double existing = 0;
                    for (Product inCart : ShoppingCart.getItems()) {
                        if (inCart.getName().equals(product.getName())) { existing = inCart.getStock(); break; }
                    }
                    if (quantity + existing > product.getStock()) {
                        showAlert("Insufficient Stock", "Only " + product.getStock() + " kg available (you already have " + existing + " kg in cart).");
                    } else {
                        ShoppingCart.addItem(product, quantity);
                        showAlert("Added", quantity + " kg added.");
                    }
                }
            } catch (NumberFormatException e) { showAlert("Invalid Input", "Enter a valid number."); }
        });
    }

    @FXML private void openCart() { openScreen("/cart.fxml", "My Shopping Cart"); }
    @FXML private void openMyOrders() { openScreen("/my_orders.fxml", "My Past Orders"); }
    @FXML private void openProfile() { openScreen("/profile.fxml", "My Profile"); }

    @FXML
    private void openRateCarrierDialog() {
        ObservableList<String> carriers = OwnerDAO.getAllCarriers();
        if (carriers == null || carriers.isEmpty()) { showAlert("No Carriers", "There are no carriers available to rate."); return; }

        // Safe access - list is guaranteed non-empty here
        ChoiceDialog<String> pick = new ChoiceDialog<>(carriers.get(0), carriers);
        pick.setTitle("Rate Carrier");
        pick.setHeaderText("Select carrier to rate");
        pick.setContentText("Carrier:");
        pick.showAndWait().ifPresent(selectedCarrier -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Rate " + selectedCarrier);
            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(10));
            ChoiceBox<String> carrierBox = new ChoiceBox<>(carriers);
            carrierBox.setValue(selectedCarrier);
            Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
            TextArea reviewArea = new TextArea();
            reviewArea.setPromptText("Optional review...");
            grid.add(new Label("Carrier:"), 0, 0); grid.add(carrierBox, 1, 0);
            grid.add(new Label("Rating (1-5):"), 0, 1); grid.add(ratingSpinner, 1, 1);
            grid.add(new Label("Review:"), 0, 2); grid.add(reviewArea, 1, 2);
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    Rating r = new Rating();
                    r.setCarrier(carrierBox.getValue());
                    String customer = (UserSession.getInstance() != null) ? UserSession.getInstance().getUsername() : "anonymous";
                    r.setCustomer(customer);
                    r.setRating(ratingSpinner.getValue());
                    r.setReview(reviewArea.getText());
                    boolean ok = RatingDAO.addOrUpdateRating(r);
                    if (ok) showAlert("Thanks", "Your rating has been saved."); else showAlert("Error", "Could not save rating.");
                }
            });
        });
    }

    @FXML
    private void openMessageOwner() {
        if (UserSession.getInstance() == null) { showAlert("Not logged in", "Please log in to message the owner."); return; }
        String customer = UserSession.getInstance().getUsername();
        String owner = "admin"; // default owner username
        TextInputDialog tid = new TextInputDialog();
        tid.setTitle("Message Owner");
        tid.setHeaderText("Send a message to the owner");
        tid.setContentText("Message:");
        tid.showAndWait().ifPresent(content -> {
            try {
                MessageThread thread = MessageDAO.ensureThread(customer, owner);
                if (thread == null) { showAlert("Error", "Could not create message thread."); return; }
                Message m = new Message();
                m.setThreadId(thread.getId());
                m.setSender(customer);
                m.setContent(content);
                m.setRead(false);
                boolean ok = MessageDAO.addMessage(m);
                if (ok) showAlert("Sent", "Your message was sent to the owner."); else showAlert("Error", "Could not send message.");
            } catch (Exception e) { e.printStackTrace(); showAlert("Error", "Failed to send message."); }
        });
    }

    @FXML
    private void openMyMessages() {
        if (UserSession.getInstance() == null) { showAlert("Not logged in", "Please log in to view your messages."); return; }
        String customer = UserSession.getInstance().getUsername();
        List<MessageThread> threads = MessageDAO.getThreadsForCustomer(customer);
        if (threads == null || threads.isEmpty()) { showAlert("No Messages", "You have no message threads."); return; }

        // There is only one owner; open the first thread directly (list guaranteed non-empty here)
        MessageThread chosen = threads.get(0);
        List<Message> msgs = MessageDAO.getMessagesForThread(chosen.getId());
        
        // Create modern chat dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("💬 Chat with Owner");
        dialog.setHeaderText("Conversation with " + chosen.getOwner());
        
        // Create chat layout
        VBox mainBox = new VBox(15);
        mainBox.setPrefWidth(550);
        mainBox.setPrefHeight(500);
        mainBox.setStyle("-fx-padding: 20; -fx-background-color: #F8F9FA;");
        
        // Chat messages area
        VBox chatMessagesBox = new VBox(10);
        chatMessagesBox.setStyle("-fx-padding: 15; -fx-background-color: #FAFBFC;");
        
        // Populate messages with chat bubbles
        for (Message m : msgs) {
            VBox messageBubble = new VBox(5);
            messageBubble.setStyle("-fx-padding: 12; -fx-background-color: " + 
                (m.getSender().equals(customer) ? "#C5E1A5" : "#E8EAF6") + 
                "; -fx-background-radius: 15; -fx-max-width: 400;");
            
            Label senderLabel = new Label(m.getSender());
            senderLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #455A64;");
            
            Label contentLabel = new Label(m.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #263238;");
            
            messageBubble.getChildren().addAll(senderLabel, contentLabel);
            
            HBox messageRow = new HBox();
            if (m.getSender().equals(customer)) {
                messageRow.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageRow.setAlignment(Pos.CENTER_LEFT);
            }
            messageRow.getChildren().add(messageBubble);
            chatMessagesBox.getChildren().add(messageRow);
        }
        
        ScrollPane scrollPane = new ScrollPane(chatMessagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #FAFBFC; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8;");
        
        // Reply area
        TextArea replyField = new TextArea();
        replyField.setPromptText("Type your message here...");
        replyField.setWrapText(true);
        replyField.setPrefRowCount(3);
        replyField.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12; -fx-font-size: 13px;");
        
        // Custom send button (not in dialog button bar)
        Button sendButton = new Button("📤 Send Message");
        sendButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: 700; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        sendButton.setMaxWidth(Double.MAX_VALUE);
        
        // Handle send button
        sendButton.setOnAction(event -> {
            String messageText = replyField.getText().trim();
            if (!messageText.isEmpty()) {
                Message newMsg = new Message();
                newMsg.setThreadId(chosen.getId());
                newMsg.setSender(customer);
                newMsg.setContent(messageText);
                newMsg.setRead(false);
                boolean ok = MessageDAO.addMessage(newMsg);
                if (ok) {
                    // Add the new message to the chat display
                    VBox messageBubble = new VBox(5);
                    messageBubble.setStyle("-fx-padding: 12; -fx-background-color: #C5E1A5; -fx-background-radius: 15; -fx-max-width: 400;");
                    
                    Label senderLabel = new Label(customer);
                    senderLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #455A64;");
                    
                    Label contentLabel = new Label(messageText);
                    contentLabel.setWrapText(true);
                    contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #263238;");
                    
                    messageBubble.getChildren().addAll(senderLabel, contentLabel);
                    
                    HBox messageRow = new HBox();
                    messageRow.setAlignment(Pos.CENTER_RIGHT);
                    messageRow.getChildren().add(messageBubble);
                    chatMessagesBox.getChildren().add(messageRow);
                    
                    replyField.clear();
                    
                    // Auto-scroll to bottom
                    scrollPane.setVvalue(1.0);
                }
            }
        });
        
        mainBox.getChildren().addAll(scrollPane, replyField, sendButton);
        
        dialog.getDialogPane().setContent(mainBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("Close");
        
        dialog.showAndWait();
    }

    private void openScreen(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            if (fxml.contains("cart")) {
                CartController cc = loader.getController();
                if (cc != null) cc.setParentController(this);
            }
            Stage stage = new Stage();
            stage.setTitle(title);
            // Normalize inline styles so our stylesheet takes effect
            main.controllers.BaseController.normalizeStyles(root);
            Scene scene = new Scene(root);
            if (getClass().getResource("/green-grocer-theme.css") != null)
                scene.getStylesheets().add(getClass().getResource("/green-grocer-theme.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace(); 
            showAlert("Error", "Could not open screen: " + fxml + "\nError: " + e.getMessage()); 
        }
    }

    @FXML
    private void handleLogout() {
        if (UserSession.getInstance() != null) UserSession.cleanUserSession();
        if (welcomeLabel.getScene() != null) welcomeLabel.getScene().getWindow().hide();
        
        // --- DÜZELTİLEN KISIM ---
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            // Normalize styles from FXML and attach stylesheet
            main.controllers.BaseController.normalizeStyles(root);
            Stage stage = new Stage(); // Önce Stage oluştur
            Scene scene = new Scene(root); // Sonra Sahneyi ata
            if (getClass().getResource("/green-grocer-theme.css") != null) scene.getStylesheets().add(getClass().getResource("/green-grocer-theme.css").toExternalForm());
            stage.setTitle("GreenGrocer Login");
            stage.setScene(scene);
            stage.setWidth(960);
            stage.setHeight(540);
            stage.centerOnScreen();
            stage.show(); // En son göster
        } catch(Exception e) { e.printStackTrace(); }
        // ------------------------
    }
}