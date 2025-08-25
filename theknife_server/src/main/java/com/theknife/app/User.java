package com.theknife.app;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class User {
    //https://dzone.com/articles/secure-password-hashing-in-java
    private static String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    private static boolean verifyPassword(String inputPassword, String storedHash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(inputPassword, storedHash);
    }

    /*requirements:
     * length between 8 and 32 characters
     * at least one lowercase alphabetical character
     * at least one uppercase alphabetical character
     * at least one numerical character
     * at least one special character
     */
    private static boolean checkPassword(String password) {
        if(password.length() < 8 || password.length() > 32)
            return false;

        boolean lalph, ualph, num, spec;
        lalph = ualph = num = spec = false;

        for(char c : password.toCharArray()) {
            if(c > 'a' && c < 'z')
                lalph = true;
            else if(c > 'A' && c < 'Z')
                ualph = true;
            else if(c > '0' && c < '9')
                num = true;
            else
                spec = true;
        }

        return lalph && ualph && num && spec;
    }

   public static String registerUser(String nome, String cognome, String username, String password, String data_nascita, String latitude, String longitude, boolean is_ristoratore) throws SQLException {
        //checks missing parameters
        if(nome.trim().isEmpty() || cognome.trim().isEmpty() || username.trim().isEmpty())
            return "missing";

        if(!checkPassword(password))
            return "password";

        //handles birth date
        long time = -1;
        try {
            if(!data_nascita.equals("-"))
                time = new SimpleDateFormat("yyyy-MM-dd").parse(data_nascita).getTime();
        } catch(ParseException e) {
            return "date";
        }

        //is the birth date is after today, returns the "date" error
        if(time > new java.util.Date().getTime())
            return "date";

         double latitude_double, longitude_double;
        try {
            latitude_double = Double.parseDouble(latitude);
            longitude_double = Double.parseDouble(longitude);
        } catch(NumberFormatException e) {
            return "coordinates";
        }

        return DBHandler.addUser(nome, cognome, username, hashPassword(password), time, latitude_double, longitude_double, is_ristoratore) ? "ok" : "username";

    }

    public static int loginUser(String username, String password) throws SQLException {
        String[] user_info = DBHandler.getUserLoginInfo(username);

        //if no user was found with given username
        if(user_info == null)
            return -1;

        if(verifyPassword(password, user_info[1]))
            return Integer.parseInt(user_info[0]);

        //password missmatch
        return -2;
    }
}
