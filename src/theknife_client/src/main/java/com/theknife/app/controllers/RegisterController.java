package com.theknife.app.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Pattern;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller per la schermata di registrazione.
 * Gestisce l'inserimento dei dati utente, la validazione e la comunicazione con il server.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class RegisterController {
    /** Campo di testo per il nome dell'utente. */
    @FXML
    private TextField name, 
    /** Campo di testo per il cognome dell'utente. */
    surname, 
    /** Campo di testo per lo username scelto dall'utente. */
    username, 
    /** Campo di testo per la latitudine della posizione dell'utente. */
    latitude, 
    /** Campo di testo per la longitudine della posizione dell'utente. */
    longitude;
    /** Campo password per l'inserimento della password. */
    @FXML
    private PasswordField password, 
    /** Campo password per confermare la password inserita. */
    confirm_password;
    /** Selettore per la data di nascita dell'utente. */
    @FXML
    private DatePicker birth_date;
    /** Checkbox per indicare se l'utente è un ristoratore. */
    @FXML
    private CheckBox is_restaurateur;
    /** Etichetta per mostrare notifiche o messaggi di errore. */
    @FXML
    private Label notification_label;

    /**
     * Inizializza la schermata disabilitando la selezione di date future nel DatePicker.
     */
    @FXML
    private void initialize() {
        //to disable the days after today in the birth date selection
        birth_date.setDayCellFactory(d ->
           new DateCell() {
               @Override public void updateItem(LocalDate item, boolean empty) {
                   super.updateItem(item, empty);
                   setDisable(item.isAfter(LocalDate.now()));
               }});
    }

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
     * Esegue la registrazione dell'utente.
     * Valida i campi, invia i dati al server e gestisce la risposta.
     * Mostra notifiche in caso di errore o conferma in caso di successo.
     *
     * @throws IOException se si verifica un errore nella comunicazione o nel cambio scena
     */
    @FXML
    private void register() throws IOException {
        //checks if the two passwords inserted correspond
        String pwd = password.getText();
        String confirmPwd=confirm_password.getText();
        if(!pwd.equals(confirmPwd)){
            setNotification("Le password inserite non corrispondono");
            return;
        }
        Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.{8,32}$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\W).*$"
        );

         try{
                Communicator.sendStream("register");
                Communicator.sendStream(name.getText());
                Communicator.sendStream(surname.getText());
                Communicator.sendStream(username.getText());
                Communicator.sendStream(pwd);
                Communicator.sendStream(
                birth_date.getValue() == null
                    ? "-"
                    : birth_date.getValue().toString()
                );
                Communicator.sendStream(latitude.getText());
                Communicator.sendStream(longitude.getText());
                Communicator.sendStream(is_restaurateur.isSelected() ? "y" : "n");
                switch(Communicator.readStream()){
                    case "ok":
                    SceneManager.setAppAlert("Registrazione avvenuta con successo");
                    SceneManager.changeScene("App");
                    break;
                case "missing":
                    setNotification("Devi inserire tutti i campi obbligatori");
                    break;
                case "password":
                        if(!PASSWORD_PATTERN.matcher(pwd).matches()){
                            setNotification( "La password non rispetta i requisiti. Verificare che abbia \n" +
                            "almeno 8 caratteri | " +
                            "almeno una maiuscola | " +
                            "almeno una minuscola | " +
                            "almeno un numero \n" +
                            "| almeno un carattere speciale");
                        return;
                    }
                    break;
                case "credentials":
                    setNotification("Username già esistente");
                    break;
                default:
                    setNotification("Errore imprevisto dal server");
                }

            }catch(IOException e){
            setNotification("Errore di comunicazione: "+e.getMessage());
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