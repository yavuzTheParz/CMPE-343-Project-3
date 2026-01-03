package main.dao;

import main.DatabaseAdapter;
import main.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;

/**
 * Product data access object. Handles reading products, adding, updating and
 * deleting product records including image BLOB handling.
 */
public class ProductDAO {

    public static ObservableList<Product> getAllProducts() {
        ObservableList<Product> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM products";
        try (Connection conn = DatabaseAdapter.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getDouble("stock_kg"),
                    rs.getDouble("threshold")
                );
                
                InputStream is = rs.getBinaryStream("image_blob");
                if (is != null) {
                    try {
                        p.setImage(new Image(is));
                    } catch (Exception imgEx) {
                        // Image loading failed, use null image (will show placeholder)
                        System.err.println("Failed to load image for product " + p.getName() + ": " + imgEx.getMessage());
                        p.setImage(null);
                    }
                }
                
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static void addProduct(String name, String category, double price, double stock, double threshold, File imageFile) {
        String query = "INSERT INTO products (name, category, price, stock_kg, threshold, image_blob) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.setDouble(4, stock);
            stmt.setDouble(5, threshold);
            
            if (imageFile != null && imageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    stmt.setBinaryStream(6, fis, (int) imageFile.length());
                }
            } else {
                stmt.setNull(6, Types.BLOB);
            }
            
            stmt.executeUpdate();
            LoggerDAO.log("ADD_PRODUCT", "Product added: " + name);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UPDATED METHOD: image update support ---
    // The updateProduct method below supports replacing the image blob when provided.
    /**
     * Update product attributes. If {@code imageFile} is non-null, the image
     * BLOB is replaced; otherwise the image is left unchanged.
     *
     * @param id product id
     * @param name new name
     * @param category new category
     * @param price new price
     * @param stock new stock in kg
     * @param threshold threshold value
     * @param imageFile optional image file (may be null)
     */
    public static void updateProduct(int id, String name, String category, double price, double stock, double threshold, File imageFile) {
    String query;
    if (imageFile != null) {
        query = "UPDATE products SET name = ?, category = ?, price = ?, stock_kg = ?, threshold = ?, image_blob = ? WHERE id = ?";
    } else {
        query = "UPDATE products SET name = ?, category = ?, price = ?, stock_kg = ?, threshold = ? WHERE id = ?";
    }

    try (Connection conn = DatabaseAdapter.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, name);
        stmt.setString(2, category);
        stmt.setDouble(3, price);
        stmt.setDouble(4, stock);
        stmt.setDouble(5, threshold);

        if (imageFile != null && imageFile.exists()) {
            try (FileInputStream fis = new FileInputStream(imageFile)) {
                stmt.setBinaryStream(6, fis, (int) imageFile.length());
                stmt.setInt(7, id);
            }
        } else {
            stmt.setInt(6, id);
        }

        stmt.executeUpdate();
        LoggerDAO.log("UPDATE_PRODUCT", "Updated Product ID: " + id);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    public static void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            LoggerDAO.log("DELETE_PRODUCT", "Deleted Product ID: " + id);
        } catch (SQLException e) { e.printStackTrace(); }
    }
}