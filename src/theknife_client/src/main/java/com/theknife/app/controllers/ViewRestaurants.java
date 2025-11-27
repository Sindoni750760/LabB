package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller per la schermata di visualizzazione dei ristoranti.
 * Permette agli utenti di cercare ristoranti applicando vari filtri,
 * navigare tra le pagine dei risultati e visualizzare informazioni dettagliate.
 *
 * Implementa OnlineChecker per gestione unificata dei casi di server offline.
 */
public class ViewRestaurants implements OnlineChecker {

    private static boolean startFavoritesMode = false;

    public static void openFavoritesFromApp() throws IOException {
        startFavoritesMode = true;
        SceneManager.changeScene("ViewRestaurants");
    }

    private String[] restaurants_ids;
    private String[] restaurants_names;

    private int pages;
    private int current_page;

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
            category = null;

    @FXML private Label notification_label;
    @FXML private Label no_restaurants_label;
    @FXML private Label pages_label;

    @FXML private TextField latitude_field, longitude_field, range_km_field,
            price_min_field, price_max_field, stars_min_field, stars_max_field,
            category_field;

    @FXML private CheckBox delivery_check, online_check, favourites_check, near_me_check;

    @FXML private ListView<String> restaurants_listview;

    @FXML private Button prev_btn, next_btn, view_info_btn, clear_btn;

    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("ViewRestaurants initialized");

        EditingRestaurant.reset();

        if (User.getInfo() != null) {
            favourites_check.setVisible(true);
            near_me_check.setVisible(true);
        }

        if (startFavoritesMode) {
            only_favourites = "y";
            favourites_check.setSelected(true);
            startFavoritesMode = false;
        }

        Platform.runLater(() -> {
            try {
                searchPage(0);
            } catch (IOException e) {
                ClientLogger.getInstance().error(
                        "Error loading restaurants in initialize: " + e.getMessage()
                );
            }
        });
    }

    private String filledOrDash(String s) {
        return s.isEmpty() ? "-" : s;
    }

    @FXML
    private void updateFilters() throws IOException {
        hideNotification();

        String lat = latitude_field.getText().trim();
        String lon = longitude_field.getText().trim();
        String range = range_km_field.getText().trim();

        if (near_me_check.isSelected()) {
            latitude = "-";
            longitude = "-";
            range_km = range.isEmpty() ? "-" : range;
        } else {

            if (lat.isEmpty() && lon.isEmpty()) {
                latitude = "-";
                longitude = "-";
                range_km = "-";
            } else if (lat.isEmpty() || lon.isEmpty() || range.isEmpty()) {
                setNotification("Inserisci latitudine, longitudine e raggio, oppure lascia tutto vuoto");
                return;
            } else {
                latitude = lat;
                longitude = lon;
                range_km = range;
            }
        }

        price_min = filledOrDash(price_min_field.getText());
        price_max = filledOrDash(price_max_field.getText());
        has_delivery = delivery_check.isSelected() ? "y" : "n";
        has_online = online_check.isSelected() ? "y" : "n";
        stars_min = filledOrDash(stars_min_field.getText());
        stars_max = filledOrDash(stars_max_field.getText());
        only_favourites = favourites_check.isSelected() ? "y" : "n";
        category = category_field.getText().isEmpty() ? null : category_field.getText();

        searchPage(0);
    }

    private void searchPage(int page) throws IOException {
        if (!checkOnline()) {
            return;
        }

        current_page = page;
        no_restaurants_label.setVisible(false);

        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");

        Communicator.send("getRestaurants");
        Communicator.send(Integer.toString(page));
        Communicator.send(latitude);
        Communicator.send(longitude);
        Communicator.send(range_km);
        Communicator.send(price_min);
        Communicator.send(price_max);
        Communicator.send(has_delivery);
        Communicator.send(has_online);
        Communicator.send(stars_min);
        Communicator.send(stars_max);
        Communicator.send(only_favourites);

        if (category == null) {
            Communicator.send("n");
        } else {
            Communicator.send("y");
            Communicator.send(category);
        }

        String response = Communicator.read();

        if (response == null) {
            ClientLogger.getInstance().error("Server unreachable: failed to retrieve restaurants");
            fallback();
            return;
        }

        switch (response) {

            case "ok":

                String pagesStr = Communicator.read();
                if (pagesStr == null) {
                    fallback();
                    return;
                }
                pages = Integer.parseInt(pagesStr);

                if (pages < 1) {
                    no_restaurants_label.setVisible(true);
                    Communicator.read(); // linea vuota inviata dal server
                    break;
                }

                if (page > 0) prev_btn.setDisable(false);
                if (page + 1 < pages) next_btn.setDisable(false);

                pages_label.setText((page + 1) + "/" + pages);

                String sizeStr = Communicator.read();
                if (sizeStr == null) {
                    fallback();
                    return;
                }

                int size = Integer.parseInt(sizeStr);
                restaurants_ids = new String[size];
                restaurants_names = new String[size];

                for (int i = 0; i < size; i++) {
                    restaurants_ids[i] = Communicator.read();
                    restaurants_names[i] = Communicator.read();

                    if (restaurants_ids[i] == null || restaurants_names[i] == null) {
                        fallback();
                        return;
                    }
                }

                restaurants_listview.getItems().setAll(restaurants_names);
                break;

            case "coordinates":
                setNotification("Coordinate non valide");
                break;

            case "price":
                setNotification("Range di prezzo non valido");
                break;

            case "stars":
                setNotification("Range di stelle non valido");
                break;

            default:
                setNotification("Errore imprevisto dal server: " + response);
        }

        checkSelected();
    }

    @FXML
    private void handleCoordinates() {
        boolean disable = near_me_check.isSelected();
        latitude_field.setDisable(disable);
        longitude_field.setDisable(disable);
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
        boolean disable = restaurants_listview.getSelectionModel().getSelectedIndex() < 0;
        view_info_btn.setDisable(disable);
    }

    @FXML
    private void viewRestaurantInfo() throws IOException {
        int index = restaurants_listview.getSelectionModel().getSelectedIndex();
        int restaurant_id = Integer.parseInt(restaurants_ids[index]);

        EditingRestaurant.setEditing(restaurant_id);

        SceneManager.changeScene("ViewRestaurantInfo");
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void clearFilters() throws IOException {
        latitude_field.clear();
        longitude_field.clear();
        range_km_field.clear();
        price_min_field.clear();
        price_max_field.clear();
        stars_min_field.clear();
        stars_max_field.clear();
        category_field.clear();

        delivery_check.setSelected(false);
        online_check.setSelected(false);
        favourites_check.setSelected(false);
        near_me_check.setSelected(false);

        latitude = longitude = range_km = "-";
        price_min = price_max = stars_min = stars_max = "-";
        has_delivery = has_online = only_favourites = "n";
        category = null;

        hideNotification();

        searchPage(0);
    }

    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                latitude_field, longitude_field, range_km_field,
                price_min_field, price_max_field, stars_min_field, stars_max_field,
                category_field,
                delivery_check, online_check, favourites_check, near_me_check,
                restaurants_listview,
                prev_btn, next_btn, view_info_btn, clear_btn
        };
    }
}
