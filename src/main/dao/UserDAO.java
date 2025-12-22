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
}