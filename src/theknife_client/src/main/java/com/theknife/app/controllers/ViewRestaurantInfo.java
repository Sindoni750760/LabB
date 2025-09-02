package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller per la schermata di visualizzazione delle informazioni di un ristorante.
 * Mostra i dettagli del ristorante selezionato, consente di visualizzare le recensioni
 * e di aggiungere/rimuovere il ristorante dai preferiti.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ViewRestaurantInfo {
    /** Indica se il ristorante è attualmente tra i preferiti dell'utente. */
    private boolean is_favourite;
        /** Etichetta per il nome del ristorante. */
    @FXML private Label name_label;

    /** Etichetta per la nazione del ristorante. */
    @FXML private Label nation_label;

    /** Etichetta per la città del ristorante. */
    @FXML private Label city_label;

    /** Etichetta per l'indirizzo del ristorante. */
    @FXML private Label address_label;

    /** Etichetta per le coordinate geografiche del ristorante. */
    @FXML private Label coordinates_label;

    /** Etichetta per il numero di recensioni ricevute. */
    @FXML private Label reviews_label;

    /** Etichetta per il prezzo medio del ristorante. */
    @FXML private Label price_label;

    /** Etichetta per la valutazione media in stelle. */
    @FXML private Label stars_label;

    /** Etichetta per i servizi offerti (delivery, prenotazione online). */
    @FXML private Label services_label;

    /** Etichetta per le categorie associate al ristorante. */
    @FXML private Label categories_label;

    /** Pulsante per aggiungere o rimuovere il ristorante dai preferiti. */
    @FXML private Button fav_btn;


    /**
     * Inizializza la schermata caricando le informazioni del ristorante selezionato.
     * Verifica se il ristorante è tra i preferiti e aggiorna l'interfaccia di conseguenza.
     *
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */
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
        categories_label.setText(restaurant_info[11]);

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

    /**
     * Torna alla schermata di visualizzazione dell'elenco dei ristoranti.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("ViewRestaurants");
    }

    /**
     * Passa alla schermata delle recensioni del ristorante selezionato.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void viewReviews() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }

    /**
     * Aggiunge o rimuove il ristorante dai preferiti dell'utente.
     * Aggiorna il testo del pulsante e lo stato interno.
     *
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */
    @FXML
    private void addToFavourites() throws IOException {
        //sets/unsets current restaurant as favourite
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