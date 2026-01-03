package main.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.DatabaseAdapter;
import main.models.Coupon;

public class CouponDAO {

    public static boolean createCoupon(Coupon c) {
        String sql = "INSERT INTO coupons (code, discount_percent, fixed_amount, active, expires_at, usage_limit, used_count, min_cart_value) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode());
            ps.setDouble(2, c.getDiscountPercent());
            ps.setDouble(3, c.getFixedAmount());
            ps.setBoolean(4, c.isActive());
            if (c.getExpiresAt() != null) ps.setTimestamp(5, c.getExpiresAt()); else ps.setNull(5, Types.TIMESTAMP);
            ps.setInt(6, c.getUsageLimit());
            ps.setInt(7, c.getUsedCount());
            ps.setDouble(8, c.getMinCartValue());
            int rows = ps.executeUpdate();
            if (rows == 0) return false;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Coupon getCouponByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ?";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Coupon> getAllCoupons() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Coupon validateCouponForCustomer(String code, String customerUsername, double cartTotal) {
        Coupon c = getCouponByCode(code);
        if (c == null) return null;
        if (!c.isActive()) return null;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (c.getExpiresAt() != null && c.getExpiresAt().before(now)) return null;
        if (c.getUsageLimit() > 0 && c.getUsedCount() >= c.getUsageLimit()) return null;
        if (cartTotal < c.getMinCartValue()) return null;
        // check redemption
        String sql = "SELECT 1 FROM coupon_redemptions WHERE coupon_id = ? AND customer = ?";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setString(2, customerUsername);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return null; // already redeemed by this customer
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return c;
    }

    public static boolean redeemCoupon(int couponId, String customerUsername) {
        String insert = "INSERT INTO coupon_redemptions (coupon_id, customer) VALUES (?,?)";
        String update = "UPDATE coupons SET used_count = used_count + 1 WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psIns = conn.prepareStatement(insert); PreparedStatement psUpd = conn.prepareStatement(update)) {
                psIns.setInt(1, couponId);
                psIns.setString(2, customerUsername);
                psIns.executeUpdate();

                psUpd.setInt(1, couponId);
                psUpd.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                // possible duplicate redemption or constraint violation
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Coupon mapRow(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountPercent(rs.getDouble("discount_percent"));
        c.setFixedAmount(rs.getDouble("fixed_amount"));
        c.setActive(rs.getBoolean("active"));
        c.setExpiresAt(rs.getTimestamp("expires_at"));
        c.setUsageLimit(rs.getInt("usage_limit"));
        c.setUsedCount(rs.getInt("used_count"));
        c.setMinCartValue(rs.getDouble("min_cart_value"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
