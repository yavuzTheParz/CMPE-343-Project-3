package main.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.DatabaseAdapter;
import main.models.Rating;

public class RatingDAO {

    public static boolean addOrUpdateRating(Rating r) {
        String sql = "INSERT INTO ratings (carrier, customer, rating, review) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating = VALUES(rating), review = VALUES(review), created_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, r.getCarrier());
            pst.setString(2, r.getCustomer());
            pst.setInt(3, r.getRating());
            pst.setString(4, r.getReview());
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Rating> getRatingsForCarrier(String carrier) {
        List<Rating> list = new ArrayList<>();
        String sql = "SELECT id, carrier, customer, rating, review FROM ratings WHERE carrier = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, carrier);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Rating r = new Rating();
                r.setId(rs.getInt("id"));
                r.setCarrier(rs.getString("carrier"));
                r.setCustomer(rs.getString("customer"));
                r.setRating(rs.getInt("rating"));
                r.setReview(rs.getString("review"));
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static double getAverageRating(String carrier) {
        String sql = "SELECT AVG(rating) as avg_rating FROM ratings WHERE carrier = ?";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, carrier);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("avg_rating");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}
