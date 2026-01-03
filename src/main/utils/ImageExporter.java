package main.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Standalone utility to export product images from database to files for submission.
 * No JavaFX dependencies - can be compiled and run independently.
 */
public class ImageExporter {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "yavuz1234";
    
    public static void main(String[] args) {
        System.out.println("Exporting images from database...");
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            exportAllImages();
            System.out.println("Export complete!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void exportAllImages() {
        String query = "SELECT id, name, image_blob FROM products WHERE image_blob IS NOT NULL";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                InputStream imageStream = rs.getBinaryStream("image_blob");
                
                if (imageStream != null) {
                    // Clean filename - remove special characters
                    String cleanName = name.replaceAll("[^a-zA-Z0-9]", "_");
                    String filename = "images/" + id + "_" + cleanName + ".jpg";
                    
                    try (FileOutputStream fos = new FileOutputStream(filename)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = imageStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Exported: " + filename);
                        count++;
                    } catch (Exception e) {
                        System.err.println("Failed to export " + name + ": " + e.getMessage());
                    }
                }
            }
            
            if (count == 0) {
                System.out.println("\n========================================");
                System.out.println("WARNING: No images found in database!");
                System.out.println("========================================");
                System.out.println("\nYou need to:");
                System.out.println("1. Add products with images through the Owner GUI");
                System.out.println("2. OR manually place image files in the images/ folder");
                System.out.println("\nREQUIRED: At least 12 fruits and 12 vegetables with images");
                System.out.println("Then create GroupImagesXX.zip from the images folder");
            } else {
                System.out.println("\n========================================");
                System.out.println("SUCCESS: Total images exported: " + count);
                System.out.println("========================================");
                System.out.println("\nNext steps:");
                System.out.println("1. Check the images/ folder");
                System.out.println("2. Create GroupImagesXX.zip containing all images");
            }
            
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
