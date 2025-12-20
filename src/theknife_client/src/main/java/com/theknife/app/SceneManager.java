package com.theknife.app;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gestore centralizzato della navigazione tra le scene JavaFX del client.
 * 
 * <p>
 * Questa classe incapsula la logica di caricamento e cambio delle viste, 
 * fornendo un unico punto di accesso e coerente alla navigazione
 * </p>
 * 
 * <p>
 * E' stato scelto un approccio centralizzato al fine di semplificarne il 
 * controllo del flusso applicativo e la gestione dello stato
 * </p>
 */
public class SceneManager {
    /** Stage principale dell'applicazione. */
    private static Stage stage;
    /** Messaggio globale da mostrare nella scena "App". */
    private static String appMessage = null;
    /** Colore associato al messaggio (es. rosso o verde). */
    private static String appMessageColor = null;
    /** Contesto di navigazione utilizzato per ripristinare la schermata precedente. */
    private static String previousNavigation = null;

    /**
     * Costruttore privato.
     *
     * <p>La classe {@code SceneManager} fornisce esclusivamente metodi statici
     * per la gestione delle scene JavaFX e non deve essere istanziata.</p>
     */
    private SceneManager() {
        /* utility class */
    }


    /**
     * Inizializza lo Stage principale e carica la scena iniziale.
     *
     * @param s lo stage principale JavaFX
     * @throws IOException se la scena iniziale non può essere caricata
    */
    public static void init(Stage s) throws IOException {
        stage = s;
        changeScene("App");
    }

    /**
     * Cambia la scena visualizzata caricando un nuovo file FXML.
     *
     * <p>Regole:</p>
     * <ul>
     *     <li>Carica la scena associata al nome passato come parametro</li>
     *     <li>Ripulisce il messaggio globale se la nuova scena non è "App"</li>
     *     <li>In caso di errore, ripristina la scena "App" mostrando un messaggio d’avviso</li>
     * </ul>
     *
     * @param sceneName nome della scena da caricare
     * @throws IOException se il file FXML non può essere aperto
    */
    public static void changeScene(String sceneName) throws IOException {
        try {
            String path = "/scenes/" + sceneName + ".fxml";
            Parent root = FXMLLoader.load(SceneManager.class.getResource(path));
            stage.setScene(new Scene(root));
            stage.show();

            if (!"App".equals(sceneName)) {
                appMessage = null;
            }
        } catch (Exception e) {
            appMessage = "Errore caricamento scena (server offline?)";
            appMessageColor = "red";
            Parent root = FXMLLoader.load(SceneManager.class.getResource("/scenes/App.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        }
    }

    /**
     * Cambia scena impostando preventivamente il contesto di navigazione.
     *
     * <p>Questo metodo è un wrapper che:</p>
     * <ul>
     *     <li>salva il contesto di provenienza</li>
     *     <li>delegata il cambio scena a {@link #changeScene(String)}</li>
     * </ul>
     *
     * @param sceneName nome della scena di destinazione
     * @param fromContext contesto logico di provenienza
     * @throws IOException se il file FXML non può essere caricato
     */
    public static void change(String sceneName, String fromContext) throws IOException {
        setPreviousNavigation(fromContext);
        changeScene(sceneName);
    }


    /**
     * Restituisce l'eventuale messaggio globale da visualizzare nella scena "App".
     *
     * @return array contenente messaggio e colore associato,
     *         oppure {@code null} se non è presente un messaggio
    */
    public static String[] getAppMessage() {
        if (appMessage == null)
            return null;
        return new String[]{appMessage, appMessageColor};
    }


    /**
     * Imposta un messaggio di conferma da mostrare nella scena principale.
     *
     * @param text testo del messaggio da visualizzare
    */
    public static void setAppAlert(String text) {
        appMessage = text;
        appMessageColor = "green";
    }

    /**
     * Imposta un messaggio di avviso/errore da mostrare nella scena principale.
     *
     * @param text testo del messaggio da visualizzare
    */
    public static void setAppWarning(String text) {
        appMessage = text;
        appMessageColor = "red";
    }

    /**
     * Imposta il contesto di navigazione, utile per ripristinare la scena precedente.
     *
     * @param ctx identificatore logico del contesto (es. "Favorites", "ViewRestaurants")
     */
    public static void setPreviousNavigation(String ctx) {
        previousNavigation = ctx;
    }

    /**
     * Restituisce l'ultimo contesto di navigazione impostato.
     *
     * @return una stringa che rappresenta il contesto di ritorno
     */
    public static String getPreviousNavigation() {
        return previousNavigation;
    }

    /**
     * Ripristina la scena precedente se presente nel contesto di navigazione.
     *
     * <p>Se un contesto precedente è stato impostato tramite
     * {@link #setPreviousNavigation(String)}, viene caricata la relativa scena.
     * In caso contrario, viene caricata la scena principale "App".</p>
     *
     * @throws IOException se il caricamento della scena fallisce
     */
    public static void goBack() throws IOException {
        if (previousNavigation != null) {
            String target = previousNavigation;
            previousNavigation = null;
            changeScene(target);
        } else {
            changeScene("App");
        }
    }
}
