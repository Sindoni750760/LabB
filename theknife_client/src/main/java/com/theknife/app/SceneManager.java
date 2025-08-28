package com.theknife.app;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe utility per la gestione delle scene JavaFX.
 * Permette di cambiare scena, inizializzare lo {@link Stage} principale,
 * e visualizzare messaggi di stato (alert o warning) nella schermata principale.
 */
public class SceneManager {
    /** Stage principale dell'applicazione JavaFX. */
    private static Stage stage;

    /** Messaggio da visualizzare nella scena "App". */
    private static String appMessage = null;

    /** Colore del messaggio da visualizzare ("green" per alert, "red" per warning). */
    private static String appMessageColor = null;

    /**
     * Inizializza il gestore delle scene con lo {@code Stage} principale.
     * Imposta la scena iniziale su "App".
     *
     * @param s lo {@code Stage} principale dell'applicazione
     * @throws IOException se il file FXML non può essere caricato
     */
    public static void init(Stage s) throws IOException {
        stage = s;
        changeScene("App");
    }

    /**
     * Cambia la scena corrente con quella specificata.
     * Se la scena non è "App", cancella eventuali messaggi precedenti.
     *
     * @param sceneName nome della scena da caricare (senza estensione)
     * @throws IOException se il file FXML non può essere caricato
     */
    public static void changeScene(String sceneName) throws IOException {
        //clears the message displayed in the App scene when changing scene
        if(!sceneName.equals("App"))
            appMessage = null;

        String scene_path = "/scenes/" + sceneName + ".fxml";
        Parent root = FXMLLoader.load(SceneManager.class.getResource(scene_path));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Restituisce il messaggio da visualizzare nella scena "App", se presente.
     *
     * @return array contenente il testo del messaggio e il colore, oppure {@code null} se assente
     */
    public static String[] getAppMessage() {
        if(appMessage == null)
            return null;

        return new String[]{appMessage, appMessageColor};
    }

    /**
     * Imposta un messaggio di tipo "alert" da visualizzare nella scena "App".
     * Il colore associato sarà verde.
     *
     * @param text testo del messaggio
     */
    public static void setAppAlert(String text) {
        appMessage = text;
        appMessageColor = "green";
    }

    /**
     * Imposta un messaggio di tipo "warning" da visualizzare nella scena "App".
     * Il colore associato sarà rosso.
     *
     * @param text testo del messaggio
     */
    public static void setAppWarning(String text) {
        appMessage = text;
        appMessageColor = "red";
    }
}