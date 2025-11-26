package com.theknife.app;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityManager {

    private static SecurityManager instance = null;
    private final BCryptPasswordEncoder encoder;

    private SecurityManager() {
        encoder = new BCryptPasswordEncoder();
    }

    public static synchronized SecurityManager getInstance() {
        if (instance == null)
            instance = new SecurityManager();
        return instance;
    }

    public String hashPassword(String raw) {
        return encoder.encode(raw);
    }

    public boolean verifyPassword(String raw, String hash) {
        return encoder.matches(raw, hash);
    }

    public boolean checkPasswordStrength(String pw) {
        if (pw.length() < 8 || pw.length() > 32) return false;
        boolean lower = false, upper = false, digit = false, special = false;

        for (char c : pw.toCharArray()) {
            if (Character.isLowerCase(c)) lower = true;
            else if (Character.isUpperCase(c)) upper = true;
            else if (Character.isDigit(c)) digit = true;
            else if (!Character.isWhitespace(c)) special = true;
        }
        return lower && upper && digit && special;
    }
}
