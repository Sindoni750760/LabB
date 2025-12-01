package com.theknife.app.controllers;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * Controller per la schermata di visualizzazione dei ristoranti.
 * Permette agli utenti di cercare ristoranti applicando vari filtri,
 * navigare tra le pagine dei risultati e visualizzare informazioni dettagliate.
 *
 * Implementa OnlineChecker per gestione unificata dei casi di server offline.
 */

public class ViewRestaurants implements OnlineChecker {

    private String[] restaurants_ids;
    private String[] restaurants_names;

    private int pages;
    private int current_page;

    // Filtri logici
    private String nation = "-";
    private String city = "-";
    private String price_min = "-";
    private String price_max = "-";
    private String stars_min = "-";
    private String stars_max = "-";
    private String category = null;
    private String has_delivery = "n";
    private String has_online = "n";
    private String only_favourites = "n";
    private String latitude = "-";
    private String longitude = "-";
    private String range_km = "-";

    @FXML private Label notification_label;
    @FXML private Label no_restaurants_label;
    @FXML private Label pages_label;

    @FXML private TextField nation_field;
    @FXML private TextField city_field;

    @FXML private TextField latitude_field;
    @FXML private TextField longitude_field;
    @FXML private TextField range_km_field;

    @FXML private TextField price_min_field;
    @FXML private TextField price_max_field;

    @FXML private TextField stars_min_field;
    @FXML private TextField stars_max_field;

    @FXML private TextField category_field;

    @FXML private CheckBox delivery_check;
    @FXML private CheckBox online_check;
    @FXML private CheckBox favourites_check;
    @FXML private CheckBox near_me_check;

    @FXML private ListView<String> restaurants_listview;

    @FXML private Button prev_btn;
    @FXML private Button next_btn;
    @FXML private Button view_info_btn;
    @FXML private Button clear_btn;

    @FXML
    private void initialize() {
        ClientLogger.getInstance().info("ViewRestaurants initialized");

        EditingRestaurant.reset();

        // I filtri "solo preferiti" e "vicino a me" hanno senso solo da loggato
        if (User.getInfo() != null) {
            favourites_check.setVisible(true);
            near_me_check.setVisible(true);
        } else {
            favourites_check.setVisible(false);
            near_me_check.setVisible(false);
        }

        // All’avvio NON facciamo ricerche automatiche:
        // l’utente deve prima inserire nazione + città e cliccare "Filtra".
        pages_label.setText("-/-");
        no_restaurants_label.setVisible(false);
        view_info_btn.setDisable(true);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
    }

    private String filledOrDash(String s) {
        return s.isEmpty() ? "-" : s;
    }

    @FXML
    private void updateFilters() throws IOException {
        hideNotification();

        // Nazione + città obbligatorie (per le ricerche normali)
        nation = nation_field.getText().trim();
        city   = city_field.getText().trim();

        if (nation.isEmpty() || city.isEmpty()) {
            setNotification("Inserisci NAZIONE e CITTÀ per effettuare la ricerca.");
            return;
        }

        // Gestione coordinate / raggio
        String lat   = latitude_field.getText().trim();
        String lon   = longitude_field.getText().trim();
        String range = range_km_field.getText().trim();

        if (near_me_check.isSelected()) {
            latitude = "-";               // saranno prese dal profilo utente lato server
            longitude = "-";
            range_km = range.isEmpty() ? "-" : range;
        } else {
            if (range.isEmpty()) {
                // Nessun filtro di distanza
                latitude = "-";
                longitude = "-";
                range_km = "-";
            } else {
                // Raggio presente → richiedo anche lat/lon
                if (lat.isEmpty() || lon.isEmpty()) {
                    setNotification("Per usare il raggio inserisci latitudine e longitudine, oppure lascia tutti e 3 i campi vuoti.");
                    return;
                }
                latitude = lat;
                longitude = lon;
                range_km = range;
            }
        }

        price_min = filledOrDash(price_min_field.getText().trim());
        price_max = filledOrDash(price_max_field.getText().trim());

        has_delivery = delivery_check.isSelected() ? "y" : "n";
        has_online   = online_check.isSelected()   ? "y" : "n";

        stars_min = filledOrDash(stars_min_field.getText().trim());
        stars_max = filledOrDash(stars_max_field.getText().trim());

        only_favourites = favourites_check.isSelected() ? "y" : "n";

        String cat = category_field.getText().trim();
        category = cat.isEmpty() ? null : cat;

        searchPage(0);
    }

