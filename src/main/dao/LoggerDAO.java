package main.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.DatabaseAdapter;

public class LoggerDAO {

    /**
     * Persist transaction logs into the {@code transaction_logs} table.
     * The log content is stored as LONGTEXT (CLOB) in the database.
     */
    public static void log(String actionType, String details) {
        String query = "INSERT INTO transaction_logs (log_data) VALUES (?)";
        
        // Log format: [TIMESTAMP] Action: <type> | Details: <details>
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