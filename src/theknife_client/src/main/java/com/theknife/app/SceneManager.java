package com.theknife.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe utility per la gestione delle scene JavaFX.
 * Permette di cambiare scena, inizializzare lo {@link Stage} principale,
 * e visualizzare messaggi di stato (alert o warning) nella schermata principale.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */

public class SceneManager {

    private static Stage stage;

    private static String appMessage = null;
    private static String appMessageColor = null;


    /**
     * Inizializza il gestore delle scene.
     */
    public static void init(Stage s) throws java.io.IOException {
        stage = s;
        changeScene("App");
    }


    /**
     * Cambia scena con controllo del server raggiungibile.
     */
    public static void changeScene(String sceneName) throws java.io.IOException {

        if (!sceneName.equals("App")) {
            if (!Communicator.isOnline()) {
                appMessage = "Il server non è raggiungibile";
                appMessageColor = "red";
                sceneName = "App";
            }
        }

        if (!sceneName.equals("App"))
            appMessage = null;

        String path = "/scenes/" + sceneName + ".fxml";
        Parent root = FXMLLoader.load(SceneManager.class.getResource(path));
        stage.setScene(new Scene(root));
        stage.show();
    }


    /**
     * Ritorna il messaggio da mostrare nella scena App.
     */
    public static String[] getAppMessage() {
        if (appMessage == null)
            return null;

        return new String[]{appMessage, appMessageColor};
    }


    public static void setAppAlert(String text) {
        appMessage = text;
        appMessageColor = "green";
    }


    public static void setAppWarning(String text) {
        appMessage = text;
        appMessageColor = "red";
    }
}
