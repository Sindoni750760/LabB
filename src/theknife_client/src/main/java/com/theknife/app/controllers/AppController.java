package com.theknife.app.controllers;

import java.io.IOException;
import java.net.UnknownHostException;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller principale dell'applicazione.
 * Gestisce l'interfaccia utente e le interazioni con i pulsanti della schermata principale
 */
public class AppController {
    /**
     * Numero di tentativi di riconnessione effettuati
     */
    private static int reconnection_tries;
    /** Etichetta che mostra le informazioni dell'utente loggato. */
    @FXML
    private Label user_info_label, 
    /** Etichetta per notifiche generali dell'app. */
    notification_label;
    /** Pulsante per la registrazione o visualizzazione delle recensioni. */
    @FXML
    private Button register_btn, 
    /** Pulsante per accedere all'account utente. */
    login_btn, 
    /** Pulsante per effettuare il logout. */
    logout_btn, 
    /** Pulsante per visualizzare i ristoranti. */
    view_btn, 
    /** Pulsante per tentare la riconnessione in caso di disconnessione. */
    reconnect_btn;

    /**
     * Inizializza il controller dopo il caricamento dell'interfaccia FXML.
     * Imposta i messaggi dell'app e aggiorna l'interfaccia in base allo stato di connessione.
     *
     * @throws IOException se si verifica un errore durante il caricamento delle risorse
     */
    @FXML
    private void initialize() throws IOException {
        reconnection_tries = 1;

        //loads the App message
        String[] app_message = SceneManager.getAppMessage();
        if(app_message != null) {
            notification_label.setVisible(true);
            notification_label.setText(app_message[0]);
            notification_label.setStyle("-fx-text-fill: " + app_message[1]);
        }

        if(Communicator.isOnline()) {
            //loads logged in user data
            String[] user_info = User.getInfo();
            if(user_info != null) {
                user_info_label.setText("Login effettuato come " + user_info[0] + " " + user_info[1]);
                //shows/hides button if the user is logged in
                register_btn.setText("Vedi recensioni");
                register_btn.setLayoutX(252);
                login_btn.setVisible(false);
                logout_btn.setVisible(true);
            }
        } else
            handleButtonsForDisconnection(true);
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
        //if the user is logged in, go to MyReviews page, else go to the registration page
        if(User.getInfo() == null)
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
     * @throws UnknownHostException se l'host non è raggiungibile
     * @throws IOException se si verifica un errore di I/O durante la connessione
     */
    @FXML
    private void reconnect() throws UnknownHostException, IOException {
        //used to handle the reconnection messages
        if(Communicator.connect()) {
            reconnection_tries = 1;
            notification_label.setVisible(true);
            notification_label.setText("Riconnessione riuscita");
            notification_label.setStyle("-fx-text-fill: green");

            handleButtonsForDisconnection(false);
        } else {
            notification_label.setVisible(true);
            notification_label.setText("Errore nella riconnessione, tentativo numero " + reconnection_tries++);
            notification_label.setStyle("-fx-text-fill: red");
        }
    }
}