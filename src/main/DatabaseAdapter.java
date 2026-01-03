package main;

import java.sql.*;

/**
 * DatabaseAdapter provides a centralized JDBC connection helper and schema
 * creation/migration utilities used at application startup.
 *
 * It exposes: connect(), getConnection(), and createTables() to ensure the
 * required schema exists and performs safe migrations when necessary.
 */
public class DatabaseAdapter {

    /** JDBC URL for the application's database. */
    private static final String DB_URL = "jdbc:mysql://localhost:3306/project3_db";
    /** DB username. */
    private static final String USER = "myuser";
    /** DB password. Update for your environment. */
    private static final String PASS = "1234";

    /**
     * Test the database connection using configured credentials.
     *
     * @throws SQLException if the connection attempt fails
     */
    public static void connect() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("✅ Database connection successful!");
        }
    }

    /**
     * Return a new JDBC {@link Connection} using configured credentials.
     *
     * @return a new {@link Connection}
     * @throws SQLException if creating the connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Ensure the required database tables and columns exist. This method will
     * create missing tables and perform idempotent migrations for older
     * schemas (for example adding new columns to the `orders` table).
     */
    public static void createTables() {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "email VARCHAR(100), " +
            "address TEXT, " +
            "phone VARCHAR(30), " +
            "role VARCHAR(20) NOT NULL)",

            "CREATE TABLE IF NOT EXISTS products (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "category VARCHAR(50) NOT NULL, " +
            "price DOUBLE NOT NULL, " +
            "stock_kg DOUBLE NOT NULL, " +
            "threshold DOUBLE DEFAULT 0, " +
            "image_blob LONGBLOB NULL)",

            "CREATE TABLE IF NOT EXISTS orders (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50), " +
            "total_price DOUBLE, " +
            "discount_applied DOUBLE DEFAULT 0, " +
            "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "requested_delivery_time DATETIME NULL, " +
            "invoice_text LONGTEXT, " +
            "invoice_pdf LONGBLOB NULL, " +
            "status VARCHAR(20) DEFAULT 'PENDING', " +
            "carrier VARCHAR(50) DEFAULT NULL, " +
            "actual_delivery_time TIMESTAMP NULL, " +
            "carrier_rating INT NULL)",

            "CREATE TABLE IF NOT EXISTS order_items (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "order_id INT, " +
            "product_name VARCHAR(100), " +
            "quantity DOUBLE, " +
            "price DOUBLE, " +
            "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS system_settings (" +
            "setting_key VARCHAR(100) PRIMARY KEY, " +
            "setting_value VARCHAR(255))",

            "CREATE TABLE IF NOT EXISTS transaction_logs (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "log_data LONGTEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : tables) stmt.execute(sql);

            // Ratings table: one rating per (carrier, customer)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ratings (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "carrier VARCHAR(50) NOT NULL, " +
                    "customer VARCHAR(50) NOT NULL, " +
                    "rating INT NOT NULL, " +
                    "review TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (carrier, customer))");

            // Threads for one-to-one customer<->owner conversations
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS threads (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "customer VARCHAR(50) NOT NULL, " +
                    "owner VARCHAR(50) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (customer, owner))");

            // Messages belonging to threads
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "thread_id INT NULL, " +
                    "sender VARCHAR(50) NULL, " +
                    "sender_username VARCHAR(50) NULL, " +
                    "content LONGTEXT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "is_read BOOLEAN DEFAULT FALSE, " +
                    "FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE)");

            // Migrate legacy messages into threads when possible
            try {
                stmt.executeUpdate("UPDATE messages m JOIN threads t ON m.sender_username = t.customer SET m.thread_id = t.id WHERE m.thread_id IS NULL AND m.sender_username IS NOT NULL");
            } catch (SQLException ignored) {}

            // Ensure orders table has newer columns if it was created by an older schema
            try {
                String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = ?";
                String[][] cols = new String[][]{
                    {"invoice_text", "ALTER TABLE orders ADD COLUMN invoice_text LONGTEXT"},
                    {"invoice_pdf", "ALTER TABLE orders ADD COLUMN invoice_pdf LONGBLOB NULL"},
                    {"discount_applied", "ALTER TABLE orders ADD COLUMN discount_applied DOUBLE DEFAULT 0"},
                    {"requested_delivery_time", "ALTER TABLE orders ADD COLUMN requested_delivery_time DATETIME NULL"},
                    {"actual_delivery_time", "ALTER TABLE orders ADD COLUMN actual_delivery_time TIMESTAMP NULL"},
                    {"carrier", "ALTER TABLE orders ADD COLUMN carrier VARCHAR(50) DEFAULT NULL"},
                    {"carrier_rating", "ALTER TABLE orders ADD COLUMN carrier_rating INT NULL"}
                };

                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    for (String[] col : cols) {
                        ps.setString(1, col[0]);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                try {
                                    stmt.executeUpdate(col[1]);
                                    System.out.println("INFO: added missing column to orders: " + col[0]);
                                } catch (SQLException e) {
                                    System.out.println("WARN: failed to add column " + col[0] + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (SQLException ignored) {}

            // Admin and sample users
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, email, role) VALUES ('admin', SHA2('admin123', 256), 'admin@market.com', 'owner')");
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, email, role) VALUES ('carr', SHA2('carr', 256), 'carrier@market.com', 'carrier')");
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, email, role) VALUES ('cust', SHA2('cust', 256), 'cust@market.com', 'customer')");

            System.out.println("✅ Tables checked/created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Previously missing method section ---
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
        return null; // user not found
    }
}