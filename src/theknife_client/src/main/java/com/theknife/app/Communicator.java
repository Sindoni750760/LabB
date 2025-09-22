package com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

/**
 * Classe utility per gestire la comunicazione con il server tramite socket TCP.
 * Fornisce metodi per inviare e ricevere stringhe, gestire errori di connessione,
 * e verificare lo stato della connessione.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class Communicator {
    /** Socket TCP utilizzato per la comunicazione con il server. */
    private static Socket socket;

    /** Lettore per ricevere dati dal server. */
    private static BufferedReader reader;

    /** Stream di output per inviare dati al server. */
    private static OutputStream os;

    /** Indirizzo IP del server. */
    private static String ip;

    /** Porta del server. */
    private static int port;

    /** Stato della connessione al server. */
    private static boolean server_reachable = true;


    /**
     * Inizializza la comunicazione con il server usando IP e porta specificati.
     * Se la connessione fallisce, gestisce l'errore in modo parziale.
     *
     * @param _ip indirizzo IP del server
     * @param _port porta del server
     * @throws UnknownHostException se l'host non è raggiungibile
     * @throws IOException se si verifica un errore di I/O
     */
    public static void init(String _ip, int _port) throws UnknownHostException, IOException {
        ip = _ip;
        port = _port;
        if(!connect())
            handleConnectionError(false);
    }

    /**
     * Verifica se il server è attualmente raggiungibile.
     *
     * @return {@code true} se la connessione è attiva, {@code false} altrimenti
     */
    public static boolean isOnline() {
        return server_reachable;
    }

    /**
     * Gestisce gli errori di connessione.
     * Mostra un messaggio di avviso e, se richiesto, torna alla schermata principale.
     *
     * @param complete {@code true} se l'errore richiede il cambio scena
     * @throws IOException se si verifica un errore durante il cambio scena
     */
    private static void handleConnectionError(boolean complete) throws IOException {
        User.panic();
        SceneManager.setAppWarning("Impossibile comunicare con il server");
        //does the integral portion of the handling
        if(complete)
            SceneManager.changeScene("App");
            JOptionPane.showMessageDialog(null, "IL SERVER E' SPENTO. \n PER POTER UTILIZZARE L'APP, ACCENDERE IL SERVER");
        server_reachable = false;
    }

    /**
     * Tenta di stabilire una connessione con il server.
     * Configura gli stream di input/output.
     *
     * @return {@code true} se la connessione è riuscita, {@code false} altrimenti
     * @throws UnknownHostException se l'host non è valido
     * @throws IOException se si verifica un errore di I/O
     */
    public static boolean connect() throws UnknownHostException, IOException {
        try {
            //creates a new socket and configures the input/output streams
            socket = new Socket(ip, port);
            socket.setSoTimeout(1000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
            os = socket.getOutputStream();
            server_reachable = true;
            return socket != null;
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "IL SERVER E' SPENTO. \n PER POTER UTILIZZARE L'APP, ACCENDERE IL SERVER");
            return false;
        }
    }

    /**
     * Legge una stringa dal server.
     * Se la connessione è chiusa o fallisce, gestisce l'errore e restituisce una stringa vuota.
     *
     * @return stringa ricevuta dal server, oppure stringa vuota in caso di errore
     * @throws IOException se si verifica un errore di lettura
     */
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

    /**
     * Invia una stringa al server.
     * Se la connessione è chiusa o fallisce, gestisce l'errore.
     *
     * @param msg stringa da inviare al server
     * @throws IOException se si verifica un errore di scrittura
     */
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