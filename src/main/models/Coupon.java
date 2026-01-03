package main.models;

import java.sql.Timestamp;

public class Coupon {
    /**
     * Model representing a coupon/promo code with percent or fixed discounts,
     * expiry and usage limits.
     */
    private int id;
    private String code;
    private double discountPercent;
    private double fixedAmount;
    private boolean active;
    private Timestamp expiresAt;
    private int usageLimit;
    private int usedCount;
    private double minCartValue;
    private Timestamp createdAt;

    public Coupon() {}

    public Coupon(int id, String code, double discountPercent, double fixedAmount, boolean active, Timestamp expiresAt,
                  int usageLimit, int usedCount, double minCartValue, Timestamp createdAt) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.fixedAmount = fixedAmount;
        this.active = active;
        this.expiresAt = expiresAt;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.minCartValue = minCartValue;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
    public double getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(double fixedAmount) { this.fixedAmount = fixedAmount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public double getMinCartValue() { return minCartValue; }
    public void setMinCartValue(double minCartValue) { this.minCartValue = minCartValue; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
