package com.ka4piece.utilities;

import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;

public class PasswordUtil {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL = UPPER + LOWER + SPECIAL + DIGITS;
    private static final int LENGTH = 12;
    private static final String TEMP_PREFIX = "Tmp#1";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String generateTemporaryPassword() {
        StringBuilder temp = new StringBuilder(TEMP_PREFIX);
        while (temp.length() < LENGTH) {
            temp.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }
        return shuffleKeepingPrefix(temp.toString());
    }

    private static String shuffleKeepingPrefix(String value) {
        char[] chars = value.toCharArray();
        for (int i = chars.length - 1; i > TEMP_PREFIX.length(); i--) {
            int j = TEMP_PREFIX.length() + RANDOM.nextInt(i - TEMP_PREFIX.length() + 1);
            char swap = chars[i];
            chars[i] = chars[j];
            chars[j] = swap;
        }
        return new String(chars);
    }

    public static boolean isTemporaryPassword(String password) {
        return password != null && password.startsWith(TEMP_PREFIX);
    }

    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(10));
    }

    public static boolean verifyPassword(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainText, storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    public static String validate(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit.";
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            return "Password must contain at least one special character (!@#$%^&*).";
        }
        if (isTemporaryPassword(password)) {
            return "Choose a different password. The temporary password format is not allowed.";
        }
        return null;
    }
}
