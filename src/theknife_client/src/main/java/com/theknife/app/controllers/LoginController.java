package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller per la schermata di login.
 * Gestisce l'autenticazione dell'utente e la navigazione tra le scene.
 *
 * Implementa OnlineChecker per gestire il fallback in caso di server offline.
 *
 * @author ...
 */
public class LoginController implements OnlineChecker {

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
        if (!checkOnline()) return;
        

        ClientLogger.getInstance().info("Attempting login for user: " + username.getText());

        String response;
        try {
            response = User.login(username.getText(), password.getText());
        } catch (IOException e) {
            ClientLogger.getInstance().error("Communication error during login: " + e.getMessage());
            setNotification("Errore di comunicazione col server");
            fallback();
            return;
        }

        if (response == null) {
            fallback();
            return;
        }

        switch (response) {
            case "ok":
                ClientLogger.getInstance().info("Login successful for user: " + username.getText());
                String[] info = User.getInfo();

                if (info != null && info[2].equals("y")) {
                    SceneManager.changeScene("MyRestaurants");
                    return;
                }

                SceneManager.setAppAlert("Login effettuato con successo");
                SceneManager.changeScene("App");
                return;

            case "username":
                ClientLogger.getInstance().warning("Login failed: user does not exist - " + username.getText());
                setNotification("Utente inesistente");
                return;

            case "password":
                ClientLogger.getInstance().warning("Login failed: incorrect password for user - " + username.getText());
                setNotification("Password errata");
                return;

            default:
                ClientLogger.getInstance().error("Unexpected login response: " + response);
                setNotification("Errore imprevisto: " + response);
        }
    }

    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }

    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                username, password, notification_label
        };
    }
}
