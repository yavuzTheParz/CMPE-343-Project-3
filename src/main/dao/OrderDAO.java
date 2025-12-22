package main.dao;

import main.DatabaseAdapter;
import main.models.CartItem;
import main.utils.SessionManager;
import javafx.collections.ObservableList;
import java.sql.*;

public class OrderDAO {

    public static boolean placeOrder(ObservableList<CartItem> items, double totalPrice) {
        String insertOrderSQL = "INSERT INTO orders (username, total_price) VALUES (?, ?)";
        String insertItemSQL = "INSERT INTO order_items (order_id, product_name, quantity, price_per_kg) VALUES (?, ?, ?, ?)";
        String updateStockSQL = "UPDATE products SET stock_kg = stock_kg - ? WHERE id = ?";
        String checkStockSQL = "SELECT stock_kg, name FROM products WHERE id = ?"; // <-- Stok Kontrol Sorgusu

        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;
        PreparedStatement checkStmt = null; // <-- Kontrol için
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false); // TRANSACTION BAŞLAT

            // --- 1. ADIM: GÜVENLİK KONTROLÜ (Stok Yeterli mi?) ---
            checkStmt = conn.prepareStatement(checkStockSQL);
            
            for (CartItem item : items) {
                checkStmt.setInt(1, item.getProduct().getId());
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    double currentStock = rs.getDouble("stock_kg");
                    String productName = rs.getString("name");
                    
                    // Eğer istenen miktar stoktan fazlaysa İPTAL ET
                    if (item.getQuantity() > currentStock) {
                        System.out.println("CRITICAL: Not enough stock for " + productName + 
                                           ". Requested: " + item.getQuantity() + ", Available: " + currentStock);
                        conn.rollback(); // Her şeyi geri al
                        return false;    // Başarısız dön
                    }
                }
                rs.close();
            }
            // -----------------------------------------------------

            // 2. Sipariş Başlığını Kaydet
            String username = SessionManager.getCurrentUsername();
            if (username == null) username = "Guest";

            orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, username);
            orderStmt.setDouble(2, totalPrice);
            orderStmt.executeUpdate();

            generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // 3. Ürünleri Kaydet ve Stoktan Düş
            itemStmt = conn.prepareStatement(insertItemSQL);
            stockStmt = conn.prepareStatement(updateStockSQL);

            for (CartItem item : items) {
                // Kayıt
                itemStmt.setInt(1, orderId);
                itemStmt.setString(2, item.getProduct().getName());
                itemStmt.setDouble(3, item.getQuantity());
                itemStmt.setDouble(4, item.getProduct().getPrice());
                itemStmt.addBatch();

                // Stok Düşme
                stockStmt.setDouble(1, item.getQuantity());
                stockStmt.setInt(2, item.getProduct().getId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();

            conn.commit(); // Her şey yolunda, onayla!
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Hata anında geri al
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // Kaynakları kapat
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) {}
            try { if (checkStmt != null) checkStmt.close(); } catch (SQLException e) {}
            try { if (itemStmt != null) itemStmt.close(); } catch (SQLException e) {}
            try { if (stockStmt != null) stockStmt.close(); } catch (SQLException e) {}
            try { if (orderStmt != null) orderStmt.close(); } catch (SQLException e) {}
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    public static ObservableList<main.models.Order> getPendingOrders() {
        ObservableList<main.models.Order> orders = javafx.collections.FXCollections.observableArrayList();
        String query = "SELECT * FROM orders WHERE status = 'PENDING'";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orders.add(new main.models.Order(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("order_date").toString(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // 2. Sipariş durumunu güncelle (PENDING -> DELIVERED)
    public static void updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}