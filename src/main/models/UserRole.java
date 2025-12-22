package main.models;

public enum UserRole {
    OWNER,
    CUSTOMER,
    CARRIER,
    UNKNOWN;

    // String'den Enum'a çeviren güvenli metod
    public static UserRole fromString(String role) {
        try {
            return valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return UNKNOWN;
        }
    }
}