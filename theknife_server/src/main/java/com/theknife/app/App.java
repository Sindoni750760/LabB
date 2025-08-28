package com.theknife.app;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

public class App {
    private static ServerSocket serverSocket;
    private static Thread serverThread;
    private static volatile boolean running = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowGUI);
    }

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

    private static void startServer(JLabel statusLabel, JButton startButton, JButton stopButton) throws IOException, SQLException {
        // Parametri di default
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
            try {
                while (running) {
                    new ClientThread(serverSocket.accept()).start();
                }
            } catch (IOException ignored) {}
        });
        serverThread.start();

        statusLabel.setText("Server attivo sulla porta " + port);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private static void stopServer(JLabel statusLabel, JButton startButton, JButton stopButton) {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        statusLabel.setText("Server spento");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}
