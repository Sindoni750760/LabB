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
 * Controller della vista di dettaglio delle recensioni di un ristorante.
 * <p>
 * Accorpa la gestione delle recensioni e delle risposte in quanto strettamente
 * legate alla stessa vista applicativa
 * </p>
 *
 * <p>Responsabilità principali:</p>
 * <ul>
 *     <li>Caricare e mostrare recensioni con supporto paginazione</li>
 *     <li>Gestire aggiunta/modifica recensioni lato utente</li>
 *     <li>Gestire inserimento o modifica risposta lato ristoratore</li>
 *     <li>Mostrare informazioni di intestazione (media recensioni, numero recensioni)</li>
 * </ul>
 *
 * <p>La classe gestisce inoltre logiche condizionali basate sul tipo di utente:</p>
 * <ul>
 *     <li>Utente normale:
 *         <ul>
 *             <li>Può sempre aggiungere/modificare la propria recensione</li>
 *         </ul>
 *     </li>
 *     <li>Ristoratore:
 *         <ul>
 *             <li>Può rispondere esclusivamente ad una recensione selezionata</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per:</p>
 * <ul>
 *     <li>Disattivare UI quando il server non risponde</li>
 *     <li>Tentare riconnessione automatica</li>
 *     <li>Fallback verso schermata principale in caso di persistenza offline</li>
 * </ul>
 */
public class RestaurantReviews implements OnlineChecker {

    /** Indica se l'utente è autenticato nell'applicazione. */
    private static boolean is_logged;
    /** Indica se l'utente loggato è ristoratore. */
    private static boolean is_restaurateur;
    /** Identificativi delle recensioni attualmente visualizzate. */
    private static String[] reviews_ids;
    /** Numero totale di pagine restituite dal server. */
    private static int total_pages;
    /** Pagina attualmente caricata. */
    private static int current_page;

    @FXML private Label no_reviews_label;
    @FXML private Label page_label;
    @FXML private Label reviews_label;
    @FXML private Label stars_label;
    @FXML private Button prev_btn;
    @FXML private Button next_btn;
    @FXML private Button add_review_btn;
    @FXML private ListView<String> reviews_listview;

    /**
     * Costruttore di default del controller {@code RestaurantReviews}.
     *
     * <p>Il costruttore non esegue inizializzazioni esplicite poiché
     * l'istanziazione del controller e l'iniezione dei campi {@code @FXML}
     * sono gestite automaticamente dal framework JavaFX tramite {@code FXMLLoader}.</p>
     */
    public RestaurantReviews() {
        super();
    }

    /**
     * Inizializza la schermata dei commenti caricando i dati relativi al ristorante.
     *
     * <p>Effettua le seguenti azioni:</p>
     * <ol>
     *     <li>Reset della UI</li>
     *     <li>Determina se l'utente è registrato e se è ristoratore</li>
     *     <li>Carica intestazione ristorante (media recensioni, numero recensioni)</li>
     *     <li>Interroga il server per numero totale pagine</li>
     *     <li>Carica e renderizza la prima pagina</li>
     *     <li>Configura celle della lista per supporto multilinea</li>
     * </ol>
     *
     * @throws IOException se si verifica errore durante il recupero dati o cambio scena
     */
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

