package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ViewRestaurantInfo {
    @FXML
    private Label name_label, nation_label, city_label, address_label, coordinates_label, reviews_label, price_label, stars_label, services_label;

    @FXML
    private void initialize() {
        String[] restaurant_info = EditingRestaurant.getInfo();

        name_label.setText(restaurant_info[0]);
        nation_label.setText(restaurant_info[1]);
        city_label.setText(restaurant_info[2]);
        address_label.setText(restaurant_info[3]);
        coordinates_label.setText(restaurant_info[4] + ',' + restaurant_info[5]);
        reviews_label.setText(restaurant_info[10]);
        price_label.setText(restaurant_info[6] + " €");
        stars_label.setText(restaurant_info[9].equals("0") ? "Non disponibile" : restaurant_info[9] + "/5");

        boolean has_delivery = restaurant_info[7].equals("y"), has_online = restaurant_info[8].equals("y");
        if(has_delivery && has_online)
            services_label.setText("Delivery e prenotazione online");
        else if(has_delivery)
            services_label.setText("Delivery");
        else if(has_online)
            services_label.setText("Prenotazione online");
        else
            services_label.setText("Nessuno");
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("ViewRestaurants");
    }
}