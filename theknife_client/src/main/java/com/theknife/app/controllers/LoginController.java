package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller per la schermata di login.
 * Gestisce l'autenticazione dell'utente e la navigazione tra le scene.
 */
public class LoginController {
    /** Campo di testo per l'inserimento del nome utente. */
    @FXML
    private TextField username;
    /** Campo password per l'inserimento della password. */
    @FXML
    private PasswordField password;
    /** Etichetta per mostrare notifiche o messaggi di errore. */
    @FXML
    private Label notification_label;

    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    /**
     * Esegue il login dell'utente verificando le credenziali.
     * Se l'utente è un ristoratore, viene reindirizzato alla schermata "MyRestaurants".
     * Altrimenti, torna alla schermata principale con una conferma.
     * Mostra notifiche in caso di errore.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void login() throws IOException {
        switch(User.login(username.getText(), password.getText())) {
            case "ok":
                //if the user is a restaurateur, changes the scene to MyRestaurants
                if(User.getInfo()[2].equals("y")) {
                    SceneManager.changeScene("MyRestaurants");
                    break;
                }
                SceneManager.setAppAlert("Login effettuato con successo");
                SceneManager.changeScene("App");
                break;
            case "username":
                setNotification("Utente inesistente");
                break;
            case "password":
                setNotification("Password errata");
                break;
        }
    }

    /**
     * Mostra un messaggio di notifica nella schermata corrente.
     *
     * @param text il messaggio da visualizzare
     */
    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }
}