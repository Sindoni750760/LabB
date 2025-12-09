package com.theknife.app.controllers;

import java.io.IOException;
import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 * Interfaccia che fornisce un sistema unificato per la gestione della connettività
 * verso il server da parte dei controller dell'applicazione.
 *
 * <p>Le responsabilità principali comprendono:</p>
 * <ul>
 *     <li>verifica dello stato di connessione verso il server</li>
 *     <li>abilitazione/disabilitazione della UI in caso di disconnessione</li>
 *     <li>fallback automatico con tentativo di riconnessione</li>
 *     <li>ritorno alla scena principale in caso di indisponibilità prolungata</li>
 * </ul>
 *
 * <p>Ogni controller che effettua comunicazioni col server deve implementare questa interfaccia.</p>
 */
public interface OnlineChecker {

     /**
     * Restituisce la lista di nodi dell'interfaccia utente che devono poter essere
     * abilitati/disabilitati in base allo stato del server.
     *
     * <p>Tipicamente contiene pulsanti, campi di input o sezioni cliccabili.</p>
     *
     * @return array di nodi UI da poter gestire dinamicamente
     */
    Node[] getInteractiveNodes();


    /**
     * Disabilita tutti i nodi restituiti da {@link #getInteractiveNodes()}.
     * <p>Utilizzato durante il fallback e quando il server risulta irraggiungibile.</p>
     */
    default void disableUI() {
        Node[] nodes = getInteractiveNodes();
        if (nodes != null) {
            for (Node n : nodes)
                if (n != null) n.setDisable(true);
        }
    }

    /**
     * Riabilita i nodi precedentemente disabilitati.
     * <p>Tipicamente invocato quando il server torna raggiungibile.</p>
     */
    default void enableUI() {
        Node[] nodes = getInteractiveNodes();
        if (nodes != null) {
            for (Node n : nodes)
                if (n != null) n.setDisable(false);
        }
    }


    /**
     * Verifica lo stato del server tramite {@link Communicator#isOnline()}.
     * <p>Se il server non risulta raggiungibile:</p>
     * <ul>
     *     <li>attiva il fallback</li>
     *     <li>restituisce {@code false}</li>
     * </ul>
     *
     * @return true se il server è online, false altrimenti
     */
    default boolean checkOnline() {
        if (Communicator.isOnline())
            return true;

        fallback();
        return false;
    }


    /**
     * Esegue la procedura di fallback centralizzata nel caso in cui il server risultasse offline.
     *
     * <p>Comportamento:</p>
     * <ul>
     *     <li>disabilita l'interfaccia utente</li>
     *     <li>mostra un messaggio di avviso globale tramite {@link SceneManager}</li>
     *     <li>attende un periodo predefinito (1 minuto)</li>
     *     <li>al termine esegue un nuovo controllo sulla connessione</li>
     *     <li>
     *          se il server è tornato online → riabilita la UI
     *          <br>altrimenti → ritorna automaticamente alla scena principale "App"
     *     </li>
     * </ul>
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
