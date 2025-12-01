package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controller per la schermata "MyRestaurants".
 * Gestisce la visualizzazione, modifica, aggiunta e rimozione dei ristoranti dell'utente.
 * Supporta la paginazione e la navigazione tra le recensioni.
 *
 * Implementa OnlineChecker per gestione centralizzata del fallback.
 *
 * @author ...
 */
public class MyRestaurants implements OnlineChecker {

    @FXML private ListView<String> restaurants_container;
    @FXML private Label no_restaurants_label;
    @FXML private Label page_label;
    @FXML private Button edit_btn, reviews_btn, prev_btn, next_btn;

    private int[] restaurants_ids;
    private String[] restaurants_names;

    private int total_pages;
    private int current_page = 0;

    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("MyRestaurants initialized");
        EditingRestaurant.reset();

        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        if (!checkOnline()) {
            no_restaurants_label.setVisible(true);
            no_restaurants_label.setText("Il server non è raggiungibile");
            return;
        }

        Communicator.send("getMyRestaurantsPages");
        String pagesStr = Communicator.read();
        if (pagesStr == null) {
            fallback();
            return;
        }

        total_pages = Integer.parseInt(pagesStr);

        if (total_pages > 0)
            changePage(0);
        else
            no_restaurants_label.setVisible(true);
    }

    private void changePage(int page) throws IOException {
        if (!checkOnline()) return;
        

        current_page = page;

        page_label.setText((page + 1) + "/" + total_pages);

        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.send("getMyRestaurants");
        Communicator.send(Integer.toString(page));

        String sizeStr = Communicator.read();
        if (sizeStr == null) {
            fallback();
            return;
        }

        int size = Integer.parseInt(sizeStr);

        restaurants_ids = new int[size];
        restaurants_names = new String[size];

        for (int i = 0; i < size; i++) {
            String idStr = Communicator.read();
            String nameStr = Communicator.read();

            if (idStr == null || nameStr == null) {
                fallback();
                return;
            }

            restaurants_ids[i] = Integer.parseInt(idStr);
            restaurants_names[i] = nameStr;
        }

        restaurants_container.getItems().setAll(restaurants_names);
        checkSelected();
    }

    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    @FXML
    private void checkSelected() {
        int index = restaurants_container.getSelectionModel().getSelectedIndex();
        boolean disable = index < 0;

        edit_btn.setDisable(disable);
        reviews_btn.setDisable(disable);
    }

    @FXML
    private void editSelected() throws IOException {
        if (!checkOnline()) return;
        int id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(id);
        SceneManager.changeScene("EditRestaurant");
    }

    @FXML
    private void viewReviews() throws IOException {
        if(!checkOnline()) return;
        int id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(id);
        SceneManager.changeScene("RestaurantReviews");
    }

    @FXML
    private void logout() throws IOException {
        User.logout();
        SceneManager.changeScene("App");
    }

    @FXML
    private void addRestaurant() throws IOException {
        EditingRestaurant.reset();
        SceneManager.changeScene("EditRestaurant");
    }

    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                restaurants_container,
                edit_btn, reviews_btn, prev_btn, next_btn
        };
    }
}
