package com.theknife.app;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione JavaFX.
 * Inizializza la configurazione di rete, verifica l'integrità del file di configurazione,
 * e avvia i moduli principali: {@code Communicator} e {@code SceneManager}.
 */
public class App extends Application {
    /**
     * Metodo di avvio dell'applicazione JavaFX.
     * Carica o crea il file di configurazione {@code config.ini}, estrae l'indirizzo IP e la porta,
     * inizializza il modulo di comunicazione e il gestore delle scene.
     *
     * @param stage lo {@code Stage} principale dell'applicazione
     */
    public void start(Stage stage) {
        try {
            //default configuration
            String ip = "127.0.0.1";
            int port = 12345;

            //loads config file if exists, else creates it
            File config_file = new File("config.ini");
            boolean integrity_check = config_file.exists();
            if(config_file.exists()) {
                Scanner fs = new Scanner(config_file);
                if(fs.hasNextLine()) {
                    String line = fs.nextLine();
                    String[] parts = line.split("=");

                    if(parts.length < 2)
                        integrity_check = false;
                    else {
                        parts = parts[1].split(":");
                        if(parts.length < 2)
                            integrity_check = false;
                        else {
                            ip = parts[0];
                            try {
                                port = Integer.parseInt(parts[1]);
                            } catch(NumberFormatException e) {
                                integrity_check = false;
                            }
                        }
                    }
                } else
                    integrity_check = false;
                fs.close();
            }

            //if the file is corrupted/absent, recreate it
            if(!integrity_check) {
                FileWriter fw = new FileWriter("config.ini");
                fw.write("ip=" + ip + ':' + port);
                fw.close();
            }


            //initiate the communicator and the scene manager
            Communicator.init(ip, port);

            SceneManager.init(stage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Metodo main dell'applicazione.
     * Avvia il ciclo di vita JavaFX.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        launch(args);
    }
}