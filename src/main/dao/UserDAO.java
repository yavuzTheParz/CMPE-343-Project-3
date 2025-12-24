package main.dao;

import main.DatabaseAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Kullanıcıyı doğrula ve ROLÜNÜ döndür (Hatalıysa null döner)
    public static main.models.UserRole validateUser(String username, String password) {
    String query = "SELECT role FROM users WHERE username = ? AND password = SHA2(?, 256)";
    
    try (java.sql.Connection conn = main.DatabaseAdapter.getConnection();
         java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, username);
        stmt.setString(2, password);
        
        try (java.sql.ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                // Veritabanından gelen String'i Enum'a çeviriyoruz
                return main.models.UserRole.fromString(rs.getString("role"));
            }
        }
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
    }
    return null; 
    }
    public static boolean registerUser(String username, String password, String email, main.models.UserRole role) {
        String query = "INSERT INTO users (username, password, email, role) VALUES (?, SHA2(?, 256), ?, ?)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password); // Veritabanı bunu şifreleyecek
            stmt.setString(3, email);
            stmt.setString(4, role.toString().toLowerCase()); // Enum -> String ("customer")
            
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. KULLANICI ADI MÜSAİT Mİ?
    public static boolean isUsernameTaken(String username) {
        String query = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Eğer kayıt varsa 'true' döner (yani isim alınmış)
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Hata varsa risk alma, alınmış de
        }
    }
}