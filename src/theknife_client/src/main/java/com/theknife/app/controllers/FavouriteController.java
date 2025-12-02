package com.theknife.app.controllers;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavouriteController implements OnlineChecker {

    @FXML private ListView<String> favourites_list;
    @FXML private Button remove_btn;
    @FXML private Button view_btn;
    @FXML private Label notification_label;

    private String[] fav_ids;
    private String[] fav_names;

    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("FavouriteController initialized");
        if (!checkOnline()) return;
        loadFavorites();
    }

    /**
     * Carica tutti i ristoranti che l'utente ha segnato come preferiti.
     * Usa il protocollo getRestaurants con onlyFav = "y".
     */
    private void loadFavorites() throws IOException {
        if (!checkOnline()) return;

        notification_label.setVisible(false);
        favourites_list.getItems().clear();
        remove_btn.setDisable(true);
        view_btn.setDisable(true);

        /* ================================
           PROTOCOLLO getRestaurants
           allineato al RestaurantHandler
         ================================= */

        Communicator.send("getRestaurants");
        Communicator.send("0");          // page
        Communicator.send("all");        // mode
        Communicator.send("-");          // first
        Communicator.send("-");          // second
        Communicator.send("-");          // range
        Communicator.send("-");          // price min
        Communicator.send("-");          // price max
        Communicator.send("-");          // category
        Communicator.send("n");          // delivery filter
        Communicator.send("n");          // online filter
        Communicator.send("-");          // stars min
        Communicator.send("-");          // stars max
        Communicator.send("y");          // ONLY FAVOURITES !!!

        /* ======================
           RISPOSTA DEL SERVER
         ====================== */

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        if (!"ok".equals(resp)) {
            notification_label.setVisible(true);
            notification_label.setText("Errore dal server: " + resp);
            return;
        }

        // pagine (non ci interessa)
        String pages = Communicator.read();
        String sizeStr = Communicator.read();

        if (pages == null || sizeStr == null) {
            fallback();
            return;
        }

        int size = Integer.parseInt(sizeStr);
        fav_ids   = new String[size];
        fav_names = new String[size];

        for (int i = 0; i < size; i++) {
            fav_ids[i]   = Communicator.read();
            fav_names[i] = Communicator.read();

            if (fav_ids[i] == null || fav_names[i] == null) {
                fallback();
                return;
            }
        }

        favourites_list.getItems().setAll(fav_names);
        checkSelected();
    }


    @FXML
    private void removeFavourite() throws IOException {
        int index = favourites_list.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Communicator.send("removeFavourite");
        Communicator.send(fav_ids[index]);

        String resp = Communicator.read();
        if (resp == null || !"ok".equals(resp)) {
            SceneManager.setAppWarning("Errore nella rimozione: " + resp);
            return;
        }

        // Rimuovi dall'elenco locale
        List<String> names = new ArrayList<>(Arrays.asList(fav_names));
        List<String> ids = new ArrayList<>(Arrays.asList(fav_ids));
        names.remove(index);
        ids.remove(index);

        fav_names = names.toArray(new String[0]);
        fav_ids = ids.toArray(new String[0]);

        favourites_list.getItems().setAll(fav_names);
        checkSelected();
    }


    @FXML
    private void viewRestaurant() throws IOException {
        if (!checkOnline()) return;

        int index = favourites_list.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        EditingRestaurant.setEditing(Integer.parseInt(fav_ids[index]));
        SceneManager.setPreviousNavigation("Favorites");
        SceneManager.changeScene("ViewRestaurantInfo");
    }


    @FXML
    private void checkSelected() {
        int index = favourites_list.getSelectionModel().getSelectedIndex();
        boolean disable = index < 0;

        remove_btn.setDisable(disable);
        view_btn.setDisable(disable);
    }


    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }


    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                favourites_list, remove_btn, view_btn
        };
    }
}
