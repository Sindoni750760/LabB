package com.theknife.app.controllers;

import java.io.IOException;
import java.time.LocalDate;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    private TextField name, surname, username, latitude, longitude;
    @FXML
    private PasswordField password, confirm_password;
    @FXML
    private DatePicker birth_date;
    @FXML
    private CheckBox is_restaurateur;
    @FXML
    private Label notification_label;

    @FXML
    private void initialize() {
        birth_date.setDayCellFactory(d ->
           new DateCell() {
               @Override public void updateItem(LocalDate item, boolean empty) {
                   super.updateItem(item, empty);
                   setDisable(item.isAfter(LocalDate.now()));
               }});
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void register() throws IOException {
        if(!password.getText().equals(confirm_password.getText())) {
            setNotification("Le password inserite non corrispondono");
            return;
        }

        Communicator.sendStream("register");
        Communicator.sendStream(name.getText());
        Communicator.sendStream(surname.getText());
        Communicator.sendStream(username.getText());
        Communicator.sendStream(password.getText());
        Communicator.sendStream(birth_date.getValue() == null ? "-" : birth_date.getValue().toString());
        Communicator.sendStream(latitude.getText());
        Communicator.sendStream(longitude.getText());
        Communicator.sendStream(is_restaurateur.isSelected() ? "y" : "n");

        switch(Communicator.readStream()) {
            case "ok":
                SceneManager.setAppAlert("Registrazione avvenuta con successo");
                SceneManager.changeScene("App");
                break;
            case "missing":
                setNotification("Devi inserire tutti i campi obbligatori");
                break;
            case "password":
                setNotification("La password inserita non rispetta i requisiti");
                break;
            case "date":
                //shouldn't happen with this client
                setNotification("Errore nel client");
                break;
            case "coordinates":
                setNotification("Le coordinate inserite non sono nel formato corretto");
                break;
            case "username":
                setNotification("Esiste già un utente con questo username");
                break;
            default:
                setNotification("Errore imprevisto da parte del server");
                break;
        }
    }

    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }
}