                if (is_logged && !is_restaurateur)
                    add_review_btn.setDisable(false);
            }

        } catch (Exception e) {
            ClientLogger.getInstance().error("RestaurantReviews initialization error: " + e.getMessage());
            fallback();
        }

        setupListCellFactory();
    }


    /**
     * Imposta il comportamento dell’interfaccia in funzione:
     * <ul>
     *     <li>dell'identità dell'utente</li>
     *     <li>del ruolo (ristoratore o utente normale)</li>
     *     <li>dell’eventuale recensione già inserita</li>
     * </ul>
     *
     * <p>Viene anche aggiornato il testo del pulsante principale.</p>
     *
     * @throws IOException se la comunicazione verso il server fallisce
     */
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
            add_review_btn.setText("Rispondi / Modifica risposta");
            add_review_btn.setDisable(true);
            return;
        }

        if (!checkOnline()) return;

        Communicator.send("getMyReview");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String starsStr = Communicator.read();
        if (starsStr == null) { fallback(); return; }

        int stars = Integer.parseInt(starsStr);
        Communicator.read();

        if (stars > 0)
            add_review_btn.setText("Modifica recensione");
        else
            add_review_btn.setText("Aggiungi recensione");

        add_review_btn.setDisable(false);
    }


      // =============================== HEADER ================================
    /**
     * Carica nell'interfaccia le informazioni generali del ristorante:
     * <ul>
     *     <li>numero totale recensioni</li>
     *     <li>media valutazioni</li>
     * </ul>
     */

    private void loadRestaurantHeader() {
        String[] info = EditingRestaurant.getInfo();

        reviews_label.setText("Recensioni: " + info[10]);

        String avg = info[9];
        stars_label.setText(
                avg.equals("0") ? "Valutazione media: -" : ("Valutazione media: " + avg + "/5")
        );
    }


      // ===================== PAGINAZIONE – COUNT PAGINE ======================

    /**
     * Recupera dal server il numero totale di pagine disponibili per le recensioni del ristorante.
     *
     * @throws IOException se il server non risponde correttamente
     */
    private void loadTotalPages() throws IOException {
        if(!checkOnline()) return;
        Communicator.send("getReviewsPages");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String pagesStr = Communicator.read();
        if (pagesStr == null) { fallback(); return; }

        total_pages = Integer.parseInt(pagesStr);
    }


 // ============================= PAGINAZIONE ==============================

    /**
     * Carica una specifica pagina di recensioni per il ristorante corrente.
     *
     * <p>Flusso interno:</p>
     * <ol>
     *     <li>Aggiorna pagina corrente e UI paginazione</li>
     *     <li>Interroga il server col protocollo "getReviews"</li>
     *     <li>Legge ID, testo, stelle e risposta del ristoratore</li>
     *     <li>Formatta il risultato per visualizzazione multilinea</li>
     * </ol>
     *
     * @param page indice pagina richiesta
     * @throws IOException se la comunicazione con il server fallisce
     */

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

        String[] formatted = new String[size];
        for (int i = 0; i < size; i++)
            formatted[i] = stars[i] + "/5 " + texts[i] +
                    (replies[i] != null ? "\nRisposta: " + replies[i] : "");

        reviews_listview.getItems().setAll(formatted);

        if (is_logged && !is_restaurateur)
            add_review_btn.setDisable(false);
    }


    // ============================== EVENTI UI ===============================

    /**
     * Carica la pagina precedente rispetto a quella corrente.
     *
     * @throws IOException se il server non risponde
     */
    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    /**
     * Carica la pagina successiva rispetto a quella corrente.
     *
     * @throws IOException se il server non risponde
     */
    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    /**
     * Permette:
     * <ul>
     *     <li>all'utente di scrivere o modificare la propria recensione</li>
     *     <li>al ristoratore di rispondere/modificare una risposta</li>
     * </ul>
     *
     * @throws IOException se la scena successiva non è caricabile
     */
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

    /**
     * Aggiorna l’abilitazione del pulsante <i>Rispondi/Modifica</i>
     * in base alla recensione selezionata.
     */
    @FXML
    private void checkSelected() {
        if (is_restaurateur) {
            add_review_btn.setDisable(reviews_listview.getSelectionModel().getSelectedIndex() < 0);
        }
    }

    /**
     * Torna alla schermata precedente in modo coerente al ruolo dell’utente:
     * <ul>
     *     <li>ristoratore → pagina "MyRestaurants"</li>
     *     <li>utente normale → pagina "ViewRestaurantInfo"</li>
     *     <li>utente guest → pagina lista ristoranti</li>
     * </ul>
     *
     * @throws IOException se il cambio scena fallisce
    */
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


    // =============================== ONLINE CHECKER ==========================

    /**
     * Restituisce i nodi UI gestibili da {@link OnlineChecker}.
     * <p>
     * Verranno disattivati durante fallback e riattivati alla riconnessione.
     * </p>
     *
     * @return lista nodi interattivi
    */
    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                prev_btn, next_btn, add_review_btn, reviews_listview
        };
    }


    // ======================= FORMATTAZIONE LIST VIEW ========================

    /**
     * Configura le celle della {@link ListView} affinché:
     * <ul>
     *     <li>supportino testo multilinea</li>
     *     <li>adattino l’altezza al contenuto</li>
     *     <li>non ritaglino testo interno</li>
     * </ul>
     */

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
