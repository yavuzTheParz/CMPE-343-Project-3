package main.dao;

import main.DatabaseAdapter;
import main.models.Product;
import main.models.Fruit;
import main.models.Vegetable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class ProductDAO {

    // Tüm ürünleri (Meyve/Sebze) getirir
    public static ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String query = "SELECT * FROM products";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price_per_kg");
                double stock = rs.getDouble("stock_kg");
                String category = rs.getString("category");

                Product product;
                // Polymorphism: Kategoriye göre nesne oluştur
                if ("Fruit".equalsIgnoreCase(category)) {
                    product = new Fruit(id, name, price, stock);
                } else {
                    product = new Vegetable(id, name, price, stock);
                }
                
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // 1. ADD PRODUCT
    public static void addProduct(String name, String category, double price, double stock) {
        String query = "INSERT INTO products (name, category, price_per_kg, stock_kg) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, name);
            stmt.setString(2, category); // 'Fruit' or 'Vegetable'
            stmt.setDouble(3, price);
            stmt.setDouble(4, stock);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. UPDATE PRODUCT
    public static void updateProduct(int id, double price, double stock) {
        String query = "UPDATE products SET price_per_kg = ?, stock_kg = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, price);
            stmt.setDouble(2, stock);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. DELETE PRODUCT
    public static void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Stok güncelleme metodu (BURADA OLMALI)
    public static void updateStock(int productId, double quantitySold) {
        String query = "UPDATE products SET stock_kg = stock_kg - ? WHERE id = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, quantitySold);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}