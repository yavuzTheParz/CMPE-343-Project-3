package main.utils;

/**
 * Simple session holder for the current username during application runtime.
 */
public class SessionManager {
    // Holds the currently logged-in username for the running application
    private static String currentUsername;

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }
}