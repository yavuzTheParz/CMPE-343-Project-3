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
                    p.setImage(new Image(is));
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
            
            if (imageFile != null) {
                FileInputStream fis = new FileInputStream(imageFile);
                stmt.setBinaryStream(6, fis, (int) imageFile.length());
            } else {
                stmt.setNull(6, Types.BLOB);
            }
            
            stmt.executeUpdate();
            LoggerDAO.log("ADD_PRODUCT", "Product added: " + name);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- GÜNCELLENEN METOD: RESİM GÜNCELLEME DESTEĞİ ---
    public static void updateProduct(int id, double price, double stock, double threshold, File imageFile) {
        // Eğer yeni resim seçildiyse onu da güncelle, seçilmediyse sadece diğerlerini güncelle
        String query;
        if (imageFile != null) {
            query = "UPDATE products SET price = ?, stock_kg = ?, threshold = ?, image_blob = ? WHERE id = ?";
        } else {
            query = "UPDATE products SET price = ?, stock_kg = ?, threshold = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, price);
            stmt.setDouble(2, stock);
            stmt.setDouble(3, threshold);
            
            if (imageFile != null) {
                FileInputStream fis = new FileInputStream(imageFile);
                stmt.setBinaryStream(4, fis, (int) imageFile.length());
                stmt.setInt(5, id);
            } else {
                stmt.setInt(4, id);
            }
            
            stmt.executeUpdate();
            LoggerDAO.log("UPDATE_PRODUCT", "Updated Product ID: " + id);
        } catch (Exception e) { e.printStackTrace(); }
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