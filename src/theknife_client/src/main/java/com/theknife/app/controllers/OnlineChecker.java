package com.theknife.app.controllers;

import java.io.IOException;
import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 * Interfaccia che fornisce un sistema unificato di:
 * - verifica connessione al server
 * - disabilitazione UI in fallback
 * - tentativo automatico di riconnessione
 * - ritorno alla scena "App" se il server rimane offline
 *
 * MEMO: Va implementata da tutti i controller che comunicano col server.
 */
public interface OnlineChecker {

    /**
     * Fornisce la lista dei nodi UI (pulsanti, campi input, ecc.)
     * che devono essere disabilitati quando il server è offline.
     */
    Node[] getInteractiveNodes();


    /**
     * Disabilita ogni nodo definito da getInteractiveNodes().
     */
    default void disableUI() {
        Node[] nodes = getInteractiveNodes();
        if (nodes != null) {
            for (Node n : nodes)
                if (n != null) n.setDisable(true);
        }
    }

    /**
     * Riabilita i nodi disabilitati.
     */
    default void enableUI() {
        Node[] nodes = getInteractiveNodes();
        if (nodes != null) {
            for (Node n : nodes)
                if (n != null) n.setDisable(false);
        }
    }


    /**
     * Controllo unificato dello stato del server.
     * Se offline → fallback() e ritorna false
     */
    default boolean checkOnline() {
        if (Communicator.isOnline())
            return true;

        fallback();
        return false;
    }


    /**
     * Fallback unificato:
     * - mostra warning
     * - disabilita la UI
     * - dopo 5 secondi ricontrolla
     * - se rimane offline → torna alla scena App
     */
    default void fallback() {
        ClientLogger.getInstance().warning("Server offline – fallback engaged");

        disableUI();

        Platform.runLater(() ->
            SceneManager.setAppWarning("Server irraggiungibile, riconnessione...")
        );

        PauseTransition wait = new PauseTransition(javafx.util.Duration.minutes(1));;
        
        wait.setOnFinished(e -> {
            if (Communicator.isOnline()) {
                ClientLogger.getInstance().info("Server reconnected – re-enabling UI");
                enableUI();
            } else {
                try {
                    ClientLogger.getInstance().error("Server still offline – returning to App");
                    SceneManager.setAppWarning("Il server non è raggiungibile");
                    SceneManager.changeScene("App");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        wait.play();
    }
}
