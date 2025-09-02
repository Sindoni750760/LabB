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
    /** ID del ristorante attualmente in fase di modifica. */
    private int editing_id;
    /** Pulsante per confermare la modifica o aggiunta del ristorante. */
    @FXML
    private Button edit_btn, 
    /** Pulsante per eliminare il ristorante selezionato. */
    delete_btn;
    /** Campo di testo per il nome del ristorante. */
    @FXML
    private TextField name_field, 
    /** Campo di testo per la nazione del ristorante. */
    nation_field, 
    /** Campo di testo per la città del ristorante. */
    city_field, 
    /** Campo di testo per l'indirizzo del ristorante. */
    address_field, 
    /** Campo di testo per la latitudine del ristorante. */
    latitude_field, 
    /** Campo di testo per la longitudine del ristorante. */
    longitude_field, 
    /** Campo di testo per il prezzo medio del ristorante. */
    price_field;
    /** Area di testo per le categorie associate al ristorante. */
    @FXML
    private TextArea categories_textarea;
    /** Checkbox per indicare se il ristorante offre consegna a domicilio. */
    @FXML
    private CheckBox delivery_check,
    /** Checkbox per indicare se il ristorante è disponibile online. */
    online_check;
    /** Etichetta per mostrare notifiche o messaggi di errore all'utente. */
    @FXML
    private Label notification_label;
    /**
     * Inizializza la schermata caricando i dati del ristorante da modificare,
     * oppure imposta la modalità di aggiunta se non è presente un ID.
     */
    @FXML
    private void initialize() {
        //gets the id of the restaurant being edited
        editing_id = EditingRestaurant.getId();

        //if it's editing a restaurant, sets the info
        if(editing_id > 0) {
            String[] restaurant_info = EditingRestaurant.getInfo();
            name_field.setText(restaurant_info[0]);
            nation_field.setText(restaurant_info[1]);
            city_field.setText(restaurant_info[2]);
            address_field.setText(restaurant_info[3]);
            latitude_field.setText(restaurant_info[4]);
            longitude_field.setText(restaurant_info[5]);
            price_field.setText(restaurant_info[6]);
            delivery_check.setSelected(restaurant_info[7].equals("y"));
            online_check.setSelected(restaurant_info[8].equals("y"));
            categories_textarea.setText(restaurant_info[11]);
        } else { //if not editing, changes buttons displays
            edit_btn.setText("Aggiungi ristorante");
            delete_btn.setVisible(false);
        }
    }

    /**
     * Torna alla schermata "MyRestaurants" senza salvare modifiche.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("MyRestaurants");
    }

    /**
     * Aggiorna o aggiunge un ristorante in base alla modalità corrente.
     * Valida i campi e mostra notifiche in caso di errore.
     *
     * @throws IOException se si verifica un errore durante il cambio scena
     */
    @FXML
    private void updateRestaurant() throws IOException {
        //loads the values from the fields
        String name = name_field.getText(),
        nation = nation_field.getText(),
        city = city_field.getText(),
        address = address_field.getText(),
        latitude = latitude_field.getText(),
        longitude = longitude_field.getText(),
        price = price_field.getText(),
        categories = categories_textarea.getText();
        boolean has_delivery = delivery_check.isSelected(), has_online = online_check.isSelected();

        String response;
        //editing a restaurant
        if(editing_id > 0)
            response = EditingRestaurant.editRestaurant(editing_id, name, nation, city, address, latitude, longitude, price, categories, has_delivery, has_online);
        else //not editing a restaurant
            response = EditingRestaurant.addRestaurant(name, nation, city, address, latitude, longitude, price, categories, has_delivery, has_online);
        
        switch(response) {
            case "ok":
                SceneManager.changeScene("MyRestaurants");
                break;
            case "missing": //some information is missing
                setNotification("Inserisci tutti i campi");
                break;
            case "coordinates": //wrong coordinates format
                setNotification("Le coordinate inserite non sono nel formato corretto");
                break;
            case "price_format": //wrong price format
                setNotification("Il prezzo medio inserito non è nel formato corretto");
                break;
            case "price_negative": //price is negative
                setNotification("Il prezzo deve essere positivo");
                break;
        }
    }

    /**
     * Controlla la lunghezza del testo nelle categorie e lo tronca se supera i 255 caratteri.
     */
    @FXML
    private void checkTextBox() {
        String text = categories_textarea.getText();
        //truncates the text in the textbox if it exceedes the max length
        if(text.length() > 255)
            categories_textarea.setText(text.substring(0, 255));
    }

    /**
     * Imposta un messaggio di notifica visibile nella schermata corrente.
     *
     * @param msg il messaggio da visualizzare
     */
    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    /**
     * Elimina il ristorante corrente dopo conferma da parte dell'utente.
     * Mostra una notifica in caso di errore.
     *
     * @throws IOException se si verifica un errore durante la comunicazione o il cambio scena
     */
    @FXML
    private void deleteRestaurant() throws IOException {
        //prompts the user if he is sure to delete the current restaurant
        Alert alert = new Alert(AlertType.CONFIRMATION, "Sei sicuro di voler eliminare questo ristorante?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            Communicator.sendStream("deleteRestaurant");
            Communicator.sendStream(Integer.toString(editing_id));

            if(Communicator.readStream().equals("ok"))
                SceneManager.changeScene("MyRestaurants");
            else
                setNotification("Errore nell'eliminazione del ristorante");
        }
    }
}