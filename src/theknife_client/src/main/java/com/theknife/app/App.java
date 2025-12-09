package com.theknife.app;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Classe di avvio dell'applicazione JavaFX.
 *
 * <p>Responsabilità principali:</p>
 * <ul>
 *     <li>verifica, lettura e ricreazione del file {@code config.ini}</li>
 *     <li>inizializzazione della connessione client verso il server tramite {@link Communicator}</li>
 *     <li>inizializzazione del {@link SceneManager} con lo {@link Stage} principale</li>
 *     <li>avvio del ciclo di vita JavaFX tramite {@code start()}</li>
 * </ul>
 *
 * <p>Il file di configurazione ha formato:</p>
 * <pre>ip=HOST:PORTA</pre>
 *
 * In caso di integrità compromessa, viene ricreato con valori di default
 * (<code>127.0.0.1:12345</code>).
 */

public class App extends Application {

    /**
     * Metodo di avvio JavaFX.
     *
     * <p>Esegue le seguenti operazioni:</p>
     * <ol>
     *     <li>Legge il file {@code config.ini}</li>
     *     <li>Verifica i parametri host e porta</li>
     *     <li>Ricrea il file in caso di formattazione errata o mancante</li>
     *     <li>Inizializza la connessione verso il server</li>
     *     <li>Inizializza {@link SceneManager} caricando l'interfaccia iniziale</li>
     * </ol>
     *
     * @param stage lo {@link Stage} principale su cui verranno caricate le scene
     */
    @Override
    public void start(Stage stage) {
        try {
            String ip = "127.0.0.1";
            int port = 12345;

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

            if (!integrity_check) {
                FileWriter fw = new FileWriter("config.ini");
                fw.write("ip=" + ip + ':' + port);
                fw.close();
            }

            Communicator.init(ip, port);

            SceneManager.init(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point dell'applicazione.
     *
     * <p>Si limita a delegare a {@link Application#launch(String...)}
     * l'avvio del framework JavaFX.</p>
     *
     * @param args eventuali parametri (ignorati)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
