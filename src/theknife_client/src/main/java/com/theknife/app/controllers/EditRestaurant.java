package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller per la schermata di modifica o aggiunta di un ristorante.
 * Gestisce il caricamento dei dati, l'aggiornamento, l'eliminazione e la validazione dei campi.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */

public class EditRestaurant {

    private int editing_id;

    @FXML private Button edit_btn;
    @FXML private Button delete_btn;
    @FXML private TextField name_field, nation_field, city_field, address_field;
    @FXML private TextField latitude_field, longitude_field, price_field;
    @FXML private TextArea categories_textarea;
    @FXML private CheckBox delivery_check, online_check;
    @FXML private Label notification_label;

    @FXML
    private void initialize() {

        editing_id = EditingRestaurant.getId();

        if (editing_id > 0) {
            String[] info = EditingRestaurant.getInfo();

            name_field.setText(info[0]);
            nation_field.setText(info[1]);
            city_field.setText(info[2]);
            address_field.setText(info[3]);
            latitude_field.setText(info[4]);
            longitude_field.setText(info[5]);
            price_field.setText(info[6]);
            delivery_check.setSelected(info[7].equals("y"));
            online_check.setSelected(info[8].equals("y"));
            categories_textarea.setText(info[11]);
        } else {
            edit_btn.setText("Aggiungi ristorante");
            delete_btn.setVisible(false);
        }
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("MyRestaurants");
    }

    @FXML
    private void updateRestaurant() throws IOException {

        String name = name_field.getText();
        String nation = nation_field.getText();
        String city = city_field.getText();
        String address = address_field.getText();
        String latitude = latitude_field.getText();
        String longitude = longitude_field.getText();
        String price = price_field.getText();
        String categories = categories_textarea.getText();
        boolean delivery = delivery_check.isSelected();
        boolean online = online_check.isSelected();

        String response;

        if (editing_id > 0) {
            response = EditingRestaurant.editRestaurant(
                    editing_id, name, nation, city, address,
                    latitude, longitude, price, categories,
                    delivery, online
            );
        } else {
            response = EditingRestaurant.addRestaurant(
                    name, nation, city, address,
                    latitude, longitude, price, categories,
                    delivery, online
            );
        }

        switch (response) {
            case "ok":
                SceneManager.changeScene("MyRestaurants");
                break;

            case "missing":
                setNotification("Inserisci tutti i campi");
                break;

            case "coordinates":
                setNotification("Coordinate non valide");
                break;

            case "price_format":
                setNotification("Prezzo non valido");
                break;

            case "price_negative":
                setNotification("Il prezzo deve essere positivo");
                break;

            default:
                setNotification("Errore dal server: " + response);
        }
    }

    @FXML
    private void checkTextBox() {
        String text = categories_textarea.getText();
        if (text.length() > 255)
            categories_textarea.setText(text.substring(0, 255));
    }


    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }


    @FXML
    private void deleteRestaurant() throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare questo ristorante?",
                ButtonType.YES, ButtonType.NO
        );
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES)
            return;

        Communicator.send("deleteRestaurant");
        Communicator.send(Integer.toString(editing_id));

        String response = Communicator.read();

        if (response != null && response.equals("ok"))
            SceneManager.changeScene("MyRestaurants");
        else
            setNotification("Errore durante l'eliminazione");
    }
}
