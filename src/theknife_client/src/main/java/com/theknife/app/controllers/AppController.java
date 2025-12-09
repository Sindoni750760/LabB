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
 * <ul>
 * <li> stato di connessione
 * <li> messaggi globali (alert/warning)
 * <li> pulsanti di navigazione (login, registrazione, ristoranti, preferiti, ecc.)
 * </ul>
 * Implementa {@link OnlineChecker} per poter disabilitare la UI in caso di server offline.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class AppController implements OnlineChecker {
    /**Contatore statico del numero di tentativi effettuati per riconnettersi al server*/
    private static int reconnection_tries;

    @FXML private Label user_info_label;
    @FXML private Label notification_label;

    @FXML private Button register_btn;
    @FXML private Button login_btn;
    @FXML private Button logout_btn;
    @FXML private Button view_btn;
    @FXML private Button reconnect_btn;
    @FXML private Button fav_button;
    
    /**
     * Metodo di inizializzazione dell'interfaccia.
     * <br>Viene invocato automaticamente da JavaFX al caricamento della view
     * 
     * Responsabilità:
     * <ul>
     *      <li> Impostare label e pulsanti in base allo stato online\offline</li>
     *      <li> Gestire eventuali notifiche da {@link SceneManager#getAppMessage()}</li>
     *      <li> Mostrare o nascondere pulsanti relativi all'utente loggato
     * </ul>
     */
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

    /**
     * Gestisce il click verso la sezione "vedi ristoranti"
     * @throws IOException se la scena non riesce ad essere caricata
     */
    @FXML
    private void click_view_restaurants() throws IOException {
        SceneManager.changeScene("ViewRestaurants");
    }

    /**
     * Gestisce il click sul pulsante di registrazione.
     * <ul>
     *      <li> Se l'utente non è loggato, passa alla scena "Register" </li>
     *      <li> Se l'utente è loggato, passa alla scena "MyReviews" </li>
     * </ul>
     * @throws IOException
     */
    @FXML
    private void click_register() throws IOException {
        if (User.getInfo() == null)
            SceneManager.changeScene("Register");
        else
            SceneManager.changeScene("MyReviews");
    }

    /**
     * Gestisce il click sul pulsante di login
     * 
     * @throws IOException se la scena non viene caricata correttamente
     */
    @FXML
    private void click_login() throws IOException {
        SceneManager.changeScene("Login");
    }

     /**
     * Gestisce l'accesso alla schermata dei preferiti.
     * Imposta inoltre la navigazione precedente per SceneManager.
     *
     * @throws IOException se la scena non viene caricata correttamente
     */
    @FXML
    private void click_favorites() throws IOException {
        SceneManager.setPreviousNavigation("Favorites");
        SceneManager.changeScene("Favorites");
    }

    /**
     * Effettua il logout dell'utente corrente.
     *
     * @throws Exception se il logout non va a buon fine
     */
    @FXML
    private void logout() throws Exception {
        User.logout();
        SceneManager.changeScene("App");
    }

    /**
     * Abilita o disabilita i pulsanti sensibili alla connessione al server.
     * Viene usato quando il server risulta offline o dopo la riconnessione.
     *
     * @param disable {@true} → la UI viene disabilitata, {@false} → la UI viene riattivata
     */
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

    /**
     * Tenta di ristabilire la connessione con il server.
     * <br>Se riesce:
     * <ul>
     *     <li>Resetta il contatore dei tentativi</li>
     *     <li>Mostra notifica di successo</li>
     *     <li>Riattiva la UI</li>
     * </ul>
     *
     * Se fallisce:
     * <ul>
     *     <li>Incrementa il contatore</li>
     *     <li>Mostra messaggio di errore</li>
     * </ul>
     *
     * @throws IOException se il tentativo di connessione genera errori I/O
     */
    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                register_btn, login_btn, logout_btn, view_btn, reconnect_btn, fav_button
        };
    }
}
