package com.theknife.app;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Classe principale del server per l'applicazione TheKnife.
 * Fornisce un'interfaccia grafica Swing per avviare e arrestare il server TCP,
 * gestisce la connessione al database PostgreSQL e accetta connessioni client.
 */
public class App {
    /** Socket del server TCP. */
    private static ServerSocket serverSocket;

    /** Thread che gestisce le connessioni client. */
    private static Thread serverThread;

    /** Flag che indica se il server è attivo. */
    private static volatile boolean running = false;

    /**
     * Metodo principale. Avvia l'interfaccia grafica Swing.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowGUI);
    }

    /**
     * Crea e mostra la finestra principale del server.
     * Contiene pulsanti per avviare e arrestare il server,
     * e una label per lo stato corrente.
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("TheKnife Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        JLabel statusLabel = new JLabel("Server non avviato", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton startButton = new JButton("Avvia Server");
        JButton stopButton = new JButton("Spegni Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> {
            try {
                startServer(statusLabel, startButton, stopButton);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopButton.addActionListener(e -> stopServer(statusLabel, startButton, stopButton));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        frame.setLayout(new BorderLayout());
        frame.add(statusLabel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    /**
     * Avvia il server TCP sulla porta specificata.
     * Richiede le credenziali e l'URL JDBC per connettersi al database.
     * Inizializza il database e avvia il thread per gestire le connessioni client.
     *
     * @param statusLabel etichetta per aggiornare lo stato del server
     * @param startButton pulsante per avviare il server
     * @param stopButton pulsante per arrestare il server
     * @throws IOException se si verifica un errore di I/O
     * @throws SQLException se si verifica un errore SQL
     */
    private static void startServer(JLabel statusLabel, JButton startButton, JButton stopButton) throws IOException, SQLException {
        String jdbcUrl = JOptionPane.showInputDialog("JDBC URL:", "jdbc:postgresql://localhost:5432/theknife");
        String username = JOptionPane.showInputDialog("DB Username:", "postgres");
        String password = JOptionPane.showInputDialog("DB Password:", "postgres");
        String portStr = JOptionPane.showInputDialog("Porta Server:", "12345");

        int port = 12345;
        try {
            port = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException ignored) {}

        if (!DBHandler.connect(jdbcUrl, username, password)) {
            throw new IOException("Connessione al DB fallita");
        }

        int initCode = DBHandler.initDB();
        if (initCode == 1) throw new IOException("Inizializzazione DB fallita");

        serverSocket = new ServerSocket(port);
        running = true;

        serverThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (running) {
                        new ClientThread(clientSocket).start();
                    } else {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Errore nel thread del server: " + e.getMessage());
                    }
                    break;
                }
            }
        });
        serverThread.start();

        statusLabel.setText("Server attivo sulla porta " + port);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    /**
     * Arresta il server TCP e chiude il socket.
     * Ferma il thread del server e disconnette dal database.
     *
     * @param statusLabel etichetta per aggiornare lo stato del server
     * @param startButton pulsante per riavviare il server
     * @param stopButton pulsante per disabilitare l'arresto
     */
    private static void stopServer(JLabel statusLabel, JButton startButton, JButton stopButton) {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        try {
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.join(1000);
            }
        } catch (InterruptedException ignored) {}

        // 🔌 Disconnessione dal database
        DBHandler.disconnect();

        statusLabel.setText("Server spento");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}
