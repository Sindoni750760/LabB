package com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Communicator {
    private static Socket socket;
    private static BufferedReader reader;
    private static OutputStream os;
    private static String ip;
    private static int port;
    private static boolean server_reachable = true;

    public static void init(String _ip, int _port) throws UnknownHostException, IOException {
        ip = _ip;
        port = _port;
        if(!connect())
            handleConnectionError(false);
    }

    public static boolean isOnline() {
        return server_reachable;
    }

    private static void handleConnectionError(boolean complete) throws IOException {
        User.panic();
        SceneManager.setAppWarning("Impossibile comunicare con il server");
        //does the integral portion of the handling
        if(complete)
            SceneManager.changeScene("App");
        server_reachable = false;
    }

    public static boolean connect() throws UnknownHostException, IOException {
        try {
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
            os = socket.getOutputStream();
            server_reachable = true;
            return socket != null;
        } catch(IOException e) {
            return false;
        }
    }

    public static String readStream() throws IOException {
        try {
            if(socket.isClosed())
                throw new IOException();
            return reader.readLine();
        } catch(IOException e) {
            handleConnectionError(true);
            return "";
        }
    }

    public static void sendStream(String msg) throws IOException {
        try {
            if(socket.isClosed())
                throw new IOException();
            os.write((msg + '\n').getBytes(StandardCharsets.UTF_8));
        } catch(IOException e) {
            handleConnectionError(true);
        }
    }
}