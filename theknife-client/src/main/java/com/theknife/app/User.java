package com.theknife.app;

public class User {
    private static String name, surname;
    private static boolean logged_in = false, is_restaurateur;

    public static void login(String _name, String _surname, boolean _is_restaurateur) {
        name = _name;
        surname = _surname;
        is_restaurateur = _is_restaurateur;
        logged_in = true;
    }

    public static void logout() {
        logged_in = false;
    }

    public static String[] getInfo() {
        if(!logged_in)
            return null;
        return new String[]{name, surname, is_restaurateur ? "y" : "n"};
    }
}