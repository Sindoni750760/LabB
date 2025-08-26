package com.theknife.app;

import java.io.IOException;

public class User {
    private static String name, surname;
    private static boolean logged_in = false, is_restaurateur;

    //function used to login the user on the client and the server
    public static String login(String username, String password) throws IOException {        
        Communicator.sendStream("login");
        Communicator.sendStream(username);
        Communicator.sendStream(password);

        String response = Communicator.readStream();

        if(response.equals("ok")) {
            Communicator.sendStream("getUserInfo");
            name = Communicator.readStream();
            surname = Communicator.readStream();
            is_restaurateur = Communicator.readStream().equals("y");
            logged_in = true;
        }

        return response;
    }

    public static void logout() throws IOException {
        Communicator.sendStream("logout");
        if(Communicator.readStream().equals("ok"))
            logged_in = false;
    }

    //handles user logout when a connection error occurs
    public static void panic() {
        logged_in = false;
    }

    //function used to get the current user info
    public static String[] getInfo() {
        if(!logged_in)
            return null;
        return new String[]{name, surname, is_restaurateur ? "y" : "n"};
    }
}