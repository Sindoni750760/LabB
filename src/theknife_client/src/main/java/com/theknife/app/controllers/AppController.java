package com.theknife.app.controllers;

import java.io.IOException;
import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
/**
 * Controller della schermata principale "App".
 * Gestisce:
 * - stato di connessione
 * - messaggi globali (alert/warning)
 * - pulsanti di navigazione (login, registrazione, ristoranti, ecc.)
 *
 * Implementa OnlineChecker per poter disabilitare la UI in caso di server offline
 * e sfruttare il fallback unificato.
 *
 * @author ...
 */

public class AppController implements OnlineChecker {

    /**
     * Numero di tentativi di riconnessione effettuati
     */
    private static int reconnection_tries;

    /** Etichetta che mostra le informazioni dell'utente loggato. */
    @FXML
    private Label user_info_label;

    /** Etichetta per notifiche generali dell'app. */
    @FXML
    private Label notification_label;

    /** Pulsante per la registrazione o visualizzazione delle recensioni. */
    @FXML
    private Button register_btn;

    /** Pulsante per accedere all'account utente. */
    @FXML
    private Button login_btn;

    /** Pulsante per effettuare il logout. */
    @FXML
    private Button logout_btn;

    /** Pulsante per visualizzare i ristoranti. */
    @FXML
    private Button view_btn;

    /** Pulsante per tentare la riconnessione in caso di disconnessione. */
    @FXML
    private Button reconnect_btn;

    /**
     * Inizializza il controller dopo il caricamento dell'interfaccia FXML.
     * Imposta i messaggi dell'app e aggiorna l'interfaccia in base allo stato di connessione.
     *
     * @throws IOException se si verifica un errore durante il caricamento delle risorse
     */
    @FXML
    private void initialize() throws IOException {
        reconnection_tries = 1;
        ClientLogger.getInstance().info("AppController initialized");

        // carica eventuale messaggio globale
        String[] app_message = SceneManager.getAppMessage();
        if (app_message != null) {
            notification_label.setVisible(true);
            notification_label.setText(app_message[0]);
            notification_label.setStyle("-fx-text-fill: " + app_message[1]);
        }

        if (Communicator.isOnline()) {
            handleButtonsForDisconnection(false);

            // carica eventuale utente loggato
            String[] user_info = User.getInfo();
            if (user_info != null) {
                user_info_label.setText("Login effettuato come " + user_info[0] + " " + user_info[1]);
                register_btn.setText("Vedi recensioni");
                register_btn.setLayoutX(252);
                login_btn.setVisible(false);
                logout_btn.setVisible(true);
            }
        } else {
            handleButtonsForDisconnection(true);
        }
    }

    /**
     * Gestisce il click sul pulsante per visualizzare i ristoranti.
     * Cambia scena verso la schermata di visualizzazione.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void click_view_restaurants() throws IOException {
        SceneManager.changeScene("ViewRestaurants");
    }

    /**
     * Gestisce il click sul pulsante di registrazione.
     * Se l'utente è loggato, apre la schermata delle recensioni personali;
     * altrimenti apre la schermata di registrazione.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void click_register() throws IOException {
        if (User.getInfo() == null)
            SceneManager.changeScene("Register");
        else
            SceneManager.changeScene("MyReviews");
    }

    /**
     * Gestisce il click sul pulsante di login.
     * Cambia scena verso la schermata di accesso.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void click_login() throws IOException {
        SceneManager.changeScene("Login");
    }

    /**
     * Effettua il logout dell'utente e ritorna alla schermata principale.
     *
     * @throws Exception se si verifica un errore durante il logout
     */
    @FXML
    private void logout() throws Exception {
        // Il logout può comunque essere gestito anche se il server non risponde,
        // ma se vuoi puoi aggiungere: if (!checkOnline()) return;
        User.logout();
        SceneManager.changeScene("App");
    }

    /**
     * Abilita o disabilita i pulsanti dell'interfaccia in base allo stato di connessione.
     *
     * @param disable true per disabilitare i pulsanti, false per abilitarli
     */
    private void handleButtonsForDisconnection(boolean disable) {
        register_btn.setDisable(disable);
        login_btn.setDisable(disable);
        logout_btn.setDisable(disable);
        view_btn.setDisable(disable);
        reconnect_btn.setVisible(disable);
    }

    /**
     * Tenta di riconnettersi al server.
     * Mostra un messaggio di successo o errore e aggiorna l'interfaccia di conseguenza.
     *
     * @throws IOException se si verifica un errore di I/O durante la connessione
     */
    @FXML
    private void reconnect() throws IOException {
        if (Communicator.connect()) {
            reconnection_tries = 1;
            ClientLogger.getInstance().info("Successfully reconnected to server");
            notification_label.setVisible(true);
            notification_label.setText("Riconnessione riuscita");
            notification_label.setStyle("-fx-text-fill: green");
            handleButtonsForDisconnection(false);
        } else {
            ClientLogger.getInstance().warning("Reconnection failed, attempt number " + reconnection_tries);
            notification_label.setVisible(true);
            notification_label.setText(
                    "Errore nella riconnessione, tentativo numero " + reconnection_tries++
            );
            notification_label.setStyle("-fx-text-fill: red");
        }
    }

    /**
     * Implementazione di OnlineChecker: nodi interattivi da disabilitare in fallback.
     */
    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                register_btn, login_btn, logout_btn, view_btn, reconnect_btn
        };
    }
}
