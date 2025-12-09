package com.theknife.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Singleton responsabile della gestione del server TCP.
 * <p>
 * Questa classe incapsula l'intero ciclo di vita del server:
 * </p>
 * <ul>
 *     <li>inizializzazione e apertura del {@link ServerSocket}</li>
 *     <li>accettazione concorrente dei client tramite thread dedicato</li>
 *     <li>chiusura controllata e rilascio delle risorse</li>
 * </ul>
 *
 * <p>
 * Il flag {@link #running} è atomico per garantire visibilità
 * e coerenza tra thread differenti durante la fase di arresto.
 * </p>
 *
 * <p>Pattern utilizzato: Singleton</p>
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */
public class ServerApplication {

    /** Istanza singleton che rappresenta l’applicazione server. */
    private static ServerApplication instance;

    /** Logger dell'applicazione server. */
    private final ServerLogger log = ServerLogger.getInstance();

    /**
     * Flag atomico che indica lo stato del server.
     * <p>{@code true}: il server è avviato e accetta client</p>
     * <p>{@code false}: accept loop disattivo</p>
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Socket principale che rimane in ascolto sulla porta configurata. */
    private ServerSocket serverSocket;

    /** Thread che gestisce l'accettazione indipendente dei client. */
    private Thread acceptThread;

    /**
     * Costruttore privato per garantire l'unicità dell'istanza.
     */
    private ServerApplication() {}

    /**
     * Restituisce l’unica istanza del server.
     * <p>
     * L’accesso è sincronizzato per evitare race condition
     * sulla prima inizializzazione.
     * </p>
     *
     * @return istanza singleton del server
     */
    public static synchronized ServerApplication getInstance() {
        if (instance == null)
            instance = new ServerApplication();
        return instance;
    }

    /**
     * Avvia il server sulla porta indicata, inizializzando il {@link ServerSocket}
     * e attivando il thread che gestisce new incoming connections.
     *
     * @param port porta TCP su cui avviare il server
     * @return {@code true} se l’avvio è avvenuto correttamente,
     *         {@code false} se la porta non è disponibile o è già in run
     */
    public synchronized boolean start(int port) {
        if (running.get()) {
            log.warning("Start ignorato: server già avviato.");
            return false;
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Errore di bind sulla porta " + port + ": " + e.getMessage());
            return false;
        }

        running.set(true);

        acceptThread = new Thread(this::acceptLoop, "AcceptThread");
        acceptThread.start();

        log.info("Server avviato sulla porta " + port);
        return true;
    }

    /**
     * Loop continuo che accetta connessioni entranti finché {@link #running}
     * rimane {@code true}.
     * <p>
     * Ogni chiamata a {@link ServerSocket#accept()} è bloccante;
     * la chiusura del server disattiva {@code running} e forza l'app exit da accept(),
     * permettendo un arresto coerente.
     * </p>
     * <p>
     * Per ogni connessione accettata viene creato un nuovo thread
     * {@link ClientThread} che gestisce l’I/O client-server.
     * </p>
     */
    private void acceptLoop() {
        log.info("Accept loop avviato; attesa client...");

        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.info("Client connesso: " + clientSocket.getInetAddress());

                new ClientThread(clientSocket);

            } catch (IOException e) {
                if (running.get()) {
                    log.error("Errore in accept(): " + e.getMessage());
                }
                break;
            }
        }

        log.info("Accept loop terminato.");
    }

    /**
     * Arresta il server e interrompe in modo ordinato tutti i listener.
     * <p>
     * Operazioni principali:
     * </p>
     * <ol>
     *     <li>Disabilita il flag di stato {@link #running}</li>
     *     <li>Chiude il {@link ServerSocket}</li>
     *     <li>Attende che il thread di accettazione termini</li>
     * </ol>
     *
     * <p>
     * Non forza la terminazione degli {@link ClientThread} già attivi,
     * poiché ciascuno gestisce autonomamente la connessione
     * e si chiude sul successivo EOF lato client.
     * </p>
     */
    public synchronized void stop() {
        if (!running.get()) {
            log.warning("Stop ignorato: server non in esecuzione.");
            return;
        }

        log.info("Arresto server in corso...");
        running.set(false);

        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            log.error("Errore nella chiusura del socket principale: " + e.getMessage());
        }

        if (acceptThread != null) {
            try {
                acceptThread.join();
            } catch (InterruptedException ignored) {}
        }

        log.info("Server arrestato correttamente.");
    }

    /**
     * Verifica se il server è attualmente attivo.
     *
     * @return {@code true} se {@link #start(int)} è stato eseguito correttamente
     *         e non è ancora stato invocato {@link #stop()},
     *         {@code false} altrimenti
     */
    public boolean isRunning() {
        return running.get();
    }
}
