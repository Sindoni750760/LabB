package com.theknife.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Scanner;

public class App {
    private static final int PORT = 12345;
    private static boolean serve = true;
    public static void main(String[] args) throws IOException, SQLException {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/theknife";
        String username = "postgres";
        String password = "postgres";
        Scanner myScanner = new Scanner(System.in);
        String line;

        //requests db configuration to the user
        System.out.println("Database configuration (leave empty for default values)");

        System.out.print("jdbc url: ");
        line = myScanner.nextLine().trim();
        if(!line.isEmpty())
            jdbcUrl = line;

        System.out.print("db username: ");
        line = myScanner.nextLine().trim();
        if(!line.isEmpty())
            username = line;
        
        System.out.print("db password: ");
        line = myScanner.nextLine().trim();
        if(!line.isEmpty())
            password = line;
        
        System.out.print("server port: ");
        line = myScanner.nextLine().trim();
        if(!line.isEmpty())
            try {
                port = Integer.parseInt(line);
            } catch(NumberFormatException e) {}

        //initializes db handler object
        if(!DBHandler.connect(jdbcUrl, username, password)) {
            System.out.println("Error in the connection with the database, press enter to exit");
            myScanner.nextLine();
            myScanner.close();
            return;
        }

        //initializes the db and handles response code
        switch(DBHandler.initDB()) {
            case 0:
                System.out.println("Database initialized successfully");
                break;
            case 1:
                System.out.println("Database initialization failed, press enter to exit");
                myScanner.nextLine();
                myScanner.close();
                return;
            case 2:
                System.out.println("Database already initialized");
                break;
            default:
                System.err.println("Unhandled database initialization result, press enter to exit");
                myScanner.nextLine();
                myScanner.close();
                return;
        }
        
        myScanner.close();

        //initializes server socket
        final ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server listening on port " + port);

        //listens for clients to connect
        while(serve)
            new ClientThread(serverSocket.accept());
        
        serverSocket.close();
    }
}
