package com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ClientThread extends Thread {
    private String ip;
    private BufferedReader reader;
    private OutputStream os;
    private int user_id = -1;

    public ClientThread(Socket socket) throws IOException {
        ip = socket.getInetAddress().toString();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        os = socket.getOutputStream();
        start();
    }

    public void run() {
        try {
            exec();
        } catch(InterruptedException | IOException | SQLException e) {
            if(e.getMessage().equals("Connection reset"))
                log("Disconnected");
            else
                e.printStackTrace();
        }
    }

    private void log(String msg) {
        System.out.println("[" + ip + "] " + msg);
    }

    private String readStream() throws IOException {
        return reader.readLine();
    }

    private void sendStream(String msg) throws IOException {
        os.write((msg + '\n').getBytes(StandardCharsets.UTF_8));
    }

    private void exec() throws InterruptedException, IOException, SQLException {
        log("Connected");

        String cmd = readStream();
        while(!cmd.equals("disconnect")) {
            log("Received: " + cmd);
            switch (cmd) {
                case "ping":
                    sendStream("pong");
                    break;
                case "register":
                    String nome = readStream();
                    String cognome = readStream();
                    String username = readStream();
                    String password = readStream();
                    String data_nascita = readStream();
                    boolean is_ristoratore = readStream().equals("y");

                    sendStream(User.registerUser(nome, cognome, username, password, data_nascita, is_ristoratore));
                    break;
                case "login":
                    username = readStream();
                    password = readStream();

                    int logged_user_id = User.loginUser(username, password);
                    if(logged_user_id > 0) {
                        sendStream("ok");
                        user_id = logged_user_id;
                    }
                    else if(logged_user_id == -1) //no user was found with given username
                        sendStream("username");
                    else if(logged_user_id == -2) //password mismatch
                        sendStream("password");
                    break;
                case "getUserInfo":
                    if(user_id < 1)
                        sendStream("unauthorized");
                    else {
                        String[] user_info = DBHandler.getUserInfo(user_id);
                        sendStream(user_info[0]);
                        sendStream(user_info[1]);
                        sendStream(user_info[2]);
                    }
                case "logout":
                    user_id = -1;
                    sendStream("ok");
                    break;
                default:
                    sendStream("Unknown command");
                    break;
            }
            cmd = readStream();
        }
    }
}