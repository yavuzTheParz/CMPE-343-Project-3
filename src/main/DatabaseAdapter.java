package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAdapter {

    private static final String URL = "jdbc:mysql://localhost:3306/project3_db";
    private static final String USER = "myuser";
    private static final String PASSWORD = "1234";

    public static Connection getConnection() throws SQLException {
        try {
            // MySQL JDBC driver'ını yükleyin
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Veritabanı bağlantısını yapın
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
    }
}
