package com.theknife.app;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Gestisce la comunicazione client-server tramite socket TCP.
 * Contiene:
 *  - connessione
 *  - invio/ricezione messaggi
 */

public class Communicator {

    private static Socket socket;
    private static BufferedReader reader;
    private static BufferedWriter writer;

    private static String ip;
    private static int port;

    private static boolean serverReachable = false;


    /**
     * Inizializza il communicator e tenta subito la connessione.
     */
    public static void init(String _ip, int _port) throws IOException {
        ip = _ip;
        port = _port;
        connect();
    }

    /**
     * Restituisce true se il server è attualmente raggiungibile.
     */
    public static boolean isOnline() {
        return serverReachable;
    }

    /**
     * Tenta la connessione al server.
     * Se fallisce, mostra un warning all'utente.
     */
    public static boolean connect() {
        try {
            socket = new Socket(ip, port);

            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            );

            serverReachable = true;
            return true;

        } catch (IOException e) {
            serverReachable = false;
            JOptionPane.showMessageDialog(
                    null,
                    "IL SERVER È SPENTO.\nPER POTER UTILIZZARE L'APP, ACCENDERE IL SERVER"
            );
            return false;
        }
    }

    /**
     * Invia una riga al server.
     */
    public static boolean send(String msg){
        try {
            ClientLogger.getInstance().info("Communicator.send() - Sending: " + msg);
            writer.write(msg + "\n");
            writer.flush();
            return true;

        } catch (IOException e) {
            ClientLogger.getInstance().error("Communicator.send() - Error: " + e.getMessage());
            serverReachable = false;
            return false;
        }
    }

    /**
     * Legge una riga dal server.
     * Se arriva null, significa che il server si è disconnesso.
     */
    public static String read(){
        try {
            String msg = reader.readLine();
            ClientLogger.getInstance().info("Communicator.read() - Received: " + msg);

            if (msg == null) {
                serverReachable = false;
                close();
                return null;
            }

            return msg;

        } catch (IOException e) {
            ClientLogger.getInstance().error("Communicator.read() - Error: " + e.getMessage());
            serverReachable = false;
            close();
            return null;
        }
    }


    /**
     * Versione helper che invia multipli messaggi e attende una singola risposta.
     */
    public static String request(String... args) throws IOException {
        if (!serverReachable)
            throw new IOException("Server offline");

        for (String s : args)
            send(s);

        return read();
    }


    /**
     * Chiude la connessione con il server.
     */
    public static void close() {
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}

        serverReachable = false;
    }
}
