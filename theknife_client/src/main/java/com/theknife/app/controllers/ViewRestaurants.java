package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ViewRestaurants {
    private String[] restaurants_ids;
    private String[] restaurants_names;
    private int pages, current_page;
    private String latitude = "-",
    longitude = "-",
    range_km = "-",
    price_min = "-",
    price_max = "-",
    has_delivery = "n",
    has_online = "n",
    stars_min = "-",
    stars_max = "-",
    only_favourites = "-";

    @FXML
    private Label notification_label, no_restaurants_label, pages_label;
    @FXML
    private TextField latitude_field, longitude_field, range_km_field, price_min_field, price_max_field, stars_min_field, stars_max_field;
    @FXML
    private CheckBox delivery_check, online_check;
    @FXML
    private ListView<String> restaurants_listview;

    @FXML
    private void initialize() throws IOException {
        searchPage(0);
    }

    //returns "-" if the string is empty, or the string if it's not empty
    private String filledOrDash(String s) {
        return s.isEmpty() ? "-" : s;
    }

    @FXML
    private void updateFilters() throws IOException {
        hideNotification();
        latitude = filledOrDash(latitude_field.getText());
        longitude = filledOrDash(longitude_field.getText());
        range_km = filledOrDash(range_km_field.getText());
        price_min = filledOrDash(price_min_field.getText());
        price_max = filledOrDash(price_max_field.getText());
        has_delivery = delivery_check.isSelected() ? "y" : "n";
        has_online = online_check.isSelected() ? "y" : "n";
        stars_min = filledOrDash(stars_min_field.getText());
        stars_max = filledOrDash(stars_max_field.getText());
        searchPage(0);
    }

    private void searchPage(int page) throws IOException {
        no_restaurants_label.setVisible(false);
        Communicator.sendStream("getRestaurants");
        Communicator.sendStream(Integer.toString(page));
        Communicator.sendStream(latitude);
        Communicator.sendStream(longitude);
        Communicator.sendStream(range_km);
        Communicator.sendStream(price_min);
        Communicator.sendStream(price_max);
        Communicator.sendStream(has_delivery);
        Communicator.sendStream(has_online);
        Communicator.sendStream(stars_min);
        Communicator.sendStream(stars_max);

        if(User.getInfo() != null)
            Communicator.sendStream(only_favourites);
        
        String response = Communicator.readStream();
        switch(response) {
            case "ok":
                pages = Integer.parseInt(Communicator.readStream());
                if(pages < 1) {
                    pages_label.setText("-/-");
                    no_restaurants_label.setVisible(true);
                    break;
                }

                pages_label.setText(Integer.toString(page + 1) + '/' + pages);
                int size = Integer.parseInt(Communicator.readStream());
                restaurants_ids = new String[size];
                restaurants_names = new String[size];

                for(int i = 0; i < size; i++) {
                    restaurants_ids[i] = Communicator.readStream();
                    restaurants_names[i] = Communicator.readStream();
                }

                restaurants_listview.getItems().setAll(restaurants_names);
                break;
            case "coordinates":
                setNotification("Le coordinate non sono state inserite nel modo corretto");
                break;
            case "price":
                setNotification("Il range di prezzo non è stato inserito nel modo corretto");
                break;
            case "stars":
                setNotification("Il range di stelle non è stato inserito nel modo corretto");
                break;
        }
    }

    private void setNotification(String msg) {
        notification_label.setText(msg);
        notification_label.setVisible(true);
    }

    private void hideNotification() {
        notification_label.setVisible(false);
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }
}