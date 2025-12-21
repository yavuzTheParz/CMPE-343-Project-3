package main.utils; // Klasör yapınıza göre paket ismi

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil { // Sınıf adı dosya adıyla AYNI olmalı

    // Şifreyi SHA-256 ile hashler
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Girilen şifre ile veritabanındaki hash'i karşılaştırır
    public static boolean checkPassword(String plainPassword, String storedHash) {
        String newHash = hashPassword(plainPassword);
        return newHash.equals(storedHash);
    }
}