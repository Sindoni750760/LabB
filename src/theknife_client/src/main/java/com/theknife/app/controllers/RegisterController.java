package com.theknife.app.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Pattern;

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
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class RegisterController {

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

        // controllo password lato client
        String pwd = password.getText();
        String confirmPwd = confirm_password.getText();

        if (!pwd.equals(confirmPwd)) {
            setNotification("Le password inserite non corrispondono");
            return;
        }

        Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.{8,32}$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\p{Alnum}\\s]).*$"
        );

        if (!PASSWORD_PATTERN.matcher(pwd).matches()) {
            setNotification("La password non rispetta i requisiti:\n"
                    + "- 8-32 caratteri\n"
                    + "- almeno una maiuscola\n"
                    + "- almeno una minuscola\n"
                    + "- almeno un numero\n"
                    + "- almeno un carattere speciale");
            return;
        }

        try {

            // invia comando al server
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

            switch (response) {

                case "ok":
                    SceneManager.setAppAlert("Registrazione avvenuta con successo");
                    SceneManager.changeScene("App");
                    return;

                case "missing":
                    setNotification("Devi inserire tutti i campi obbligatori");
                    return;

                case "password":
                    setNotification("La password non rispetta i requisiti di sicurezza");
                    return;

                case "credentials":
                    setNotification("Username già esistente");
                    return;

                default:
                    setNotification("Errore imprevisto dal server: " + response);
            }

        } catch (IOException e) {
            setNotification("Errore di comunicazione col server");
        }
    }


    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }
}
