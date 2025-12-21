package main.dao;

import main.utils.PasswordUtil;  // Correct import for PasswordUtil
import main.DatabaseAdapter;     // Correct import for DatabaseAdapter

import java.sql.*;

public class UserDAO {

    // Method to add a new user to the database with hashed password
    public static void addUser(String username, String password, String email) {
        // Hash the password using SHA-256
        String hashedPassword = PasswordUtil.hashPassword(password);

        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseAdapter.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);  // Store hashed password in the database
            stmt.setString(3, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to check if the user exists by comparing the password hash
    public static boolean userExists(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection connection = DatabaseAdapter.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");  // Retrieve the stored hashed password
                // Validate password by comparing the hash
                if (PasswordUtil.checkPassword(password, storedHash)) {
                    return true;  // Password matches
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // Password does not match
    }
}
