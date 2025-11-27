package com.theknife.app;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Avvio dell'applicazione JavaFX.
 * Gestisce:
 *  - lettura/creazione del file di configurazione config.ini
 *  - inizializzazione del Communicator e connessione al server
 *  - avvio dell'heartbeat (ping periodico)
 *  - caricamento della scena iniziale tramite SceneManager
 */

public class App extends Application {

    /**
     * Metodo di avvio dell'applicazione JavaFX.
     *
     * @param stage lo {@code Stage} principale
     */
    @Override
    public void start(Stage stage) {
        try {
            // default config
            String ip = "127.0.0.1";
            int port = 12345;

            // file config.ini
            File config_file = new File("config.ini");
            boolean integrity_check = config_file.exists();

            if (config_file.exists()) {
                Scanner fs = new Scanner(config_file);
                if (fs.hasNextLine()) {
                    String line = fs.nextLine();
                    String[] parts = line.split("=");

                    if (parts.length < 2) {
                        integrity_check = false;
                    } else {
                        parts = parts[1].split(":");
                        if (parts.length < 2) {
                            integrity_check = false;
                        } else {
                            ip = parts[0];
                            try {
                                port = Integer.parseInt(parts[1]);
                            } catch (NumberFormatException e) {
                                integrity_check = false;
                            }
                        }
                    }
                } else {
                    integrity_check = false;
                }
                fs.close();
            }

            // ricrea config.ini se inesistente o corrotto
            if (!integrity_check) {
                FileWriter fw = new FileWriter("config.ini");
                fw.write("ip=" + ip + ':' + port);
                fw.close();
            }

            // inizia comunicazione col server
            Communicator.init(ip, port);

            // inizializza gestione scene
            SceneManager.init(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point dell'applicazione.
     *
     * @param args parametri CLI (non usati)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
