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
import javafx.scene.control.TextField;

public class EditRestaurant {
    private int editing_id;
    @FXML
    private Button edit_btn, delete_btn;
    @FXML
    private TextField name_field, nation_field, city_field, address_field, latitude_field, longitude_field, price_field;
    @FXML
    private CheckBox delivery_check, online_check;
    @FXML
    private Label notification_label;

    @FXML
    private void initialize() {
        //gets the id of the restaurant being edited
        editing_id = EditingRestaurant.getId();

        //if it's editing a restaurant, sets the info
        if(editing_id > 0) {
            String[] restaurant_info = EditingRestaurant.getInfo();
            //return new String[]{name, nation, city, address, latitude, longitude, avg_price, has_delivery, has_online};
            name_field.setText(restaurant_info[0]);
            nation_field.setText(restaurant_info[1]);
            city_field.setText(restaurant_info[2]);
            address_field.setText(restaurant_info[3]);
            latitude_field.setText(restaurant_info[4]);
            longitude_field.setText(restaurant_info[5]);
            price_field.setText(restaurant_info[6]);
            delivery_check.setSelected(restaurant_info[7].equals("y"));
            online_check.setSelected(restaurant_info[8].equals("y"));
        } else { //if not editing, changes buttons displays
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
        String name = name_field.getText(),
        nation = nation_field.getText(),
        city = city_field.getText(),
        address = address_field.getText(),
        latitude = latitude_field.getText(),
        longitude = longitude_field.getText(),
        price = price_field.getText();
        boolean has_delivery = delivery_check.isSelected(), has_online = online_check.isSelected();

        String response;
        //editing a restaurant
        if(editing_id > 0)
            response = EditingRestaurant.editRestaurant(editing_id, name, nation, city, address, latitude, longitude, price, has_delivery, has_online);
        else //not editing a restaurant
            response = EditingRestaurant.addRestaurant(name, nation, city, address, latitude, longitude, price, has_delivery, has_online);
        
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

    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    @FXML
    private void deleteRestaurant() throws IOException {
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