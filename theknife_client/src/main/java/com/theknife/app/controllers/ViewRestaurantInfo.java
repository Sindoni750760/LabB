package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ViewRestaurantInfo {
    private boolean is_favourite;
    @FXML
    private Label name_label, nation_label, city_label, address_label, coordinates_label, reviews_label, price_label, stars_label, services_label;
    @FXML
    private Button fav_btn;

    @FXML
    private void initialize() throws IOException {
        if(User.getInfo() == null)
            fav_btn.setVisible(false);
        else {
            //checks if the restaurant is favourite
            Communicator.sendStream("isFavourite");
            Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
            is_favourite = Communicator.readStream().equals("y");

            if(is_favourite)
                fav_btn.setText("Rimuovi dai preferiti");
        }

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

    @FXML
    private void viewReviews() throws IOException {
        SceneManager.changeScene("RestaurantReviews");

    }

    @FXML
    private void addToFavourites() throws IOException {
        String restaurant_id = Integer.toString(EditingRestaurant.getId());
        if(is_favourite) {
            fav_btn.setText("Aggiungi ai preferiti");
            Communicator.sendStream("removeFavourite");
            Communicator.sendStream(restaurant_id);
            Communicator.readStream();
        } else {
            fav_btn.setText("Rimuovi dai preferiti");
            Communicator.sendStream("addFavourite");
            Communicator.sendStream(restaurant_id);
            Communicator.readStream();
        }

        is_favourite = !is_favourite;
    }
}