package com.theknife.app.controllers;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;

public class ViewRestaurants implements OnlineChecker {

    private String[] restaurants_ids;
    private String[] restaurants_names;

    private int pages = 0;
    private int current_page = 0;

    /** searchMode:
     * "all" | "location" | "coordinates" | "invalid"
     */
    private String searchMode = "all";

    // campi usati nel protocollo
    private String field1 = "-";   // nazione o latitudine
    private String field2 = "-";   // città o longitudine

    /* ============================
       FXML FIELDS (ALLINEATI AL FXML)
       ============================ */
    @FXML private TextField field1_input;      // nazione o latitudine
    @FXML private TextField field2_input;      // città o longitudine
    @FXML private TextField range_field;       // raggio in km

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


    /* ============================
       INITIALIZE
       ============================ */
    @FXML
    private void initialize() {
        ClientLogger.getInstance().info("ViewRestaurants initialized");

        EditingRestaurant.reset();

        pages_label.setText("-/-");
        no_restaurants_label.setVisible(false);
        view_info_btn.setDisable(true);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        loadAllRestaurants();
    }


    /* ============================
       DETECT SEARCH MODE
       ============================ */
    private void detectSearchMode() {

        String f1 = field1_input.getText().trim();
        String f2 = field2_input.getText().trim();

        if (f1.isEmpty() && f2.isEmpty()) {
            searchMode = "all";
            field1 = "-";
            field2 = "-";
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


    /* ============================
       APPLY FILTERS
       ============================ */
    @FXML
    private void updateFilters() throws IOException {
        hideNotification();

        detectSearchMode();

        if (searchMode.equals("invalid")) {
            setNotification("Inserisci due numeri (lat + lon) oppure due testi (nazione + città).");
            return;
        }

        searchPage(0);
    }


    /* ============================
       QUERY SERVER
       ============================ */
    private void searchPage(int page) throws IOException {
        if (!checkOnline()) return;

        restaurants_listview.getItems().clear();
        no_restaurants_label.setVisible(false);
        pages_label.setText("-/-");
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        current_page = page;

        // ------------------
        // PROTOCOLLO
        // ------------------
        Communicator.send("getRestaurants");
        Communicator.send(Integer.toString(page));

        Communicator.send(searchMode);
        Communicator.send(field1);
        Communicator.send(field2);

        // range
        String range = range_field.getText().trim();
        Communicator.send(range.isEmpty() ? "-" : range);

        // prezzo
        Communicator.send(price_min_field.getText().trim().isEmpty() ? "-" : price_min_field.getText().trim());
        Communicator.send(price_max_field.getText().trim().isEmpty() ? "-" : price_max_field.getText().trim());

        // categoria
        String cat = category_field.getText().trim();
        Communicator.send(cat.isEmpty() ? "-" : cat);

        // delivery / online
        Communicator.send(delivery_check.isSelected() ? "y" : "n");
        Communicator.send(online_check.isSelected() ? "y" : "n");

        // stelle
        Communicator.send(stars_min_field.getText().trim().isEmpty() ? "-" : stars_min_field.getText().trim());
        Communicator.send(stars_max_field.getText().trim().isEmpty() ? "-" : stars_max_field.getText().trim());

        // ------------------
        // RISPOSTA SERVER
        // ------------------
        String response = Communicator.read();
        if (response == null) {
            fallback();
            return;
        }

        switch (response) {

            case "ok" -> {
                pages = Integer.parseInt(Communicator.read());
                int size = Integer.parseInt(Communicator.read());

                if (pages == 0 || size == 0) {
                    no_restaurants_label.setVisible(true);
                    return;
                }

                restaurants_ids = new String[size];
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

            case "coordinates" -> setNotification("Coordinate non valide o raggio non valido.");
            case "price"       -> setNotification("Filtro prezzo non valido.");
            case "stars"       -> setNotification("Filtro stelle non valido.");
            case "invalid"     -> setNotification("Errore nei campi inseriti.");

            default -> setNotification("Errore: " + response);
        }
    }


    @FXML
    private void prevPage() throws IOException {
        if (current_page > 0) searchPage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        if (current_page + 1 < pages) searchPage(++current_page);
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


    @FXML
    private void clearFilters() {

        field1_input.clear();
        field2_input.clear();
        range_field.clear();
        price_min_field.clear();
        price_max_field.clear();
        stars_min_field.clear();
        stars_max_field.clear();
        category_field.clear();
        delivery_check.setSelected(false);
        online_check.setSelected(false);

        searchMode = "all";
        field1 = "-";
        field2 = "-";

        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");
        no_restaurants_label.setVisible(false);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        hideNotification();
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
}
