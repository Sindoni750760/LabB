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
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label notification_label;

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void login() throws IOException {

        String response;
        try {
            response = User.login(username.getText(), password.getText());
        } catch (IOException e) {
            setNotification("Errore di comunicazione col server");
            return;
        }

        switch (response) {
            case "ok":
                String[] info = User.getInfo();

                if (info != null && info[2].equals("y")) {
                    SceneManager.changeScene("MyRestaurants");
                    return;
                }

                SceneManager.setAppAlert("Login effettuato con successo");
                SceneManager.changeScene("App");
                return;

            case "username":
                setNotification("Utente inesistente");
                return;

            case "password":
                setNotification("Password errata");
                return;

            default:
                setNotification("Errore imprevisto: " + response);
        }
    }

    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }
}
