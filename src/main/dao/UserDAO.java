package main.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import main.DatabaseAdapter;

/**
 * User data access methods: registration, lookup and profile updates.
 */
public class UserDAO {

    /**
     * Register a new user with the given role. Passwords are stored using
     * SHA-256 via MySQL's SHA2 function in SQL.
     *
     * @param username the desired username
     * @param password plaintext password (will be hashed in DB)
     * @param email email address
     * @param role user role (e.g., customer, carrier, owner)
     * @return true if created successfully
     */
    public static boolean registerUser(String username, String password, String email, String role) {
        String query = "INSERT INTO users (username, password, email, role) VALUES (?, SHA2(?, 256), ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, role);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Username existence check (fixed)
    public static boolean isUsernameTaken(String username) {
        // Previously used SELECT id FROM... which caused issues; now use username
        String query = "SELECT username FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If a record is returned the username is taken.
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // On error, assume taken to be conservative.
        }
    }

    // 3. KULLANICI BİLGİLERİ
    public static Map<String, String> getUserDetails(String username) {
        Map<String, String> details = new HashMap<>();
        // Burada da 'id' kullanmıyoruz, garanti olsun
        String query = "SELECT email, address, phone FROM users WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                details.put("email", rs.getString("email"));
                details.put("address", rs.getString("address"));
                details.put("phone", rs.getString("phone"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return details;
    }

    // 4. PROFİL GÜNCELLEME
    public static boolean updateUserProfile(String username, String email, String address, String phone) {
        String query = "UPDATE users SET email = ?, address = ?, phone = ? WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, address);
            stmt.setString(3, phone);
            stmt.setString(4, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}