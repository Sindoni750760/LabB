package com.theknife.app.controllers;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
/**
 * Controller per la schermata di informazioni dettagliate di un ristorante.
 * Gestisce la visualizzazione di tutte le informazioni, verifica lo stato nei preferiti
 * e consente di aggiungere o rimuovere il ristorante dalla lista dei preferiti.
 * Supporta inoltre la navigazione verso le recensioni.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */

public class ViewRestaurantInfo implements OnlineChecker {

    private boolean is_favourite;

    @FXML private Label name_label;
    @FXML private Label nation_label;
    @FXML private Label city_label;
    @FXML private Label address_label;
    @FXML private Label coordinates_label;
    @FXML private Label reviews_label;
    @FXML private Label price_label;
    @FXML private Label stars_label;
    @FXML private Label services_label;
    @FXML private Label categories_label;

    @FXML private Button fav_btn;

    @FXML
    private void initialize() throws IOException {
        if(!checkOnline()) return;
        ClientLogger.getInstance().info("ViewRestaurantInfo initialized for restaurant: " + EditingRestaurant.getId());

        String[] info = EditingRestaurant.getInfo();
        if (info == null) {
            // fallback soft
            name_label.setText("Ristorante non trovato");
            return;
        }

        name_label.setText(info[0]);
        nation_label.setText(info[1]);
        city_label.setText(info[2]);
        address_label.setText(info[3]);
        coordinates_label.setText(info[4] + "," + info[5]);
        price_label.setText(info[6] + " €");
        reviews_label.setText(info[10]);
        stars_label.setText(info[9].equals("0") ? "Non disponibile" : info[9] + "/5");
        categories_label.setText(info[11]);

        boolean d = info[7].equals("y");
        boolean o = info[8].equals("y");

        if (d && o)      services_label.setText("Delivery e prenotazione online");
        else if (d)      services_label.setText("Solo delivery");
        else if (o)      services_label.setText("Solo prenotazione online");
        else             services_label.setText("Nessuno");

        // Gestione pulsante preferiti
        if (User.getInfo() == null) {
            fav_btn.setVisible(false);
        } else {
            if (!checkOnline()) {
                fav_btn.setVisible(false);
            } else {
                Communicator.send("isFavourite");
                Communicator.send(Integer.toString(EditingRestaurant.getId()));

                String favResp = Communicator.read();
                if (favResp == null) {
                    fallback();
                    return;
                }

                is_favourite = favResp.equals("y");

                if (is_favourite)
                    fav_btn.setText("Rimuovi dai preferiti");
            }
        }
    }

    @FXML
    private void goBack() throws IOException {
        String ctx = SceneManager.getPreviousNavigation();
        if ("Favorites".equals(ctx)) {
            SceneManager.changeScene("Favorites");
        } else {
            SceneManager.changeScene("ViewRestaurants");
        }
    }

    @FXML
    private void viewReviews() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }

    @FXML
    private void addToFavourites() throws IOException {
        if (!checkOnline()) return;

        boolean wasFavourite = is_favourite;

        ClientLogger.getInstance().info(
                (wasFavourite ? "Removing" : "Adding") +
                        " restaurant from/to favourites: " + EditingRestaurant.getId()
        );

        Communicator.send(wasFavourite ? "removeFavourite" : "addFavourite");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        if (!"ok".equals(resp)) {
            // piccolo feedback sullo stato globale
            SceneManager.setAppWarning("Errore nella gestione dei preferiti: " + resp);
        } else {
            SceneManager.setAppAlert(
                    wasFavourite ? "Rimosso dai preferiti" : "Aggiunto ai preferiti"
            );
        }

        // Se appena AGGIUNTO → vai sempre alla pagina Preferiti
        if (!wasFavourite) {
            SceneManager.changeScene("Favorites");
            return;
        }

        // Se era preferito e l'ho rimosso:
        String ctx = SceneManager.getPreviousNavigation();
        if ("Favorites".equals(ctx)) {
            SceneManager.changeScene("Favorites");
        } else {
            SceneManager.changeScene("ViewRestaurants");
        }
    }

    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{fav_btn};
    }
}
