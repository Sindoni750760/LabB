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
import javafx.scene.Node;

public class ViewRestaurants implements OnlineChecker {

    private String[] restaurants_ids;
    private String[] restaurants_names;

    private int pages = 0;
    private int current_page = 0;

    // MODE AND FILTERS
    private String searchMode = "invalid";   // "coordinates" | "location" | "invalid" | "all"
    private String field1 = "-";
    private String field2 = "-";

    @FXML private TextField field1_input;
    @FXML private TextField field2_input;
    @FXML private TextField range_field;

    @FXML private TextField price_min_field;
    @FXML private TextField price_max_field;

    @FXML private TextField stars_min_field;
    @FXML private TextField stars_max_field;

    @FXML private TextField category_field;

    @FXML private CheckBox delivery_check;
    @FXML private CheckBox online_check;

    @FXML private ListView<String> restaurants_listview;

    @FXML private Label notification_label;
    @FXML private Label no_restaurants_label;
    @FXML private Label pages_label;

    @FXML private Button prev_btn;
    @FXML private Button next_btn;
    @FXML private Button view_info_btn;

    @FXML private Button clear_btn;


    @FXML
    private void initialize() {
        ClientLogger.getInstance().info("ViewRestaurants initialized");

        EditingRestaurant.reset();

        pages_label.setText("-/-");
        no_restaurants_label.setVisible(false);
        view_info_btn.setDisable(true);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        // carica tutti i ristoranti all'avvio
        loadAllRestaurants();
    }


    private void detectSearchMode() {
        String f1 = field1_input.getText().trim();
        String f2 = field2_input.getText().trim();

        if (f1.isEmpty() || f2.isEmpty()) {
            searchMode = "invalid";
            return;
        }

        boolean f1Double = f1.matches("[-+]?[0-9]*\\.?[0-9]+");
        boolean f2Double = f2.matches("[-+]?[0-9]*\\.?[0-9]+");

        if (f1Double && f2Double) {
            searchMode = "coordinates";
        } else if (!f1Double && !f2Double) {
            searchMode = "location";
        } else {
            searchMode = "invalid";
        }

        field1 = f1;
        field2 = f2;
    }


    @FXML
    private void updateFilters() throws IOException {
        hideNotification();

        detectSearchMode();

        if (searchMode.equals("invalid")) {
            setNotification("I due campi devono essere entrambi numeri (coordinate) oppure entrambi testo (nazione + città).");
            return;
        }

        searchPage(0);
    }


    private void searchPage(int page) throws IOException {
        if (!checkOnline()) return;

        restaurants_listview.getItems().clear();
        no_restaurants_label.setVisible(false);
        pages_label.setText("-/-");
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        current_page = page;


        /* =====================================================
           PROTOCOLLO getRestaurants (13 parametri)
         ===================================================== */

        Communicator.send("getRestaurants");
        Communicator.send(Integer.toString(page));

        // Search mode
        Communicator.send(searchMode.equals("all") ? "all" : searchMode);
        Communicator.send(field1);
        Communicator.send(field2);

        // Range
        String range = range_field.getText().trim();
        Communicator.send(range.isEmpty() ? "-" : range);

        // Price
        Communicator.send(price_min_field.getText().trim().isEmpty() ? "-" : price_min_field.getText().trim());
        Communicator.send(price_max_field.getText().trim().isEmpty() ? "-" : price_max_field.getText().trim());

        // Category
        String cat = category_field.getText().trim();
        Communicator.send(cat.isEmpty() ? "-" : cat);

        // Delivery / Online
        Communicator.send(delivery_check.isSelected() ? "y" : "n");
        Communicator.send(online_check.isSelected() ? "y" : "n");

        // Stars
        Communicator.send(stars_min_field.getText().trim().isEmpty() ? "-" : stars_min_field.getText().trim());
        Communicator.send(stars_max_field.getText().trim().isEmpty() ? "-" : stars_max_field.getText().trim());

        // === NEW: onlyFavourite flag (always NO here) ===
        Communicator.send("n");


        /* =====================================================
           RISPOSTA SERVER
         ===================================================== */

        String response = Communicator.read();
        if (response == null) {
            fallback();
            return;
        }

        switch (response) {

            case "ok" -> {
                String pagesStr = Communicator.read();
                pages = Integer.parseInt(pagesStr);

                String sizeStr = Communicator.read();
                int size = Integer.parseInt(sizeStr);

                if (pages == 0 || size == 0) {
                    no_restaurants_label.setVisible(true);
                    return;
                }

                restaurants_ids   = new String[size];
                restaurants_names = new String[size];

                for (int i = 0; i < size; i++) {
                    restaurants_ids[i] = Communicator.read();
                    restaurants_names[i] = Communicator.read();
                }

                restaurants_listview.getItems().setAll(restaurants_names);

                pages_label.setText((page + 1) + "/" + pages);

                if (page > 0)         prev_btn.setDisable(false);
                if (page + 1 < pages) next_btn.setDisable(false);

                checkSelected();
            }

            case "invalid" -> setNotification("I due campi devono essere coerenti (entrambi testo o entrambi numeri)");
            case "coordinates" -> setNotification("Coordinate o raggio non validi.");
            case "price" -> setNotification("Range di prezzo non valido.");
            case "stars" -> setNotification("Range stelle non valido.");

            default -> setNotification("Errore: " + response);
        }
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
        view_info_btn.setDisable(
                restaurants_listview.getSelectionModel().getSelectedIndex() < 0
        );
    }


    @FXML
    private void viewRestaurantInfo() throws IOException {
        int index = restaurants_listview.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        EditingRestaurant.setEditing(Integer.parseInt(restaurants_ids[index]));
        SceneManager.setPreviousNavigation("ViewRestaurants");
        SceneManager.changeScene("ViewRestaurantInfo");
    }

    /**
     * Ripristina tutti i campi dei filtri alla condizione iniziale,
     * imposta la searchMode su "all", azzera field1/field2,
     * e ricarica la pagina 0.
     */
    @FXML
    private void clearFilters() {
        hideNotification();

        // reset input dei due campi (possono essere nazione/città o lat/lon)
        field1_input.clear();
        field2_input.clear();
        field1 = "-";
        field2 = "-";

        // reset raggio
        range_field.clear();

        // reset prezzo
        price_min_field.clear();
        price_max_field.clear();

        // reset stelle
        stars_min_field.clear();
        stars_max_field.clear();

        // reset categoria
        category_field.clear();

        // reset checkbox
        delivery_check.setSelected(false);
        online_check.setSelected(false);

        // reset etichette / stato GUI
        no_restaurants_label.setVisible(false);
        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        // stato logico interno
        searchMode = "all";
        pages = 0;
        current_page = 0;

        // ricarica tutti i ristoranti usando la logica già testata
        loadAllRestaurants();
    }


    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }


    private void setNotification(String msg) {
        notification_label.setText(msg);
        notification_label.setVisible(true);
    }

    private void hideNotification() {
        notification_label.setVisible(false);
    }


    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                field1_input, field2_input, range_field,
                price_min_field, price_max_field,
                stars_min_field, stars_max_field,
                category_field,
                delivery_check, online_check,
                restaurants_listview,
                prev_btn, next_btn, view_info_btn, clear_btn
        };
    }


    private void loadAllRestaurants() {
        try {
            searchMode = "all";
            field1 = "-";
            field2 = "-";
            searchPage(0);
        } catch (Exception e) {
            ClientLogger.getInstance().error("Failed to load all restaurants");
        }
    }
}
