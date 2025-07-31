package com.theknife.app;

import java.io.IOException;
import java.net.ServerSocket;

public class App {
    private static final int PORT = 12345;
    private static boolean serve = true;
    public static void main(String[] args) throws IOException {
        //final DBHandler db = new DBHandler();
        final ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server listening on port " + PORT);

        while(serve)
            new ClientThread(serverSocket.accept());
        
        serverSocket.close();
    }
}