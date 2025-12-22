package main.utils;

public class SessionManager {
    // Uygulama boyunca giriş yapan kullanıcıyı burada tutacağız
    private static String currentUsername;

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }
}