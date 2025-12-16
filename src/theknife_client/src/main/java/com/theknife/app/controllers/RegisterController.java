package com.theknife.app.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Pattern;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller della schermata di registrazione utente.
 *
 * <p>Consente di:</p>
 * <ul>
 *     <li>acquisire i dati utente tramite form</li>
 *     <li>validare la correttezza dei campi inseriti</li>
 *     <li>inviare la richiesta di registrazione al server</li>
 *     <li>gestire le risposte ricevute dal backend</li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per il fallback automatico
 * in caso di server non raggiungibile.</p>
 */

public class RegisterController implements OnlineChecker {

    @FXML private TextField name;
    @FXML private TextField surname;
    @FXML private TextField username;
    @FXML private TextField latitude;
    @FXML private TextField longitude;

    @FXML private PasswordField password;
    @FXML private PasswordField confirm_password;

    @FXML private DatePicker birth_date;

    @FXML private CheckBox is_restaurateur;

    @FXML private Label notification_label;

    /**
     * Costruttore di default del controller {@code RegisterController}.
     *
     * <p>Il costruttore non esegue inizializzazioni esplicite poiché
     * l'istanziazione del controller e l'iniezione dei campi {@code @FXML}
     * sono gestite automaticamente dal framework JavaFX tramite {@code FXMLLoader}.</p>
     */
    public RegisterController() {
        super();
    }

    /**
     * Inizializza la schermata configurando il calendario.
     *
     * <p>Imposta una {@link DateCell} personalizzata che:</p>
     * <ul>
     *     <li>disabilita date future rispetto alla data odierna</li>
     * </ul>
     */
    @FXML
    private void initialize() {
        birth_date.setDayCellFactory(d ->
                new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        setDisable(item.isAfter(LocalDate.now()));
                    }
                }
        );
    }

      /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se il caricamento della nuova schermata fallisce
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    /**
     * Effettua la registrazione utente.
     *
     * <p>Il flusso logico comprende:</p>
     * <ol>
     *     <li>Verifica dello stato del server tramite {@link #checkOnline()}</li>
     *     <li>Validazione password e conferma password</li>
     *     <li>Validazione dei requisiti minimi di sicurezza</li>
     *     <li>Invio dati al server con protocollo "register"</li>
     *     <li>Gestione e interpretazione della risposta</li>
     * </ol>
     *
     * <p>Possibili risposte dal server:</p>
     * <ul>
     *     <li>"ok" → registrazione avvenuta</li>
     *     <li>"missing" → campi mancanti</li>
     *     <li>"password" → password non valida</li>
     *     <li>"credentials" → username già esistente</li>
     *     <li>altro → errore generico</li>
     * </ul>
     *
     * @throws IOException se la riconnessione o il cambio scena generano problemi
     */
    @FXML
    private void register() throws IOException {
        if (!checkOnline()) return;

        ClientLogger.getInstance().info("Attempting registration for user: " + username.getText());

        String pwd = password.getText();
        String confirmPwd = confirm_password.getText();

        if (!pwd.equals(confirmPwd)) {
            ClientLogger.getInstance().warning("Registration failed: passwords do not match");
            setNotification("Le password inserite non corrispondono");
            return;
        }

        Pattern PASSWORD_PATTERN = Pattern.compile(
                "^(?=.{8,32}$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\p{Alnum}\\s]).*$"
        );

        if (!PASSWORD_PATTERN.matcher(pwd).matches()) {
            ClientLogger.getInstance().warning("Registration failed: password does not meet security requirements");
            setNotification("La password non rispetta i requisiti:\n"
                    + "- 8-32 caratteri\n"
                    + "- almeno una maiuscola\n"
                    + "- almeno una minuscola\n"
                    + "- almeno un numero\n"
                    + "- almeno un carattere speciale");
            return;
        }

        try {
            Communicator.send("register");
            Communicator.send(name.getText());
            Communicator.send(surname.getText());
            Communicator.send(username.getText());
            Communicator.send(pwd);
            Communicator.send(
                    birth_date.getValue() == null
                            ? "-"
                            : birth_date.getValue().toString()
            );
            Communicator.send(latitude.getText());
            Communicator.send(longitude.getText());
            Communicator.send(is_restaurateur.isSelected() ? "y" : "n");

            String response = Communicator.read();

            if (response == null) {
                ClientLogger.getInstance().error("Null response during registration");
                setNotification("Il server non ha risposto. Riprova più tardi.");
                fallback();
                return;
            }

            switch (response) {

                case "ok":
                    ClientLogger.getInstance().info("Registration successful for user: " + username.getText());
                    SceneManager.setAppAlert("Registrazione avvenuta con successo");
                    SceneManager.changeScene("App");
                    return;

                case "missing":
                    ClientLogger.getInstance().warning("Registration failed: missing required fields");
                    setNotification("Devi inserire tutti i campi obbligatori");
                    return;

                case "password":
                    ClientLogger.getInstance().warning("Registration failed: password security requirements not met");
                    setNotification("La password non rispetta i requisiti di sicurezza");
                    return;

                case "credentials":
                    ClientLogger.getInstance().warning("Registration failed: username already exists - " + username.getText());
                    setNotification("Username già esistente");
                    return;

                default:
                    ClientLogger.getInstance().error("Unexpected registration response: " + response);
                    setNotification("Errore imprevisto dal server: " + response);
            }

        } catch (IOException e) {
            ClientLogger.getInstance().error("Communication error during registration: " + e.getMessage());
            setNotification("Errore di comunicazione col server");
            fallback();
        }
    }

    /**
     * Mostra un messaggio di notifica nel pannello corrente.
     *
     * @param text messaggio da visualizzare
     */
    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }

    /**
     * Restituisce la lista dei nodi che devono essere abilitati/disabilitati
     * in caso di server offline.
     *
     * @return array di nodi interattivi del form di registrazione
     */
    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                name, surname, username,
                latitude, longitude,
                password, confirm_password,
                birth_date, is_restaurateur,
                notification_label
        };
    }
}
