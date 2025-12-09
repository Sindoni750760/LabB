package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Controller della schermata che mostra tutte le recensioni lasciate dall'utente.
 *
 * <p>Funzionalità principali:</p>
 * <ul>
 *     <li>Visualizzare le recensioni dell'utente</li>
 *     <li>Supporto alla paginazione (pagina precedente/successiva)</li>
 *     <li>Rendering multilinea del contenuto della recensione</li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per la gestione del fallback UI
 * nel caso in cui la connessione al server non sia disponibile.</p>
 */

public class MyReviews implements OnlineChecker {
    /** Indice della pagina attualmente visualizzata. */
    private static int current_page;
    /** Numero totale di pagine disponibili, ricevuto dal server. */
    private static int total_pages;

    @FXML private ListView<String> reviews_listview;
    @FXML private Label no_reviews_label;
    @FXML private Label pages_label;
    @FXML private Button prev_btn, next_btn;

    /**
     * Inizializza la schermata e prepara l'interfaccia.
     *
     * <p>Passi principali:</p>
     * <ol>
     *     <li>Controlla connessione e autenticazione utente</li>
     *     <li>Interroga il server per ottenere il numero totale di pagine</li>
     *     <li>Mostra messaggi contestuali (nessuna recensione, errore, ecc.)</li>
     *     <li>Imposta la cell factory per consentire gestione multilinea</li>
     * </ol>
     *
     * @throws IOException se la scena deve essere cambiata o il server non risponde correttamente
     */
    @FXML
    private void initialize() throws IOException {
        if(!checkOnline()) return;
        if(!User.isLoggedIn()){
            SceneManager.setAppWarning("Devi effettuare il login per poter vedere le recensioni");
            SceneManager.changeScene("App");
            return;
        }
        ClientLogger.getInstance().info("MyReviews initialized");
        current_page = 0;

        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        if (!checkOnline()) {
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Il server non è raggiungibile");
            return;
        }

        Communicator.send("getMyReviewsPages");

        String res = Communicator.read();
        if (res == null) {
            fallback();
            return;
        }

        if (!res.equals("ok")) {
            ClientLogger.getInstance().warning("Server returned error for getMyReviewsPages");
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Errore dal server");
            return;
        }

        String pagesStr = Communicator.read();
        if (pagesStr == null) {
            fallback();
            return;
        }

        total_pages = Integer.parseInt(pagesStr);

        if (total_pages > 0)
            changePage(0);
        else {
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Non hai ancora recensioni");
        }

        reviews_listview.setCellFactory(lv -> new ListCell<String>() {
            {
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null)
                    setText(null);
                else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });
    }

    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se la scena "App" non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

     /**
     * Carica e mostra la pagina indicata di recensioni dell'utente.
     *
     * <p>Flusso logico:</p>
     * <ul>
     *     <li>Aggiorna il numero di pagina corrente</li>
     *     <li>Abilita/disabilita i pulsanti di paginazione</li>
     *     <li>Richiede al server i dati per la pagina richiesta</li>
     *     <li>Compone e formatta le recensioni in forma multilinea</li>
     * </ul>
     *
     * <p>Ogni recensione viene formattata in questo modo:</p>
     * <pre>
     * Nome ristorante: X
     * Valutazione: Y/5
     * Recensione: ZZZZZZZZ
     * </pre>
     *
     * @param page indice della pagina da caricare (0-based)
     * @throws IOException se la comunicazione col server non va a buon fine
     */
    private void changePage(int page) throws IOException {
        if (!checkOnline()) return;

        current_page = page;

        pages_label.setText((page + 1) + "/" + total_pages);

        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.send("getMyReviews");
        Communicator.send(Integer.toString(page));

        String sizeStr = Communicator.read();
        if (sizeStr == null) {
            fallback();
            return;
        }

        int size = Integer.parseInt(sizeStr);

        String[] reviews_compact = new String[size];

        for (int i = 0; i < size; i++) {
            String restaurant_name = Communicator.read();
            String stars = Communicator.read();
            String text = Communicator.read();

            if (restaurant_name == null || stars == null || text == null) {
                fallback();
                return;
            }

            reviews_compact[i] =
                    "Nome ristorante: " + restaurant_name +
                            "\nValutazione: " + stars + "/5" +
                            "\nRecensione: " + text;
        }

        reviews_listview.getItems().setAll(reviews_compact);
    }

    /**
     * Carica la pagina precedente di recensioni, se disponibile.
     *
     * @throws IOException se la comunicazione col server fallisce
     */
    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    /**
     * Carica la pagina successiva di recensioni, se disponibile.
     *
     * @throws IOException se la comunicazione col server fallisce
     */
    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

     /**
     * Restituisce la lista di nodi interattivi gestiti da {@link OnlineChecker},
     * utilizzati per disabilitare la UI quando il server risulta irraggiungibile.
     *
     * @return array contenente list view e pulsanti di navigazione
     */
    @Override
    public Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                reviews_listview, prev_btn, next_btn
        };
    }
}
