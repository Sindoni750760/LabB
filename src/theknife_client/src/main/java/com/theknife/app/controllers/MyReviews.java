package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Controller per la schermata "MyReviews".
 * Gestisce la visualizzazione delle recensioni lasciate dall'utente,
 * con supporto alla paginazione e formattazione del testo.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class MyReviews {
    /** Pagina attualmente visualizzata. */
    private static int current_page, 
    /** Numero totale di pagine disponibili. */
    total_pages;
    /** ListView che mostra le recensioni dell'utente. */
    @FXML
    private ListView<String> reviews_listview;
    /** Etichetta mostrata se non ci sono recensioni o in caso di errore. */
    @FXML
    private Label no_reviews_label, 
    /** Etichetta che mostra la pagina corrente e il totale delle pagine. */
    pages_label;
    /** Pulsante per passare alla pagina precedente. */
    @FXML
    private Button prev_btn, 
    /** Pulsante per passare alla pagina successiva. */
    next_btn;

    /**
     * Inizializza la schermata delle recensioni.
     * Recupera il numero di pagine disponibili e imposta la visualizzazione iniziale.
     * Applica il wrapping del testo alle celle della ListView.
     *
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */
    @FXML
    private void initialize() throws IOException {
        current_page = total_pages = 0;
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        Communicator.sendStream("getMyReviewsPages");
        if(Communicator.readStream().equals("ok")) {
            total_pages = Integer.parseInt(Communicator.readStream());
            changePage(0);
        } else {
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Errore nel server");
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
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    /**
     * Cambia la pagina visualizzata delle recensioni.
     * Aggiorna la lista e lo stato dei pulsanti di navigazione.
     *
     * @param page numero della pagina da visualizzare
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */
    private void changePage(int page) throws IOException {
        pages_label.setText(Integer.toString(page + 1) + "/" + Integer.toString(total_pages));
        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.sendStream("getMyReviews");
        Communicator.sendStream(Integer.toString(page));
        int size = Integer.parseInt(Communicator.readStream());

        String[] reviews_compact = new String[size];

        for(int i = 0; i < size; i++) {
            String restaurant_name = Communicator.readStream();
            String given_stars = Communicator.readStream();
            String review_text = Communicator.readStream();
            reviews_compact[i] = "Nome ristorante: " + restaurant_name + "\nValutazione: " + given_stars + "/5\nRecensione: " + review_text;
        }

        reviews_listview.getItems().setAll(reviews_compact);
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
}