package com.theknife.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import javafx.application.Platform;

public class Communicator {

    private static Socket socket;
    private static BufferedReader reader;
    private static BufferedWriter writer;

    private static String ip;
    private static int port;

    private static boolean serverReachable = false;

    public static void init(String _ip, int _port) throws IOException {
        ip = _ip;
        port = _port;
        connect();
    }

    public static boolean isOnline() {
        return serverReachable;
    }

    public static boolean connect() {
        try {
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            serverReachable = true;
            return true;

        } catch (IOException e) {
            serverReachable = false;
            JOptionPane.showMessageDialog(null,
                    "IL SERVER È SPENTO.\nPER POTER UTILIZZARE L'APP, ACCENDERE IL SERVER");
            return false;
        }
    }

    /**
     * Invia una stringa al server
     */
    public static void send(String msg) throws IOException {
        try {
            writer.write(msg + "\n");
            writer.flush();
        } catch (IOException e) {
            serverReachable = false;
            throw e;
        }
    }

    /**
     * Legge una stringa dal server
     */
    public static String read() throws IOException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            serverReachable = false;
            throw e;
        }
    }

    /**
     * Invia un comando + parametri, ritorna risposta singola
     */
    public static String request(String... args) throws IOException {
        if (!serverReachable) {
            throw new IOException("Server offline");
        }

        for (String s : args) {
            send(s);
        }

        return read();
    }

    /**
     * Chiude in modo pulito
     */
    public static void close() {
        try {
            if (reader != null) reader.close();
        } catch (Exception ignored) {}

        try {
            if (writer != null) writer.close();
        } catch (Exception ignored) {}

        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (Exception ignored) {}

        serverReachable = false;
    }

}
