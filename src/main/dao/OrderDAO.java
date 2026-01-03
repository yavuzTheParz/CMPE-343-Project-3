package main.dao;

import main.DatabaseAdapter;
import main.models.Order;
import main.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import java.sql.*;
import java.time.LocalDateTime;
import main.utils.PdfUtil;
import main.models.CarrierOrderModel;

/**
 * Data access methods for orders: placing orders, querying order lists
 * and updating order status. Methods use JDBC connections obtained from
 * {@link main.DatabaseAdapter}.
 */
public class OrderDAO {

    /**
     * Place a simple order (legacy method). Inserts an order and its items,
     * updates product stock, and returns the generated order id.
     *
     * @param username the customer username
     * @param totalPrice the total price value
     * @param cartItems the items to insert as order_items
     * @return created order id, or -1 on failure
     */
    public static int placeOrder(String username, double totalPrice, ObservableList<Product> cartItems) {
        String insertOrder = "INSERT INTO orders (username, total_price, order_date, status) VALUES (?, ?, ?, 'PENDING')";
        String insertItem = "INSERT INTO order_items (order_id, product_name, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_kg = stock_kg - ? WHERE name = ?";

        Connection conn = null;
        try {
            System.out.println("DEBUG: placeOrder start, user=" + username + ", totalPrice=" + totalPrice);
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false); 

            PreparedStatement stmtOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            stmtOrder.setString(1, username);
            stmtOrder.setDouble(2, totalPrice);
            stmtOrder.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            System.out.println("DEBUG: executing order insert");
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

            System.out.println("DEBUG: about to commit orderId=" + orderId);
            conn.commit();
            System.out.println("DEBUG: committed orderId=" + orderId);
            return orderId;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Get invoice text (CLOB) from database
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

    // Generate PDF on-the-fly from invoice text (CLOB) instead of storing as BLOB
    public static byte[] getInvoicePdf(int orderId) {
        String invoiceText = getInvoiceText(orderId);
        if (invoiceText == null || invoiceText.isEmpty()) {
            return null;
        }
        
        try {
            String normalizedText = main.utils.PdfUtil.normalizeTextForPdf(invoiceText);
            return main.utils.PdfUtil.createPdfFromText("Invoice - Order #" + orderId, normalizedText);
        } catch (Exception e) {
            System.err.println("Error generating PDF from invoice text: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- UPDATED METHODS (now return Order objects instead of strings) ---

    // 2. Pending orders pool
    public static ObservableList<Order> getPendingOrders() {
        return getOrdersByQuery("SELECT * FROM orders WHERE status = 'PENDING'");
    }

    //  -- ALL ORDERS (OWNER VIEW) --
    public static ObservableList<Order> getAllOrders() {
        return getOrdersByQuery("SELECT * FROM orders ORDER BY order_date DESC");
    }

    // 3. Carrier deliveries (DELIVERING)
    public static ObservableList<Order> getMyDeliveries(String carrierUsername) {
        return getOrdersByQuery("SELECT * FROM orders WHERE status = 'DELIVERING' AND carrier = '" + carrierUsername + "'");
    }

    // 4. Customer history (helper) - returns String entries (used by MyOrdersController).
    // Note: It would be cleaner to return Order objects, but the string
    // variant is retained for backwards compatibility with the UI.
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

    // --- NEW HELPER (returns Order objects) ---
    private static ObservableList<Order> getOrdersByQuery(String query) {
        ObservableList<Order> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int rating = rs.getInt("carrier_rating");
                String review = rs.getString("carrier_review");
                list.add(new Order(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("total_price"),
                    rs.getTimestamp("order_date"),
                    rs.getString("status"),
                    rs.getString("carrier"),
                    rating,
                    review
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 5. Update order status
    public static void updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 6. Assign to carrier
    public static void assignCarrier(int orderId, String carrierUsername) {
        String query = "UPDATE orders SET status = 'DELIVERING', carrier = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, carrierUsername);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Statistics (unchanged)
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

    // 9. Cancel order (only when status is PENDING)
    public static boolean cancelOrder(int orderId) {
        // Only cancellable when courier has not been dispatched (PENDING)
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

    // 10. Rate carrier
    public static void rateOrder(int orderId, int rating) {
        String query = "UPDATE orders SET carrier_rating = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // Rate carrier with comment - prevents duplicates by updating existing rating
    public static boolean rateOrderWithComment(int orderId, String carrierName, String customerName, int rating, String comment) {
        String query = "UPDATE orders SET carrier_rating = ?, carrier_review = ?, reviewed_by = ?, review_date = NOW() WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment);
            stmt.setString(3, customerName);
            stmt.setInt(4, orderId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false;
        }
    }
    
    // Get ratings for a specific carrier (for owner dashboard)
    public static ObservableList<String> getCarrierRatings(String carrierUsername) {
        ObservableList<String> ratings = FXCollections.observableArrayList();
        String query = "SELECT id, reviewed_by, carrier_rating, carrier_review, review_date FROM orders WHERE carrier = ? AND carrier_rating IS NOT NULL ORDER BY review_date DESC";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, carrierUsername);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int orderId = rs.getInt("id");
                String customer = rs.getString("reviewed_by");
                int rating = rs.getInt("carrier_rating");
                String review = rs.getString("carrier_review");
                Timestamp reviewDate = rs.getTimestamp("review_date");
                
                String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
                String reviewText = (review != null && !review.isEmpty()) ? review : "No comment";
                String dateStr = reviewDate != null ? reviewDate.toString().substring(0, 16) : "N/A";
                
                ratings.add(String.format("Order #%d | %s | %s | %s | %s", 
                    orderId, customer, stars, reviewText, dateStr));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ratings;
    }

    // 11. Place new order with details (delivery datetime and discount)
    // (Eski placeOrder metodunu bununla GÜNCELLEYİN veya Overload edin)
    public static int placeOrderWithDetails(String username, double totalPrice, double discount, ObservableList<Product> items, LocalDateTime deliveryTime, String invoiceText, String couponCode) {
        String insertOrder = "INSERT INTO orders (username, total_price, discount_applied, order_date, requested_delivery_time, invoice_text, status) VALUES (?, ?, ?, NOW(), ?, ?, 'PENDING')";
        String insertItem = "INSERT INTO order_items (order_id, product_name, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_kg = stock_kg - ? WHERE name = ?";

        Connection conn = null;
        try {
            System.out.println("DEBUG: placeOrderWithDetails start, user=" + username + ", totalPrice=" + totalPrice + ", discount=" + discount + ", couponCode=" + couponCode);
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false); 

            // Use the professionally formatted invoice text passed from CartController
            String finalInvoiceText = (invoiceText != null && !invoiceText.trim().isEmpty()) ? invoiceText : "Invoice for " + username;

            PreparedStatement stmtOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            stmtOrder.setString(1, username);
            stmtOrder.setDouble(2, totalPrice); // İndirim düşülmüş fiyat
            stmtOrder.setDouble(3, discount);
            // SQL uses NOW() for order_date, so requested_delivery_time is parameter 4
            stmtOrder.setTimestamp(4, Timestamp.valueOf(deliveryTime));
            stmtOrder.setString(5, finalInvoiceText);
            System.out.println("DEBUG: executing order insert");

            int orderId = 0;
            stmtOrder.executeUpdate();
            try (ResultSet rs = stmtOrder.getGeneratedKeys()) {
                if (rs.next()) orderId = rs.getInt(1);
            }

            // Verify stock availability (select ... FOR UPDATE to lock rows)
            for (Product p : items) {
                String q = "SELECT stock_kg FROM products WHERE name = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(q)) {
                    ps.setString(1, p.getName());
                    try (ResultSet r = ps.executeQuery()) {
                        if (r.next()) {
                            double avail = r.getDouble("stock_kg");
                            if (avail < p.getStock()) {
                                // Not enough stock - abort
                                System.out.println("ERROR: insufficient stock for " + p.getName() + ": available=" + avail + ", needed=" + p.getStock());
                                conn.rollback();
                                return -1;
                            }
                        } else {
                            System.out.println("ERROR: product not found: " + p.getName());
                            conn.rollback();
                            return -1;
                        }
                    }
                }
            }

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

            // If a coupon code was provided, attempt to record redemption and increment used_count within this transaction
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                try {
                    String sel = "SELECT id, active, expires_at, usage_limit, used_count FROM coupons WHERE code = ? FOR UPDATE";
                    try (PreparedStatement ps = conn.prepareStatement(sel)) {
                        ps.setString(1, couponCode);
                        try (ResultSet r = ps.executeQuery()) {
                            if (r.next()) {
                                int couponId = r.getInt("id");
                                boolean active = r.getBoolean("active");
                                Timestamp expires = r.getTimestamp("expires_at");
                                int usageLimit = r.getInt("usage_limit");
                                int usedCount = r.getInt("used_count");
                                Timestamp now = new Timestamp(System.currentTimeMillis());
                                if (active && (expires == null || expires.after(now)) && (usageLimit == 0 || usedCount < usageLimit)) {
                                    String ins = "INSERT INTO coupon_redemptions (coupon_id, customer) VALUES (?,?)";
                                    try (PreparedStatement psi = conn.prepareStatement(ins)) {
                                        psi.setInt(1, couponId);
                                        psi.setString(2, username);
                                        psi.executeUpdate();
                                    }
                                    String upd = "UPDATE coupons SET used_count = used_count + 1 WHERE id = ?";
                                    try (PreparedStatement psu = conn.prepareStatement(upd)) {
                                        psu.setInt(1, couponId);
                                        psu.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException ex) {
                    // If coupon redemption fails, log but do not abort the order
                    ex.printStackTrace();
                }
            }

            conn.commit(); 
            System.out.println("DEBUG: committed orderId=" + orderId);
            return orderId;

        } catch (Exception e) {
            System.out.println("ERROR in placeOrderWithDetails: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    public static ObservableList<CarrierOrderModel> getOrdersForCarrier(String status, String carrierName) {
        ObservableList<CarrierOrderModel> list = FXCollections.observableArrayList();
        
        // For completed orders, show actual_delivery_time; otherwise show requested_delivery_time
        String dateColumn = status.equals("COMPLETED") ? "o.actual_delivery_time" : "o.requested_delivery_time";
        
        String query = "SELECT o.id, o.username, u.address, o.total_price, " + dateColumn + " as delivery_time, " +
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
                    rs.getString("products"), // product list as a comma-separated string
                    rs.getDouble("total_price"),
                    rs.getString("delivery_time")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 13. SİPARİŞİ ÜZERİNE AL (PENDING -> IN_DELIVERY) with race condition protection
    public static boolean assignOrderToCarrier(int orderId, String carrierName) {
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false);
            
            // Lock the order row for update to prevent race conditions
            String lockQuery = "SELECT id, status, carrier FROM orders WHERE id = ? FOR UPDATE";
            PreparedStatement lockStmt = conn.prepareStatement(lockQuery);
            lockStmt.setInt(1, orderId);
            ResultSet rs = lockStmt.executeQuery();
            
            if (rs.next()) {
                String currentStatus = rs.getString("status");
                String currentCarrier = rs.getString("carrier");
                
                // Check if order is still available (PENDING and no carrier assigned)
                if (!"PENDING".equals(currentStatus)) {
                    conn.rollback();
                    return false; // Order not pending anymore
                }
                
                if (currentCarrier != null && !currentCarrier.isEmpty()) {
                    conn.rollback();
                    return false; // Already assigned to another carrier
                }
                
                // Order is available, assign it
                String updateQuery = "UPDATE orders SET carrier = ?, status = 'IN_DELIVERY' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, carrierName);
                updateStmt.setInt(2, orderId);
                updateStmt.executeUpdate();
                
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false; // Order not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // 14. Complete order (IN_DELIVERY -> COMPLETED)
    public static boolean completeOrderDelivery(int orderId, LocalDateTime actualDeliveryTime) {
        String query = "UPDATE orders SET status = 'COMPLETED', actual_delivery_time = ? WHERE id = ? AND status = 'IN_DELIVERY'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(actualDeliveryTime));
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // Get orders as Order objects for customer
    public static ObservableList<Order> getOrderObjectsByUser(String username) {
        String query = "SELECT * FROM orders WHERE username = ? ORDER BY order_date DESC";
        return getOrdersByQuery(query.replace("?", "'" + username + "'"));
    }
    
    // Get completion time for an order
    public static Timestamp getCompletionTime(int orderId) {
        String query = "SELECT completed_at FROM orders WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("completed_at");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}