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

/**
 * Controller per la schermata di visualizzazione dei ristoranti.
 * Permette agli utenti di cercare ristoranti applicando vari filtri,
 * navigare tra le pagine dei risultati e visualizzare informazioni dettagliate.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ViewRestaurants {
    /** ID dei ristoranti visualizzati nella pagina corrente. */
    private String[] restaurants_ids;

    /** Nomi dei ristoranti visualizzati nella pagina corrente. */
    private String[] restaurants_names;

    /** Numero totale di pagine disponibili. */
    private int pages;

    /** Pagina attualmente visualizzata. */
    private int current_page;

    /** Coordinate geografiche e parametri di filtro. */
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

    /** Etichetta per notifiche di errore o messaggi informativi. */
    @FXML private Label notification_label;

    /** Etichetta mostrata se non ci sono ristoranti da visualizzare. */
    @FXML private Label no_restaurants_label;

    /** Etichetta che mostra la pagina corrente e il totale. */
    @FXML private Label pages_label;
    /** Campi di input per i filtri di ricerca. */
    @FXML
    private TextField latitude_field, longitude_field, range_km_field, price_min_field, price_max_field, stars_min_field, stars_max_field, category_field;
    /** Checkbox per filtri booleani. */
    @FXML
    private CheckBox delivery_check, online_check, favourites_check, near_me_check;
    /** Lista dei ristoranti trovati. */
    @FXML
    private ListView<String> restaurants_listview;
    /** Pulsanti per navigazione e visualizzazione dettagli. */
    @FXML
    private Button prev_btn, next_btn, view_info_btn, clear_btn;
    /**
     * Inizializza la schermata, resetta lo stato del ristorante in modifica
     * e avvia la ricerca iniziale.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void initialize() throws IOException {
        EditingRestaurant.reset();
        if(User.getInfo() != null) {
            favourites_check.setVisible(true);
            near_me_check.setVisible(true);
        }
        searchPage(0);
    }

    /**
     * Restituisce "-" se la stringa è vuota, altrimenti la stringa stessa.
     *
     * @param s stringa da verificare
     * @return "-" oppure la stringa originale
     */    
    private String filledOrDash(String s) {
        return s.isEmpty() ? "-" : s;
    }

    /**
     * Aggiorna i filtri di ricerca in base ai valori inseriti dall'utente
     * e avvia una nuova ricerca.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void updateFilters() throws IOException {
        //updates the filters to be used in the search
        hideNotification();

        String latText = latitude_field.getText().trim();
        String lonText = longitude_field.getText().trim();
        String rangeText = range_km_field.getText().trim();

        if(latText.isEmpty() || lonText.isEmpty()){
            setNotification("Inserisci latitudine e longitudine prima di cercare");
            return;
        }

        latitude =  latText;
        longitude = lonText;
        range_km = rangeText.isEmpty() ? "-" : rangeText;

        price_min = filledOrDash(price_min_field.getText());
        price_max = filledOrDash(price_max_field.getText());
        has_delivery = delivery_check.isSelected() ? "y" : "n";
        has_online = online_check.isSelected() ? "y" : "n";
        stars_min = filledOrDash(stars_min_field.getText());
        stars_max = filledOrDash(stars_max_field.getText());
        only_favourites = favourites_check.isSelected() ? "y" : "n";
        category = category_field.getText().isEmpty() ? null : category_field.getText();
        near_me = near_me_check.isSelected() ? "y" : "n";
        searchPage(0);
    }

    /**
     * Esegue la ricerca dei ristoranti per la pagina specificata,
     * applicando i filtri correnti.
     *
     * @param page numero della pagina da visualizzare
     * @throws IOException se si verifica un errore nella comunicazione
     */
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

    /**
     * Abilita o disabilita i campi delle coordinate
     * in base allo stato del checkbox "vicino a me".
     */
    @FXML
    private void handleCoordinates() {
        //enables/disables the coordinates input box based on the "near me" check box value
        latitude_field.setDisable(near_me_check.isSelected());
        longitude_field.setDisable(near_me_check.isSelected());
    }

    /**
     * Mostra un messaggio di notifica all'utente.
     *
     * @param msg messaggio da visualizzare
     */
    private void setNotification(String msg) {
        notification_label.setText(msg);
        notification_label.setVisible(true);
    }

    /** Nasconde la notifica attualmente visibile. */
    private void hideNotification() {
        notification_label.setVisible(false);
    }

    /**
     * Passa alla pagina precedente dei risultati.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void prevPage() throws IOException {
        searchPage(--current_page);
    }
    /**
     * Passa alla pagina successiva dei risultati.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void nextPage() throws IOException {
        searchPage(++current_page);
    }
    /**
     * Controlla se un ristorante è selezionato nella lista
     * e abilita/disabilita il pulsante di visualizzazione.
     */
    @FXML
    private void checkSelected() {
        int index = restaurants_listview.getSelectionModel().getSelectedIndex();
        boolean disable_buttons = index < 0;
        
        view_info_btn.setDisable(disable_buttons);
    }
    /**
     * Apre la schermata con le informazioni dettagliate del ristorante selezionato.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void viewRestaurantInfo() throws IOException {
        int restaurant_id = Integer.parseInt(restaurants_ids[restaurants_listview.getSelectionModel().getSelectedIndex()]);
        EditingRestaurant.setEditing(restaurant_id);
        SceneManager.changeScene("ViewRestaurantInfo");
    }
    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void clearFilters() throws IOException{
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
        has_delivery = has_online = only_favourites = near_me = "n";
        category = null;

        hideNotification();

        searchPage(0);
    }
}