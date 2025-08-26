package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    only_favourites = "n",
    near_me = "n",
    category = null;

    @FXML
    private Label notification_label, no_restaurants_label, pages_label;
    @FXML
    private TextField latitude_field, longitude_field, range_km_field, price_min_field, price_max_field, stars_min_field, stars_max_field, category_field;
    @FXML
    private CheckBox delivery_check, online_check, favourites_check, near_me_check;
    @FXML
    private ListView<String> restaurants_listview;
    @FXML
    private Button prev_btn, next_btn, view_info_btn;

    @FXML
    private void initialize() throws IOException {
        EditingRestaurant.reset();
        if(User.getInfo() != null) {
            favourites_check.setVisible(true);
            near_me_check.setVisible(true);
        }
        searchPage(0);
    }

    //returns "-" if the string is empty, or the string if it's not empty
    private String filledOrDash(String s) {
        return s.isEmpty() ? "-" : s;
    }

    @FXML
    private void updateFilters() throws IOException {
        //updates the filters to be used in the search
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
        only_favourites = favourites_check.isSelected() ? "y" : "n";
        if(category_field.getText().isEmpty())
            category = null;
        else
            category = category_field.getText();
        near_me = near_me_check.isSelected() ? "y" : "n";
        searchPage(0);
    }

    //function used to update the displayed restaurants in the listview
    private void searchPage(int page) throws IOException {
        current_page = page;
        no_restaurants_label.setVisible(false);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");
        

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
        if(category == null)
            Communicator.sendStream("n");
        else {
            Communicator.sendStream("y");
            Communicator.sendStream(category);
        }
        Communicator.sendStream(near_me);


        if(User.getInfo() != null)
            Communicator.sendStream(only_favourites);
        
        String response = Communicator.readStream();
        switch(response) {
            case "ok":
                pages = Integer.parseInt(Communicator.readStream());
                if(pages < 1) {
                    no_restaurants_label.setVisible(true);
                    Communicator.readStream();
                    break;
                }

                if(page > 0)
                    prev_btn.setDisable(false);
                if(page + 1 < pages)
                    next_btn.setDisable(false);

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
        
        checkSelected();
    }

    @FXML
    private void handleCoordinates() {
        //enables/disables the coordinates input box based on the "near me" check box value
        latitude_field.setDisable(near_me_check.isSelected());
        longitude_field.setDisable(near_me_check.isSelected());
    }

    private void setNotification(String msg) {
        notification_label.setText(msg);
        notification_label.setVisible(true);
    }

    private void hideNotification() {
        notification_label.setVisible(false);
    }

    @FXML
    private void prevPage() throws IOException {
        searchPage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        searchPage(++current_page);
    }

    @FXML
    private void checkSelected() {
        int index = restaurants_listview.getSelectionModel().getSelectedIndex();
        boolean disable_buttons = index < 0;
        
        view_info_btn.setDisable(disable_buttons);
    }

    @FXML
    private void viewRestaurantInfo() throws IOException {
        int restaurant_id = Integer.parseInt(restaurants_ids[restaurants_listview.getSelectionModel().getSelectedIndex()]);
        EditingRestaurant.setEditing(restaurant_id);
        SceneManager.changeScene("ViewRestaurantInfo");
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }
}