package main.dao;

import main.DatabaseAdapter;
import main.controllers.CarrierController; // CarrierOrderModel için (veya main.models varsa oradan)
import main.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class OwnerDAO {

    // --- 1. KURYE YÖNETİMİ ---
    public static ObservableList<String> getAllCarriers() {
        ObservableList<String> carriers = FXCollections.observableArrayList();
        String query = "SELECT username FROM users WHERE role = 'carrier'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) carriers.add(rs.getString("username"));
        } catch (SQLException e) { e.printStackTrace(); }
        return carriers;
    }

    public static boolean fireCarrier(String username) {
        String query = "DELETE FROM users WHERE username = ? AND role = 'carrier'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean hireCarrier(String username, String password) {
        // UserDAO'daki register metodunu da kullanabilirdik ama burada özel
        return main.dao.UserDAO.registerUser(username, password, username + "@carrier.com", "carrier");
    }

    // --- 2. RAPORLAR (GRAFİK VERİLERİ) ---
    public static XYChart.Series<String, Number> getProductSalesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Product Sales (kg)");
        String query = "SELECT product_name, SUM(quantity) as total_qty FROM order_items GROUP BY product_name";
        try (Connection conn = DatabaseAdapter.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("product_name"), rs.getDouble("total_qty")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return series;
    }

    // --- 3. MESAJLAR ---
    public static ObservableList<Map<String, String>> getMessages() {
        ObservableList<Map<String, String>> msgs = FXCollections.observableArrayList();
        String query = "SELECT * FROM messages ORDER BY sent_at DESC";
        try (Connection conn = DatabaseAdapter.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(rs.getInt("id")));
                m.put("sender", rs.getString("sender_username"));
                m.put("content", rs.getString("content"));
                m.put("reply", rs.getString("reply"));
                msgs.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return msgs;
    }

    public static void replyMessage(int id, String reply) {
        String query = "UPDATE messages SET reply = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, reply);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // --- 4. AYARLAR ---
    public static void updateSetting(String key, String value) {
        String query = "REPLACE INTO system_settings (setting_key, setting_value) VALUES (?, ?)";
         try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static String getSetting(String key) {
        String query = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}