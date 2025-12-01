package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller della schermata principale "App".
 * Gestisce:
 * - stato di connessione
 * - messaggi globali (alert/warning)
 * - pulsanti di navigazione (login, registrazione, ristoranti, preferiti, ecc.)
 *
 * Implementa OnlineChecker per poter disabilitare la UI in caso di server offline.
 */
public class AppController implements OnlineChecker {

    private static int reconnection_tries;

    @FXML private Label user_info_label;
    @FXML private Label notification_label;

    @FXML private Button register_btn;
    @FXML private Button login_btn;
    @FXML private Button logout_btn;
    @FXML private Button view_btn;
    @FXML private Button reconnect_btn;
    @FXML private Button fav_button;

    @FXML
    private void initialize() {
        reconnection_tries = 1;
        ClientLogger.getInstance().info("AppController initialized");

        String[] app_message = SceneManager.getAppMessage();
        if (app_message != null) {
            notification_label.setVisible(true);
            notification_label.setText(app_message[0]);
            notification_label.setStyle("-fx-text-fill: " + app_message[1]);
        }

        if (Communicator.isOnline()) {
            handleButtonsForDisconnection(false);

            String[] user_info = User.getInfo();
            if (user_info != null) {
                user_info_label.setText("Login effettuato come " + user_info[0] + " " + user_info[1]);
                register_btn.setText("Vedi recensioni");
                register_btn.setLayoutX(252);
                login_btn.setVisible(false);
                logout_btn.setVisible(true);

                fav_button.setVisible(true);
                fav_button.setDisable(false);
            } else {
                user_info_label.setText("Utente ospite");
                fav_button.setVisible(false);
                fav_button.setDisable(true);
            }

        } else {
            handleButtonsForDisconnection(true);
        }
    }

    @FXML
    private void click_view_restaurants() throws IOException {
        SceneManager.changeScene("ViewRestaurants");
    }

    @FXML
    private void click_register() throws IOException {
        if (User.getInfo() == null)
            SceneManager.changeScene("Register");
        else
            SceneManager.changeScene("MyReviews");
    }

    @FXML
    private void click_login() throws IOException {
        SceneManager.changeScene("Login");
    }

    @FXML
    private void click_favorites() throws IOException {
        SceneManager.setPreviousNavigation("Favorites");
        SceneManager.changeScene("Favorites");
    }

    @FXML
    private void logout() throws Exception {
        User.logout();
        SceneManager.changeScene("App");
    }

    private void handleButtonsForDisconnection(boolean disable) {
        register_btn.setDisable(disable);
        login_btn.setDisable(disable);
        logout_btn.setDisable(disable);
        view_btn.setDisable(disable);
        reconnect_btn.setVisible(disable);

        if (disable || User.getInfo() == null) {
            fav_button.setVisible(false);
            fav_button.setDisable(true);
        }
    }

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

    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                register_btn, login_btn, logout_btn, view_btn, reconnect_btn, fav_button
        };
    }
}
