package com.theknife.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe utility per la gestione delle scene JavaFX.
 * Gestisce anche messaggi globali da mostrare nella scena "App".
 */
public class SceneManager {

    private static Stage stage;

    private static String appMessage = null;
    private static String appMessageColor = null;

    // Contesto di navigazione per il "torna indietro"
    private static String previousNavigation = null;

    public static void init(Stage s) throws java.io.IOException {
        stage = s;
        changeScene("App");
    }

    public static void changeScene(String sceneName) throws java.io.IOException {
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

    public static void setPreviousNavigation(String ctx) {
        previousNavigation = ctx;
    }

    public static String getPreviousNavigation() {
        return previousNavigation;
    }
}