    private void searchPage(int page) throws IOException {
        if (!checkOnline()) {
            return;
        }

        current_page = page;
        no_restaurants_label.setVisible(false);
        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        // Per la ricerca normale (non "solo preferiti") nazione + città sono obbligatorie
        if (!"y".equals(only_favourites)) {
            if (nation == null || nation.isBlank() ||
                city == null   || city.isBlank()) {
                setNotification("Inserisci NAZIONE e CITTÀ per effettuare la ricerca.");
                return;
            }
        }

        // Invio del nuovo protocollo
        Communicator.send("getRestaurants");
        Communicator.send(Integer.toString(page));                  // page
        Communicator.send(nation);                                  // nation
        Communicator.send(city);                                    // city
        Communicator.send(price_min);                               // priceMin
        Communicator.send(price_max);                               // priceMax
        Communicator.send(category == null ? "-" : category);       // category
        Communicator.send(has_delivery);                            // delivery
        Communicator.send(has_online);                              // online
        Communicator.send(stars_min);                               // starsMin
        Communicator.send(stars_max);                               // starsMax
        Communicator.send(near_me_check.isSelected() ? "y" : "n");  // nearMe
        Communicator.send(latitude);                                // lat
        Communicator.send(longitude);                               // lon
        Communicator.send(range_km);                                // rangeKm
        Communicator.send(only_favourites);                         // onlyFav

        String response = Communicator.read();

        if (response == null) {
            ClientLogger.getInstance().error("Server unreachable: failed to retrieve restaurants");
            fallback();
            return;
        }

        switch (response) {
            case "ok" -> {
                String pagesStr = Communicator.read();
                if (pagesStr == null) {
                    fallback();
                    return;
                }
                pages = Integer.parseInt(pagesStr);

                if (pages < 1) {
                    no_restaurants_label.setVisible(true);
                    // il server invia comunque una riga (che ignoriamo)
                    Communicator.read();
                    return;
                }

                if (page > 0)         prev_btn.setDisable(false);
                if (page + 1 < pages) next_btn.setDisable(false);

                pages_label.setText((page + 1) + "/" + pages);

                String sizeStr = Communicator.read();
                if (sizeStr == null) {
                    fallback();
                    return;
                }

                int size = Integer.parseInt(sizeStr);
                restaurants_ids   = new String[size];
                restaurants_names = new String[size];

                for (int i = 0; i < size; i++) {
                    restaurants_ids[i]   = Communicator.read();
                    restaurants_names[i] = Communicator.read();

                    if (restaurants_ids[i] == null || restaurants_names[i] == null) {
                        fallback();
                        return;
                    }
                }

                restaurants_listview.getItems().setAll(restaurants_names);
                checkSelected();
            }
            case "coordinates" -> setNotification("Coordinate / raggio non validi.");
            case "price"       -> setNotification("Range di prezzo non valido.");
            case "stars"       -> setNotification("Range di stelle non valido.");
            case "location"    -> setNotification("Nazione e città sono obbligatorie per la ricerca.");
            default            -> setNotification("Errore imprevisto dal server: " + response);
        }
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
        if (current_page > 0) {
            searchPage(--current_page);
        }
    }

    @FXML
    private void nextPage() throws IOException {
        if (current_page + 1 < pages) {
            searchPage(++current_page);
        }
    }

    @FXML
    private void checkSelected() {
        boolean disable = restaurants_listview.getSelectionModel().getSelectedIndex() < 0;
        view_info_btn.setDisable(disable);
    }

    @FXML
    private void viewRestaurantInfo() throws IOException {
        int index = restaurants_listview.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        int restaurant_id = Integer.parseInt(restaurants_ids[index]);
        EditingRestaurant.setEditing(restaurant_id);

        SceneManager.setPreviousNavigation("ViewRestaurants");
        SceneManager.changeScene("ViewRestaurantInfo");
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void clearFilters() {
        // NON azzero nazione/città: restano il contesto della ricerca
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
        handleCoordinates(); // riabilita i campi coord

        latitude = longitude = range_km = "-";
        price_min = price_max = "-";
        stars_min = stars_max = "-";
        has_delivery = has_online = "n";
        only_favourites = "n";
        category = null;

        hideNotification();
        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");
        no_restaurants_label.setVisible(false);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);
    }

    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                nation_field, city_field,
                latitude_field, longitude_field, range_km_field,
                price_min_field, price_max_field,
                stars_min_field, stars_max_field,
                category_field,
                delivery_check, online_check, favourites_check, near_me_check,
                restaurants_listview,
                prev_btn, next_btn, view_info_btn, clear_btn
        };
    }
}
