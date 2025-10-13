package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Controller per la schermata delle recensioni di un ristorante.
 * Gestisce la visualizzazione delle recensioni, la paginazione,
 * e consente agli utenti di aggiungere o modificare la propria recensione.
 * I ristoratori possono rispondere alle recensioni ricevute.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class RestaurantReviews {
    /** Indica se l'utente è loggato. */
    private static boolean is_logged;

    /** Indica se l'utente loggato è un ristoratore. */
    private static boolean is_restaurateur;

    /** Array contenente gli ID delle recensioni visualizzate. */
    private static String[] reviews_ids;

    /** Numero totale di pagine disponibili per le recensioni. */
    private static int total_pages;

    /** Pagina attualmente visualizzata. */
    private static int current_page;
    /** Etichetta mostrata se non ci sono recensioni disponibili. */
    @FXML private Label no_reviews_label;

    /** Etichetta che mostra la pagina corrente e il totale delle pagine. */
    @FXML private Label page_label;

    /** Etichetta che mostra il numero totale di recensioni ricevute. */
    @FXML private Label reviews_label;

    /** Etichetta che mostra la valutazione media in stelle. */
    @FXML private Label stars_label;

    /** Pulsante per passare alla pagina precedente. */
    @FXML private Button prev_btn;

    /** Pulsante per passare alla pagina successiva. */
    @FXML private Button next_btn;

    /** Pulsante per aggiungere o modificare una recensione. */
    @FXML private Button add_review_btn;

    /** ListView che mostra le recensioni in formato compatto. */
    @FXML private ListView<String> reviews_listview;

    /**
     * Inizializza la schermata delle recensioni.
     * Determina il ruolo dell'utente, imposta il comportamento del pulsante di recensione,
     * e carica le recensioni del ristorante selezionato.
     *
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */
    @FXML
    private void initialize() throws IOException {
        // Make initialization resilient: if any communication fails, keep UI responsive and allow user to go back
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        no_reviews_label.setVisible(false);
        current_page = 0;

        try {
            String[] user_info = User.getInfo();
            is_logged = user_info != null;

            if(is_logged) {
                add_review_btn.setVisible(true);
                is_restaurateur = user_info[2].equals("y");

                if(is_restaurateur) {
                    add_review_btn.setDisable(true);
                    add_review_btn.setText("Rispondi/modifica risposta");
                } else {
                    //checks if the user has set a review
                    Communicator.sendStream("getMyReview");
                    Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));

                    String starsStr = Communicator.readStream();
                    if (starsStr == null) throw new IOException("server unreachable");
                    int stars = Integer.parseInt(starsStr);
                    if (Communicator.readStream() == null) throw new IOException("server unreachable");

                    if(stars > 0)
                        add_review_btn.setText("Modifica recensione");
                }
            }

            String[] restaurant_info = EditingRestaurant.getInfo();
            reviews_label.setText("Recensioni ricevute: " + restaurant_info[10]);
            String stars_text = restaurant_info[9];
            stars_label.setText("Valutazione media (stelle): " + (stars_text.equals("0") ? "-" : stars_text));

            Communicator.sendStream("getReviewsPages");
            Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
            String totalPagesStr = Communicator.readStream();
            if (totalPagesStr == null) throw new IOException("server unreachable");
            total_pages = Integer.parseInt(totalPagesStr);

            if(total_pages > 0)
                changePage(0);
            else
                no_reviews_label.setVisible(true);
        } catch(Exception e) {
            // log and keep UI responsive; user can still go back to previous scene
            System.err.println("[RestaurantReviews] init error: " + e.getMessage());
            no_reviews_label.setVisible(true);
            add_review_btn.setVisible(false);
        }

        //function used to wrap the text for every cell of the listview
        reviews_listview.setCellFactory(lv -> new ListCell<String>() {
            {
                setPrefWidth(0); // forces the cell to size itself based on the ListView
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true); // the magic line
                }
            }
        });
    }

    /**
     * Cambia la pagina visualizzata delle recensioni.
     * Aggiorna la lista e lo stato dei pulsanti di navigazione.
     *
     * @param page numero della pagina da visualizzare
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */    
    private void changePage(int page) throws IOException {
        page_label.setText(Integer.toString(page + 1) + '/' + total_pages);
        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        
        Communicator.sendStream("getReviews");
        Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
        Communicator.sendStream(Integer.toString(page));

    String sizeStr = Communicator.readStream();
    if (sizeStr == null) { SceneManager.setAppWarning("Il server non è raggiungibile"); SceneManager.changeScene("App"); return; }
    int size = Integer.parseInt(sizeStr);
        String[] reviews_stars = new String[size];
        String[] reviews_texts = new String[size];
        String[] reviews_reply = new String[size];
        reviews_ids = new String[size];

        for(int i = 0; i < size; i++) {
            reviews_ids[i] = Communicator.readStream();
            reviews_stars[i] = Communicator.readStream();
            reviews_texts[i] = Communicator.readStream();

            if(Communicator.readStream().equals("y"))
                reviews_reply[i] = Communicator.readStream();
            else
                reviews_reply[i] = null;
        }

        String[] review_compact = new String[size];
        for(int i = 0; i < size; i++)
            review_compact[i] = reviews_stars[i] + "/5 " + reviews_texts[i] + (reviews_reply[i] == null ? "" : "\nRisposta del ristoratore: " + reviews_reply[i]);
        
        reviews_listview.getItems().setAll(review_compact);
    }

    /**
     * Passa alla pagina precedente delle recensioni.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    /**
     * Passa alla pagina successiva delle recensioni.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    /**
     * Apre la schermata per scrivere o modificare una recensione.
     * Se l'utente è un ristoratore, imposta l'ID della recensione da gestire.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void addReview() throws IOException {
        if(is_restaurateur) {
            //sets the id of the review to reply to (restaurator)
            int review_id = Integer.parseInt(reviews_ids[reviews_listview.getSelectionModel().getSelectedIndex()]);
            EditingRestaurant.setReviewId(review_id);
        }
        SceneManager.changeScene("WriteReview");
    }

    /**
     * Abilita o disabilita il pulsante di recensione in base alla selezione corrente.
     */
    @FXML
    private void checkSelected() {
        add_review_btn.setDisable(reviews_listview.getSelectionModel().getSelectedIndex() < 0);
    }

    /**
     * Torna alla schermata precedente in base al ruolo dell'utente.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        try {
            //changes page based on the role
            if(!is_logged){
                SceneManager.changeScene("ViewRestaurants");
            }
            else if(is_restaurateur)
                SceneManager.changeScene("MyRestaurants");
            else
                SceneManager.changeScene("ViewRestaurantInfo");
        } catch(IOException e) {
            // fallback to main restaurants view to ensure user can always navigate away
            SceneManager.changeScene("ViewRestaurants");
        }
    }
}