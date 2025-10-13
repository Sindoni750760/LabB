package com.theknife.app.controllers;

import java.io.IOException;

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
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class MyRestaurants {
    /** Lista visuale contenente i nomi dei ristoranti dell'utente. */
    @FXML
    private ListView<String> restaurants_container;
    
    /** Etichetta mostrata se l'utente non ha ristoranti. */
    @FXML
    private Label no_restaurants_label, 
    /** Etichetta che mostra la pagina corrente e il totale delle pagine. */
    page_label;
    /** Pulsante per modificare il ristorante selezionato. */
    @FXML
    private Button edit_btn, 
    /** Pulsante per visualizzare le recensioni del ristorante selezionato. */
    reviews_btn, 
    /** Pulsante per andare alla pagina precedente. */
    prev_btn, 
    /** Pulsante per andare alla pagina successiva. */
    next_btn;
    /** Array contenente gli ID dei ristoranti dell'utente. */
    private int[] restaurants_ids;
    /** Array contenente i nomi dei ristoranti dell'utente. */
    private String[] restaurants_names;
    /** Numero totale di pagine disponibili. */
    private int total_pages, 
    /** Pagina attualmente visualizzata. */
    current_page = 0;

    /**
     * Inizializza la schermata caricando i ristoranti dell'utente.
     * Se non ci sono ristoranti, mostra un messaggio.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void initialize() throws IOException {
        EditingRestaurant.reset();
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
    Communicator.sendStream("getMyRestaurantsPages");
    String totalPagesStr = Communicator.readStream();
    if (totalPagesStr == null) { SceneManager.setAppWarning("Il server non è raggiungibile"); SceneManager.changeScene("App"); return; }
    total_pages = Integer.parseInt(totalPagesStr);
        if(total_pages > 0)
            changePage(0);
        else
            no_restaurants_label.setVisible(true);
    }

/**
     * Cambia la pagina visualizzata dei ristoranti dell'utente.
     * Aggiorna la lista e lo stato dei pulsanti di navigazione.
     *
     * @param page numero della pagina da visualizzare
     * @throws IOException se si verifica un errore nella comunicazione
     */    
    private void changePage(int page) throws IOException {
    page_label.setText(Integer.toString(page + 1) + "/" + Integer.toString(total_pages));
    prev_btn.setDisable(page < 1);
    next_btn.setDisable(page + 1 >= total_pages);

    Communicator.sendStream("getMyRestaurants");
    Communicator.sendStream(Integer.toString(page));
    String sizeStr = Communicator.readStream();
    if (sizeStr == null) { SceneManager.setAppWarning("Il server non è raggiungibile"); SceneManager.changeScene("App"); return; }
    int size = Integer.parseInt(sizeStr);

    restaurants_ids = new int[size];
    restaurants_names = new String[size];

    for(int i = 0; i < size; i++) {
    String idStr = Communicator.readStream();
    if (idStr == null) { SceneManager.setAppWarning("Il server non è raggiungibile"); SceneManager.changeScene("App"); return; }
    restaurants_ids[i] = Integer.parseInt(idStr);
    String name = Communicator.readStream();
    if (name == null) { SceneManager.setAppWarning("Il server non è raggiungibile"); SceneManager.changeScene("App"); return; }
    restaurants_names[i] = name;
    }

    restaurants_container.getItems().setAll(restaurants_names);
    checkSelected();
    }

    /**
     * Passa alla pagina precedente dei ristoranti.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    /**
     * Passa alla pagina successiva dei ristoranti.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    /**
     * Abilita o disabilita i pulsanti di modifica e recensioni
     * in base alla selezione corrente nella lista.
     */
    @FXML
    private void checkSelected() {
        int index = restaurants_container.getSelectionModel().getSelectedIndex();
        edit_btn.setDisable(index < 0);
        reviews_btn.setDisable(index < 0);
    }

    /**
     * Abilita o disabilita i pulsanti di modifica e recensioni
     * in base alla selezione corrente nella lista.
     */
    @FXML
    private void editSelected() throws IOException {
        int restaurant_id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(restaurant_id);
        SceneManager.changeScene("EditRestaurant");
    }

    /**
     * Visualizza le recensioni del ristorante selezionato.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void viewReviews() throws IOException {
        int restaurant_id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(restaurant_id);
        SceneManager.changeScene("RestaurantReviews");
    }

    /**
     * Esegue il logout dell'utente e torna alla schermata principale.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void logout() throws IOException {
        User.logout();
        SceneManager.changeScene("App");
    }
    
    /**
     * Passa alla schermata per aggiungere un nuovo ristorante.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void addRestaurant() throws IOException {
        SceneManager.changeScene("EditRestaurant");
    }
}