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
 * Controller per la schermata di registrazione.
 * Gestisce l'inserimento dei dati utente, la validazione e la comunicazione con il server.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
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

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void register() throws IOException {
        if (!checkOnline()) {
            return;
        }

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

    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }

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
