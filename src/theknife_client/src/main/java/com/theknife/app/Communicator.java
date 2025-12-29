package com.theknife.app;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Gestisce la comunicazione client–server tramite socket TCP, fornendo
 * un'interfaccia di alto livello per inviare e ricevere messaggi testuali.
 *
 * <p>Responsabilità principali:</p>
 * <ul>
 *     <li>stabilire una connessione con il server</li>
 *     <li>inviare messaggi su canale socket</li>
 *     <li>leggere risposte sincrone dal server</li>
 *     <li>gestire la disponibilità del server</li>
 *     <li>chiudere correttamente la connessione</li>
 * </ul>
 *
 * <p>Formato di comunicazione:</p>
 * Ogni messaggio viene inviato come singola riga terminata da newline,
 * e il server risponde anch'esso con righe testuali singole.
 *
 * <p>Questa classe è utilizzata da tutti i controller UI e dagli handler di rete
 * per eseguire operazioni sulle risorse lato server.</p>
 */

public class Communicator {
    /** Socket TCP per la comunicazione col server. */
    private static Socket socket;
    /** Reader per la lettura UTF-8 dal server. */
    private static BufferedReader reader;
    /** Writer UTF-8 per l'invio dei messaggi. */
    private static BufferedWriter writer;
    /** Indirizzo IP del server. */
    private static String ip;
    /** Porta TCP del server. */
    private static int port;
    /** Stato di raggiungibilità del server. */
    private static boolean serverReachable = false;

    /**
     * Costruttore privato.
     *
     * <p>La classe {@code Communicator} è una utility class che fornisce
     * esclusivamente metodi statici per la gestione della comunicazione
     * client–server e non deve essere istanziata.</p>
     */
    private Communicator() {
        /* utility class */
    }

    /**
     * Inizializza il communicator memorizzando host e porta e tenta
     * immediatamente la connessione usando {@link #connect()}.
     *
     * @param _ip indirizzo IP del server target
     * @param _port porta TCP su cui il server attende connessioni
     *
     * @throws IOException se la connessione fallisce durante l'inizializzazione
     */
    public static void init(String _ip, int _port) throws IOException {
        ip = _ip;
        port = _port;
        connect();
    }

    /**
     * Ritorna {@code true} se il server risulta attualmente raggiungibile
     * e la connessione non è stata interrotta.
     *
     * @return stato di raggiungibilità server
     */
    public static boolean isOnline() {
        return serverReachable;
    }

    /**
     * Tenta l'apertura della connessione TCP con il server e inizializza
     * i flussi I/O per la comunicazione.
     *
     * <p>In caso di fallimento:</p>
     * <ul>
     *     <li>viene impostato {@code serverReachable = false}</li>
     *     <li>viene mostrata una finestra Swing di avviso</li>
     * </ul>
     *
     * @return {@code true} se connessione stabilita correttamente, {@code false} altrimenti
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
     * Invia una stringa terminata da {@code \n} al server.
     *
     * <p>Il metodo esegue automaticamente il flush del writer,
     * poiché la comunicazione avviene con messaggi discreti.</p>
     *
     * @param msg messaggio da inviare
     * @return {@code true} se l'invio è andato a buon fine, {@code false} se si verifica errore
    */
    public static boolean send(String msg) {
        try {
            String escapeMsg = msg.replace("\n", " /$%/ ");
            ClientLogger.getInstance().info("Communicator.send() - Sending: " + escapeMsg);
            writer.write(escapeMsg + "\n");
            writer.flush();
            return true;

        } catch (IOException e) {
            ClientLogger.getInstance().error("Communicator.send() - Error: " + e.getMessage());
            serverReachable = false;
            close();
            return false;
        }
    }

    /**
     * Legge una riga di risposta dal server.
     *
     * <p>Comportamenti particolari:</p>
     * <ul>
     *     <li>Se viene restituito {@code null}, significa che il server ha chiuso la connessione</li>
     *     <li>In caso di errore viene chiusa la connessione locale</li>
     * </ul>
     *
     * @return stringa letta dal server oppure {@code null} se il server è disconnesso
    */
    public static String read() {
        try {
            String msg = reader.readLine();
            if (msg == null) {
                serverReachable = false;
                close();
                return null;
            }
            
            ClientLogger.getInstance().info("Communicator.read() - Received: " + msg);
            String unescapeMsg = msg.replace(" /$%/ ", "\n");
            return unescapeMsg;

        } catch (IOException e) {
            ClientLogger.getInstance().error("Communicator.read() - Error: " + e.getMessage());
            serverReachable = false;
            close();
            return null;
        }
    }

    /**
     * Invio di più messaggi consecutivi con attesa di un'unica risposta finale.
     *
     * <p>Utilizzato per operazioni del tipo:</p>
     * <pre>
     * request("addReview", "42", "5", "testo recensione")
     * </pre>
     *
     * @param args lista messaggi da inviare in sequenza
     * @return risposta letta dal server
     *
     * @throws IOException se il server non risulta online
    */

    public static String request(String... args) throws IOException {
        if (!serverReachable){
            return null;
        }

        for (String s : args)
            send(s);

        return read();
    }


     /**
     * Chiude completamente la connessione verso il server,
     * includendo socket e flussi.
     *
     * <p>Il metodo è idempotente e tollera chiamate multiple.</p>
     */
    public static void close() {
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}

        serverReachable = false;
    }
}
