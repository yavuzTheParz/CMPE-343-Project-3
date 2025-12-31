package main.dao;

import main.DatabaseAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoggerDAO {

    // Transaction Loglarını CLOB (LONGTEXT) olarak kaydeder
    public static void log(String actionType, String details) {
        String query = "INSERT INTO transaction_logs (log_data) VALUES (?)";
        
        // Log formatı: [TARIH] [TIP] Detaylar
        String logContent = String.format("[%s] Action: %s | Details: %s", 
                                          java.time.LocalDateTime.now(), actionType, details);

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // setString, MySQL'de TEXT/LONGTEXT (CLOB) alanları için yeterlidir
            stmt.setString(1, logContent);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}