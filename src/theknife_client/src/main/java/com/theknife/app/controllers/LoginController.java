package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller della schermata di login.
 * <p>Responsabilità principali:</p>
 * <ul>
 *     <li>Gestire il tentativo di autenticazione dell'utente</li>
 *     <li>Navigare all'interno dell'applicazione in base all'esito del login</li>
 *     <li>Mostrare notifiche e messaggi di errore</li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per gestire i casi in cui il server sia offline,
 * disabilitando la UI o mostrando fallback appropriati.</p>
 */
public class LoginController implements OnlineChecker {
    /** Campo di input per lo username inserito dall'utente. */
    @FXML
    private TextField username;
    /** Campo di input per la password, protetta tramite PasswordField. */
    @FXML
    private PasswordField password;
    /** Label UI dedicata alla visualizzazione di notifiche e messaggi di errore. */
    @FXML
    private Label notification_label;

    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se la nuova scena non viene caricata correttamente
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

     /**
     * Effettua il tentativo di login utilizzando i dati inseriti.
     *
     * <p>Flusso logico:</p>
     * <ol>
     *     <li>Verifica la connessione tramite {@link #checkOnline()}</li>
     *     <li>Invoca {@link User#login(String, String)}</li>
     *     <li>Gestisce le risposte previste dal protocollo</li>
     *     <li>Naviga alla schermata corretta in base al ruolo:
     *          <ul>
     *              <li>Utente normale → "App"</li>
     *              <li>Ristoratore → "MyRestaurants"</li>
     *          </ul>
     *     </li>
     * </ol>
     *
     * <p>Possibili risposte del server:</p>
     * <ul>
     *     <li>"ok" → login completato</li>
     *     <li>"username" → utente inesistente</li>
     *     <li>"password" → password errata</li>
     *     <li>altro → errore imprevisto o messaggio generico</li>
     * </ul>
     *
     * @throws IOException se la scena successiva non può essere caricata
     */
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

    /**
     * Mostra nella UI un messaggio testuale tramite la label dedicata.
     *
     * @param text testo del messaggio da mostrare
     */
    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }

    /**
     * Restituisce i nodi interattivi che devono essere gestiti da {@link OnlineChecker}.
     * <br>Vengono principalmente utilizzati per disattivare la UI in caso di server non raggiungibile.
     *
     * @return array contenente campi di input e label notifiche
     */
    @Override
    public Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                username, password, notification_label
        };
    }
}
