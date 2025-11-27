package com.theknife.app;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Classe singleton che gestisce l'applicazione server.
 * Responsabile dell'avvio del server TCP, dell'accettazione di connessioni client,
 * e dell'arresto controllato del server. Utilizza thread dedicato per l'accettazione
 * di client e AtomicBoolean per thread-safety.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ServerApplication {

    /** Istanza singleton della classe ServerApplication. */
    private static ServerApplication instance;

    /** Logger dell'applicazione server. */
    private final ServerLogger log = ServerLogger.getInstance();
    
    /** Flag atomico che indica se il server è in esecuzione. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** ServerSocket per l'ascolto di connessioni in ingresso. */
    private ServerSocket serverSocket;
    
    /** Thread dedicato all'accettazione di connessioni client. */
    private Thread acceptThread;

    /** Porta sulla quale il server ascolta le connessioni. */
    private int port;

    /**
     * Costruttore privato per il pattern singleton.
     */
    private ServerApplication() {}

    /**
     * Restituisce l'istanza singleton dell'applicazione server.
     *
     * @return istanza singleton di ServerApplication
     */
    public static synchronized ServerApplication getInstance() {
        if (instance == null)
            instance = new ServerApplication();
        return instance;
    }

    /**
     * Avvia il server sulla porta specificata.
     * Crea un ServerSocket e avvia il thread di accettazione delle connessioni.
     *
     * @param port numero della porta su cui ascoltare le connessioni
     * @return true se il server è stato avviato con successo, false altrimenti
     */
    public synchronized boolean start(int port) {
        if (running.get()) {
            log.warning("Tentativo di avvio ma il server è già in esecuzione.");
            return false;
        }

        this.port = port;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Impossibile avviare il server sulla porta " + port + ": " + e.getMessage());
            return false;
        }

        running.set(true);

        // Thread dedicato all'accettazione dei client
        acceptThread = new Thread(this::acceptLoop, "AcceptThread");
        acceptThread.start();

        log.info("Server avviato sulla porta " + port);
        return true;
    }

    /**
     * Loop continuo che accetta connessioni entranti da client.
     * Crea un nuovo ClientThread per ogni connessione accettata.
     * Continua fino a quando il server è in esecuzione.
     */
    private void acceptLoop() {
        log.info("Accept loop attivo, in attesa di client...");

        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.info("Nuovo client connesso da " + clientSocket.getInetAddress());

                new ClientThread(clientSocket); // multithread

            } catch (IOException e) {
                if (running.get()) {
                    log.error("Errore durante accept(): " + e.getMessage());
                }
                break;
            }
        }

        log.info("Accept loop terminato.");
    }

    /**
     * Arresta il server e chiude tutte le connessioni attive.
     * Chiude il ServerSocket e attende la terminazione del thread di accettazione.
     * Rilascia tutte le risorse del ConnectionManager.
     */
    public synchronized void stop() {
        if (!running.get()) {
            log.warning("Tentativo di stop ma il server non è attivo.");
            return;
        }

        log.info("Arresto del server in corso...");

        running.set(false);

        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            log.error("Errore durante la chiusura del ServerSocket: " + e.getMessage());
        }

        if (acceptThread != null)
            try { acceptThread.join(); } catch (InterruptedException ignored) {}

        // Chiude tutte le connessioni DB
        try {
            ConnectionManager.getInstance().flush();
        } catch (Exception e) {
            log.error("Errore durante l'operazione flush() della ConnectionCache: " + e.getMessage());
        }

        log.info("Server arrestato correttamente.");
    }

    /**
     * Verifica se il server è attualmente in esecuzione.
     *
     * @return true se il server è in esecuzione, false altrimenti
     */
    public boolean isRunning() {
        return running.get();
    }
}
