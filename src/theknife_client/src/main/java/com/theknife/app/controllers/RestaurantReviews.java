package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Controller per la schermata delle recensioni di un ristorante.
 * Gestisce la visualizzazione delle recensioni, la paginazione,
 * e consente agli utenti di aggiungere/modificare la propria recensione.
 * I ristoratori possono rispondere alle recensioni ricevute.
 *
 * Implementa OnlineChecker per fallback e riconnessione.
 */
public class RestaurantReviews implements OnlineChecker {

    private static boolean is_logged;
    private static boolean is_restaurateur;

    private static String[] reviews_ids;

    private static int total_pages;
    private static int current_page;

    @FXML private Label no_reviews_label;
    @FXML private Label page_label;
    @FXML private Label reviews_label;
    @FXML private Label stars_label;
    @FXML private Button prev_btn;
    @FXML private Button next_btn;
    @FXML private Button add_review_btn;
    @FXML private ListView<String> reviews_listview;

    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("RestaurantReviews initialized for restaurant: " + EditingRestaurant.getId());

        // Reset UI
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        add_review_btn.setDisable(true);
        no_reviews_label.setVisible(false);
        current_page = 0;

        try {
            setupUserMode();
            loadRestaurantHeader();

            if (!checkOnline()) return;

            loadTotalPages();

            if (total_pages > 0) {
                changePage(0);
            } else {
                no_reviews_label.setVisible(true);

                // ⭐ Utente normale può sempre recensire
                if (is_logged && !is_restaurateur)
                    add_review_btn.setDisable(false);
            }

        } catch (Exception e) {
            ClientLogger.getInstance().error("RestaurantReviews initialization error: " + e.getMessage());
            fallback();
        }

        setupListCellFactory();
    }


    /* =====================================================================
       UTENTE / RISTORATORE -> LOGICA DI VISIBILITÀ E TESTI DEL BOTTONE
       ===================================================================== */

    private void setupUserMode() throws IOException {
        String[] user_info = User.getInfo();
        is_logged = user_info != null;

        if (!is_logged) {
            add_review_btn.setVisible(false);
            return;
        }

        add_review_btn.setVisible(true);
        is_restaurateur = user_info[2].equals("y");

        if (is_restaurateur) {
            // Il ristoratore può rispondere SOLO se seleziona una recensione
            add_review_btn.setText("Rispondi / Modifica risposta");
            add_review_btn.setDisable(true);
            return;
        }

        // ---- UTENTE NORMALE ----
        if (!checkOnline()) return;

        Communicator.send("getMyReview");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String starsStr = Communicator.read();
        if (starsStr == null) { fallback(); return; }

        int stars = Integer.parseInt(starsStr);
        Communicator.read(); // scarto testo o marker

        if (stars > 0)
            add_review_btn.setText("Modifica recensione");
        else
            add_review_btn.setText("Aggiungi recensione");

        // ⭐ Utente normale: bottone sempre abilitato
        add_review_btn.setDisable(false);
    }


    /* =====================================================================
                                    HEADER
       ===================================================================== */

    private void loadRestaurantHeader() {
        String[] info = EditingRestaurant.getInfo();

        reviews_label.setText("Recensioni: " + info[10]);

        String avg = info[9];
        stars_label.setText(
                avg.equals("0") ? "Valutazione media: -" : ("Valutazione media: " + avg + "/5")
        );
    }


    /* =====================================================================
                            LETTURA NUMERO PAGINE
       ===================================================================== */

    private void loadTotalPages() throws IOException {
        if(!checkOnline()) return;
        Communicator.send("getReviewsPages");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String pagesStr = Communicator.read();
        if (pagesStr == null) { fallback(); return; }

        total_pages = Integer.parseInt(pagesStr);
    }


    /* =====================================================================
                             CARICAMENTO PAGINA RECENSIONI
       ===================================================================== */

    private void changePage(int page) throws IOException {
        if (!checkOnline()) return;

        current_page = page;
        page_label.setText((page + 1) + "/" + total_pages);

        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.send("getReviews");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));
        Communicator.send(Integer.toString(page));

        String sizeStr = Communicator.read();
        if (sizeStr == null) { fallback(); return; }

        int size = Integer.parseInt(sizeStr);

        reviews_ids = new String[size];
        String[] stars = new String[size];
        String[] texts = new String[size];
        String[] replies = new String[size];

        for (int i = 0; i < size; i++) {
            reviews_ids[i] = Communicator.read();
            stars[i] = Communicator.read();
            texts[i] = Communicator.read();

            if (reviews_ids[i] == null || stars[i] == null || texts[i] == null) {
                fallback();
                return;
            }

            String hasReply = Communicator.read();
            if (hasReply == null) { fallback(); return; }

            if (hasReply.equals("y")) {
                replies[i] = Communicator.read();
                if (replies[i] == null) { fallback(); return; }
            }
        }

        // Formatting
        String[] formatted = new String[size];
        for (int i = 0; i < size; i++)
            formatted[i] = stars[i] + "/5 " + texts[i] +
                    (replies[i] != null ? "\nRisposta: " + replies[i] : "");

        reviews_listview.getItems().setAll(formatted);

        // ⭐ Utente normale = bottone sempre attivo
        if (is_logged && !is_restaurateur)
            add_review_btn.setDisable(false);
    }


    /* =====================================================================
                                    EVENTI UI
       ===================================================================== */

    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    @FXML
    private void addReview() throws IOException {
        if (!checkOnline()) return;
        if (is_restaurateur) {
            int index = reviews_listview.getSelectionModel().getSelectedIndex();
            if (index >= 0)
                EditingRestaurant.setReviewId(Integer.parseInt(reviews_ids[index]));
        }

        SceneManager.changeScene("WriteReview");
    }

    @FXML
    private void checkSelected() {
        if (is_restaurateur) {
            // Il ristoratore può rispondere solo a una recensione selezionata
            add_review_btn.setDisable(reviews_listview.getSelectionModel().getSelectedIndex() < 0);
        }
        // UTENTE NORMALE -> NON disabilitare il bottone!
    }

    @FXML
    private void goBack() throws IOException {
        if (!is_logged) {
            SceneManager.changeScene("ViewRestaurants");
            return;
        }

        if (is_restaurateur)
            SceneManager.changeScene("MyRestaurants");
        else
            SceneManager.changeScene("ViewRestaurantInfo");
    }


    /* =====================================================================
                                  ONLINE CHECKER
       ===================================================================== */

    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                prev_btn, next_btn, add_review_btn, reviews_listview
        };
    }


    /* =====================================================================
                                   LIST VIEW FORMAT
       ===================================================================== */

    private void setupListCellFactory() {
        reviews_listview.setCellFactory(lv -> new ListCell<String>() {
            {
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setWrapText(true);
            }
        });
    }
}
