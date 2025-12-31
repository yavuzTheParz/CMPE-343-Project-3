package main;

import java.sql.*;

public class DatabaseAdapter {

    // Veritabanı Bilgileri
    private static final String DB_URL = "jdbc:mysql://localhost:3306/project3_db";
    private static final String USER = "myuser"; 
    private static final String PASS = "1234"; // Şifrenizi buraya yazın

    // 1. BAĞLANTIYI TEST ET
    public static void connect() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("✅ Database connection successful!");
        }
    }

    // 2. BAĞLANTI NESNESİ GETİR
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // 3. TABLOLARI OLUŞTUR
    public static void createTables() {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "email VARCHAR(100), " +
            "role VARCHAR(20) NOT NULL)",

            "CREATE TABLE IF NOT EXISTS products (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "category VARCHAR(50) NOT NULL, " +
            "price_per_kg DOUBLE NOT NULL, " +
            "stock_kg DOUBLE NOT NULL, " +
            "threshold DOUBLE DEFAULT 0)",

            "CREATE TABLE IF NOT EXISTS orders (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50), " +
            "total_price DOUBLE, " +
            "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "status VARCHAR(20) DEFAULT 'PENDING', " +
            "carrier VARCHAR(50) DEFAULT NULL)",

            "CREATE TABLE IF NOT EXISTS order_items (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "order_id INT, " +
            "product_name VARCHAR(100), " +
            "quantity DOUBLE, " +
            "price DOUBLE, " +
            "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE)"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String sql : tables) {
                stmt.execute(sql);
            }
            // Admin ekle
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, email, role) VALUES ('admin', SHA2('admin123', 256), 'admin@market.com', 'owner')");
            // Kurye ekle (Test için)
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, email, role) VALUES ('carr', SHA2('carr', 256), 'carrier@market.com', 'carrier')");
            // Müşteri ekle (Test için)
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, email, role) VALUES ('cust', SHA2('cust', 256), 'cust@market.com', 'customer')");

            System.out.println("✅ Tables checked/created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- EKSİK OLAN METOD BURASIYDI ---
    public static String validateLogin(String username, String password) {
        String query = "SELECT role FROM users WHERE username = ? AND password = SHA2(?, 256)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Kullanıcı bulunamadı
    }
}