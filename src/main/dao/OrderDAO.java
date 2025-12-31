package main.dao;

import main.DatabaseAdapter;
import main.models.Order;
import main.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import java.sql.*;
import java.time.LocalDateTime;
import main.models.CarrierOrderModel;
public class OrderDAO {

    // 1. SİPARİŞ OLUŞTUR
    public static int placeOrder(String username, double totalPrice, ObservableList<Product> cartItems) {
        String insertOrder = "INSERT INTO orders (username, total_price, order_date, status) VALUES (?, ?, ?, 'PENDING')";
        String insertItem = "INSERT INTO order_items (order_id, product_name, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_kg = stock_kg - ? WHERE name = ?";

        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false); 

            PreparedStatement stmtOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            stmtOrder.setString(1, username);
            stmtOrder.setDouble(2, totalPrice);
            stmtOrder.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmtOrder.executeUpdate();

            ResultSet rs = stmtOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            PreparedStatement stmtItem = conn.prepareStatement(insertItem);
            PreparedStatement stmtStock = conn.prepareStatement(updateStock);

            for (Product p : cartItems) {
                stmtItem.setInt(1, orderId);
                stmtItem.setString(2, p.getName());
                stmtItem.setDouble(3, p.getStock()); 
                stmtItem.setDouble(4, p.getEffectivePrice());
                stmtItem.addBatch();

                stmtStock.setDouble(1, p.getStock()); 
                stmtStock.setString(2, p.getName());
                stmtStock.addBatch();
            }

            stmtItem.executeBatch();
            stmtStock.executeBatch();
            conn.commit(); 
            return orderId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- GÜNCELLENEN METODLAR (Artık String DEĞİL, Order döndürüyorlar) ---

    // 2. HAVUZDAKİ SİPARİŞLER (PENDING)
    public static ObservableList<Order> getPendingOrders() {
        return getOrdersByQuery("SELECT * FROM orders WHERE status = 'PENDING'");
    }

    // 3. KURYENİN SİPARİŞLERİ (DELIVERING)
    public static ObservableList<Order> getMyDeliveries(String carrierUsername) {
        return getOrdersByQuery("SELECT * FROM orders WHERE status = 'DELIVERING' AND carrier = '" + carrierUsername + "'");
    }

