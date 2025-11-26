package com.theknife.app;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerApplication {

    private static ServerApplication instance;

    private final ServerLogger log = ServerLogger.getInstance();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private Thread acceptThread;

    private int port;

    private ServerApplication() {}

    public static synchronized ServerApplication getInstance() {
        if (instance == null)
            instance = new ServerApplication();
        return instance;
    }

    /**
     * Avvio del server.
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
     * Loop che accetta connessioni entranti.
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
     * Spegne il server e chiude tutte le connessioni.
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
     * Ritorna true se il server è in esecuzione.
     */
    public boolean isRunning() {
        return running.get();
    }
}
