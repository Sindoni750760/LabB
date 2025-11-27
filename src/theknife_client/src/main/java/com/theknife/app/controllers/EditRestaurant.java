package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
/**
 * Controller per la schermata di modifica o aggiunta di un ristorante.
 * Gestisce il caricamento dei dati, l'aggiornamento, l'eliminazione e la validazione dei campi.
 *
 * Implementa OnlineChecker per gestire il fallback unificato in caso di server offline.
 *
 * @author ...
 */
public class EditRestaurant implements OnlineChecker {

    /** ID del ristorante in modifica, o 0 se si sta aggiungendo un nuovo ristorante. */
    private int editing_id;

    @FXML private Button edit_btn;
    @FXML private Button delete_btn;
    @FXML private TextField name_field, nation_field, city_field, address_field;
    @FXML private TextField latitude_field, longitude_field, price_field;
    @FXML private TextArea categories_textarea;
    @FXML private CheckBox delivery_check, online_check;
    @FXML private Label notification_label;

    /**
     * Inizializza i campi del form caricando i dati del ristorante se già esistente.
     */
    @FXML
    private void initialize() {
        ClientLogger.getInstance().info("EditRestaurant initialized, editing_id: " + EditingRestaurant.getId());

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

    /**
     * Torna alla schermata "MyRestaurants".
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("MyRestaurants");
    }

    /**
     * Aggiorna o aggiunge un ristorante in base al valore di editing_id.
     * Valida i campi e gestisce le risposte del server.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void updateRestaurant() throws IOException {
        if (!checkOnline()) {
            return;
        }

        ClientLogger.getInstance().info("Updating restaurant, editing_id: " + editing_id);

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

        if (response == null) {
            // server caduto durante l'operazione
            fallback();
            return;
        }

        switch (response) {
            case "ok":
                ClientLogger.getInstance().info("Restaurant update successful");
                SceneManager.changeScene("MyRestaurants");
                break;

            case "missing":
                ClientLogger.getInstance().warning("Restaurant update failed: missing required fields");
                setNotification("Inserisci tutti i campi");
                break;

            case "coordinates":
                ClientLogger.getInstance().warning("Restaurant update failed: invalid coordinates");
                setNotification("Coordinate non valide");
                break;

            case "price_format":
                ClientLogger.getInstance().warning("Restaurant update failed: invalid price format");
                setNotification("Prezzo non valido");
                break;

            case "price_negative":
                ClientLogger.getInstance().warning("Restaurant update failed: negative price");
                setNotification("Il prezzo deve essere positivo");
                break;

            default:
                ClientLogger.getInstance().error("Unexpected response from server: " + response);
                setNotification("Errore dal server: " + response);
        }
    }

    /**
     * Impedisce di superare il limite massimo di caratteri nel campo categorie.
     */
    @FXML
    private void checkTextBox() {
        String text = categories_textarea.getText();
        if (text.length() > 255)
            categories_textarea.setText(text.substring(0, 255));
    }

    /**
     * Mostra un messaggio di notifica nella schermata corrente.
     *
     * @param msg messaggio da visualizzare
     */
    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    /**
     * Elimina il ristorante corrente previa conferma dell'utente.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void deleteRestaurant() throws IOException {
        if (!checkOnline()) {
            return;
        }

        ClientLogger.getInstance().info("Deleting restaurant, editing_id: " + editing_id);

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

        if (response == null) {
            fallback();
            return;
        }

        if (response.equals("ok")) {
            ClientLogger.getInstance().info("Restaurant deleted successfully");
            SceneManager.changeScene("MyRestaurants");
        } else {
            ClientLogger.getInstance().error("Failed to delete restaurant, server response: " + response);
            setNotification("Errore durante l'eliminazione");
        }
    }

    /**
     * Nodi interattivi da disabilitare in caso di fallback.
     */
    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                edit_btn, delete_btn,
                name_field, nation_field, city_field, address_field,
                latitude_field, longitude_field, price_field,
                categories_textarea, delivery_check, online_check
        };
    }
}