    // 4. MÜŞTERİ GEÇMİŞİ (YARDIMCI METOD) - String Döner (MyOrdersController için)
    // Not: MyOrdersController hala String kullanıyorsa burası karışabilir, 
    // ama temizlik adına MyOrdersController'ı Order'a çevirmek daha iyi olurdu. 
    // Şimdilik hatayı çözmek için String versiyonunu AYRI tutalım.
    public static ObservableList<String> getOrdersByUser(String username) {
         ObservableList<String> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM orders WHERE username = '" + username + "' ORDER BY order_date DESC";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add("ID: " + rs.getInt("id") + " | Total: " + rs.getDouble("total_price") + " TL | Date: " + rs.getTimestamp("order_date"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- YENİ EKLENEN HELPER (ORDER NESNESİ DÖNDÜRÜR) ---
    private static ObservableList<Order> getOrdersByQuery(String query) {
        ObservableList<Order> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("total_price"),
                    rs.getTimestamp("order_date"),
                    rs.getString("status"),
                    rs.getString("carrier")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 5. STATUS GÜNCELLEME (Hata veren eksik metod buydu)
    public static void updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 6. KURYEYE ATA
    public static void assignCarrier(int orderId, String carrierUsername) {
        String query = "UPDATE orders SET status = 'DELIVERING', carrier = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, carrierUsername);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // İstatistik (Aynı Kalıyor)
    public static ObservableList<PieChart.Data> getProductSalesStats() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        String query = "SELECT product_name, SUM(quantity) as total_sold FROM order_items GROUP BY product_name";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pieData.add(new PieChart.Data(rs.getString("product_name"), rs.getDouble("total_sold")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return pieData;
    }
    public static int getCompletedOrderCount(String username) {
        String query = "SELECT COUNT(*) FROM orders WHERE username = ? AND status = 'COMPLETED'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 9. SİPARİŞ İPTALİ (Sadece PENDING ise)
    public static boolean cancelOrder(int orderId) {
        // Sadece kurye yola çıkmadıysa (PENDING) iptal edilebilir
        String query = "UPDATE orders SET status = 'CANCELLED' WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false;
        }
    }

    // 10. KURYE PUANLAMA
    public static void rateOrder(int orderId, int rating) {
        String query = "UPDATE orders SET carrier_rating = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 11. YENİ SİPARİŞ OLUŞTURMA (Tarih ve İndirimli Versiyon)
    // (Eski placeOrder metodunu bununla GÜNCELLEYİN veya Overload edin)
    public static int placeOrderWithDetails(String username, double totalPrice, double discount, ObservableList<Product> items, LocalDateTime deliveryTime, String invoiceText) {
        String insertOrder = "INSERT INTO orders (username, total_price, discount_applied, order_date, requested_delivery_time, invoice_text, status) VALUES (?, ?, ?, NOW(), ?, ?, 'PENDING')";
        String insertItem = "INSERT INTO order_items (order_id, product_name, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_kg = stock_kg - ? WHERE name = ?";

        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false); 

            PreparedStatement stmtOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            stmtOrder.setString(1, username);
            stmtOrder.setDouble(2, totalPrice); // İndirim düşülmüş fiyat
            stmtOrder.setDouble(3, discount);
            stmtOrder.setTimestamp(4, Timestamp.valueOf(deliveryTime));
            stmtOrder.setString(5, invoiceText);
            stmtOrder.executeUpdate();

            ResultSet rs = stmtOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            PreparedStatement stmtItem = conn.prepareStatement(insertItem);
            PreparedStatement stmtStock = conn.prepareStatement(updateStock);

            for (Product p : items) {
                stmtItem.setInt(1, orderId);
                stmtItem.setString(2, p.getName());
                stmtItem.setDouble(3, p.getStock());
                stmtItem.setDouble(4, p.getEffectivePrice());
                stmtItem.addBatch();

                stmtStock.setDouble(1, p.getStock()); 
                stmtStock.setString(2, p.getName());
                stmtStock.addBatch();
            }

            stmtItem.executeBatch();
            stmtStock.executeBatch();
            conn.commit(); 
            return orderId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    public static ObservableList<CarrierOrderModel> getOrdersForCarrier(String status, String carrierName) {
        ObservableList<CarrierOrderModel> list = FXCollections.observableArrayList();
        
        String query = "SELECT o.id, o.username, u.address, o.total_price, o.requested_delivery_time, " +
                       "GROUP_CONCAT(oi.product_name SEPARATOR ', ') as products " +
                       "FROM orders o " +
                       "JOIN users u ON o.username = u.username " +
                       "JOIN order_items oi ON o.id = oi.order_id " +
                       "WHERE o.status = ? ";
        
        if (carrierName != null) {
            query += "AND o.carrier = ? ";
        } else {
            query += "AND o.carrier IS NULL "; // Müsait olanlar sahipsizdir
        }
        
        query += "GROUP BY o.id";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            if (carrierName != null) {
                stmt.setString(2, carrierName);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new CarrierOrderModel(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("address"),
                    rs.getString("products"), // Ürün listesi string olarak
                    rs.getDouble("total_price"),
                    rs.getString("requested_delivery_time")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 13. SİPARİŞİ ÜZERİNE AL (PENDING -> IN_DELIVERY)
    public static boolean assignOrderToCarrier(int orderId, String carrierName) {
        String query = "UPDATE orders SET carrier = ?, status = 'IN_DELIVERY' WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, carrierName);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 14. SİPARİŞİ TAMAMLA (IN_DELIVERY -> COMPLETED)
    public static boolean completeOrderDelivery(int orderId) {
        String query = "UPDATE orders SET status = 'COMPLETED', actual_delivery_time = NOW() WHERE id = ? AND status = 'IN_DELIVERY'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public static String getInvoiceText(int orderId) {
        String query = "SELECT invoice_text FROM orders WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("invoice_text");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}