package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.Node;

/**
 * Controller della schermata dedicata alla ricerca e visualizzazione dei ristoranti.
 *
 * <p>Funzionalità principali:</p>
 * <ul>
 *     <li>Ricerca ristoranti tramite location, coordinate, categoria, range di prezzo e servizi</li>
 *     <li>Supporto alla paginazione dei risultati</li>
 *     <li>Possibilità di visualizzare la pagina informativa di un ristorante selezionato</li>
 *     <li>Gestione automatica di fallback e riconnessione tramite {@link OnlineChecker}</li>
 * </ul>
 *
 * <p>Il controller utilizza il protocollo <b>getRestaurants</b>,
 * inviando 13 parametri al server per filtrare i risultati.</p>
 */
public class ViewRestaurants implements OnlineChecker {
    /** Identificativi dei ristoranti attualmente caricati. */
    private String[] restaurants_ids;
    /** Nomi corrispondenti agli ID caricati. */
    private String[] restaurants_names;
    /** Numero massimo di pagine restituito dal server. */
    private int pages = 0;
    /** Pagina attualmente visualizzata.*/
    private int current_page = 0;
    /** Modalità di ricerca corrente ("coordinates", "location", "all", "invalid"). */
    private String searchMode = "invalid";
    /** Campo numerico o testuale usato come primo parametro di ricerca. */
    private String field1 = "-";
    /** Campo numerico o testuale usato come secondo parametro di ricerca. */
    private String field2 = "-";

    @FXML private TextField field1_input;
    @FXML private TextField field2_input;
    @FXML private TextField range_field;
    @FXML private TextField price_min_field;
    @FXML private TextField price_max_field;
    @FXML private TextField stars_min_field;
    @FXML private TextField stars_max_field;
    @FXML private TextField category_field;

    @FXML private CheckBox delivery_check;
    @FXML private CheckBox online_check;

    @FXML private ListView<String> restaurants_listview;

    @FXML private Label notification_label;
    @FXML private Label no_restaurants_label;
    @FXML private Label pages_label;

    @FXML private Button prev_btn;
    @FXML private Button next_btn;
    @FXML private Button view_info_btn;
    @FXML private Button clear_btn;

    /**
     * Costruttore di default del controller {@code ViewRestaurants}.
     *
     * <p>Il costruttore non esegue inizializzazioni esplicite poiché
     * l'istanziazione del controller e l'iniezione dei campi {@code @FXML}
     * sono gestite automaticamente dal framework JavaFX tramite {@code FXMLLoader}.</p>
     */
    public ViewRestaurants() {
        super();
    }

    /**
     * Inizializza l'interfaccia ripristinando lo stato grafico
     * e avvia automaticamente il caricamento iniziale dei ristoranti.
    */
    @FXML
    private void initialize() {
        ClientLogger.getInstance().info("ViewRestaurants initialized");

        EditingRestaurant.reset();

        pages_label.setText("-/-");
        no_restaurants_label.setVisible(false);
        view_info_btn.setDisable(true);
        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        restaurants_listview.getSelectionModel()
            .selectedIndexProperty()
            .addListener((obs, oldVal, newVal) -> checkSelected());
        clearFilters();
    }

    /**
     * Analizza i campi immessi dall'utente e determina il tipo di ricerca:
     * <ul>
     *     <li>coordinate → se entrambi i campi sono numerici</li>
     *     <li>location → se entrambi sono stringhe testuali</li>
     *     <li>invalid → se misti o mancanti</li>
     * </ul>
     *
     * Imposta inoltre {@link #field1} e {@link #field2}.
     */
    private void detectSearchMode() {
        String f1 = field1_input.getText().trim();
        String f2 = field2_input.getText().trim();

        if (f1.isEmpty() || f2.isEmpty()) {
            searchMode = "invalid";
            return;
        }

        boolean f1Double = f1.matches("[-+]?[0-9]*\\.?[0-9]+");
        boolean f2Double = f2.matches("[-+]?[0-9]*\\.?[0-9]+");

        if (f1Double && f2Double) {
            searchMode = "coordinates";
        } else if (!f1Double && !f2Double) {
            searchMode = "location";
        } else {
            searchMode = "invalid";
        }

        field1 = f1;
        field2 = f2;
    }

    /**
     * Aggiorna i filtri applicando la logica di ricerca
     * e, se valida, carica la pagina iniziale dei risultati.
     *
     * @throws IOException errore di comunicazione con il server
    */
    @FXML
    private void updateFilters() throws IOException {
        hideNotification();

        detectSearchMode();

        if (searchMode.equals("invalid")) {
            setNotification("I due campi devono essere entrambi numeri (coordinate) oppure entrambi testo (nazione + città).");
            return;
        }

        searchPage(0);
    }

    /**
     * Effettua una ricerca lato server e aggiorna la pagina richiesta.
     *
     * <p>Protocollo server:</p>
     * <pre>
     * getRestaurants
     * ├ page (int)
     * ├ searchMode ("all" | "coordinates" | "location")
     * ├ firstValue
     * ├ secondValue
     * ├ range
     * ├ priceMin
     * ├ priceMax
     * ├ category
     * ├ deliveryFlag y/n
     * ├ onlineFlag y/n
     * ├ starsMin
     * ├ starsMax
     * └ onlyFav = "n"
     * </pre>
     *
     * @param page pagina da caricare
     * @throws IOException se la comunicazione col server fallisce
     */
    private void searchPage(int page) throws IOException {
        if (!checkOnline()) return;

        restaurants_listview.getItems().clear();
        no_restaurants_label.setVisible(false);
        pages_label.setText("-/-");
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        current_page = page;

        Communicator.send("getRestaurants");
        Communicator.send(Integer.toString(page));

        Communicator.send(searchMode);
        Communicator.send(field1);
        Communicator.send(field2);

        String range = range_field.getText().trim();
        Communicator.send(range.isEmpty() ? "-" : range);

        Communicator.send(price_min_field.getText().trim().isEmpty() ? "-" : price_min_field.getText().trim());
        Communicator.send(price_max_field.getText().trim().isEmpty() ? "-" : price_max_field.getText().trim());

        String cat = category_field.getText().trim();
        Communicator.send(cat.isEmpty() ? "-" : cat);

        Communicator.send(delivery_check.isSelected() ? "y" : "n");
        Communicator.send(online_check.isSelected() ? "y" : "n");

        Communicator.send(stars_min_field.getText().trim().isEmpty() ? "-" : stars_min_field.getText().trim());
        Communicator.send(stars_max_field.getText().trim().isEmpty() ? "-" : stars_max_field.getText().trim());

        Communicator.send("n");

        String response = Communicator.read();
        if (response == null) {
            fallback();
            return;
        }

        switch (response) {

            case "ok" -> {
                String pagesStr = Communicator.read();
                pages = Integer.parseInt(pagesStr);

                String sizeStr = Communicator.read();
                int size = Integer.parseInt(sizeStr);

                if (pages == 0 || size == 0) {
                    no_restaurants_label.setVisible(true);
                    return;
                }

                restaurants_ids   = new String[size];
                restaurants_names = new String[size];

                for (int i = 0; i < size; i++) {
                    restaurants_ids[i] = Communicator.read();
                    restaurants_names[i] = Communicator.read();
                }

                restaurants_listview.getItems().setAll(restaurants_names);

                pages_label.setText((page + 1) + "/" + pages);

                if (page > 0)         prev_btn.setDisable(false);
                if (page + 1 < pages) next_btn.setDisable(false);

                checkSelected();
            }

            case "invalid" -> setNotification("I due campi devono essere coerenti (entrambi testo o entrambi numeri)");
            case "coordinates" -> setNotification("Coordinate o raggio non validi.");
            case "price" -> setNotification("Range di prezzo non valido.");
            case "stars" -> setNotification("Range stelle non valido.");

            default -> setNotification("Errore: " + response);
        }
    }

    /**
     * Torna alla pagina precedente se disponibile.
     *
     * @throws IOException errore comunicazione server
     */
    @FXML
    private void prevPage() throws IOException {
        if (current_page > 0) {
            searchPage(--current_page);
        }
    }

    /**
     * Avanza alla pagina successiva se disponibile.
     *
     * @throws IOException errore comunicazione server
    */
    @FXML
    private void nextPage() throws IOException {
        if (current_page + 1 < pages) {
            searchPage(++current_page);
        }
    }

    /**
     * Aggiorna lo stato del bottone "Visualizza info"
     * in base alla selezione corrente.
    */
    @FXML
    private void checkSelected() {
        view_info_btn.setDisable(
                restaurants_listview.getSelectionModel().getSelectedIndex() < 0
        );
    }

    /**
     * Apre la schermata informativa del ristorante selezionato,
     * impostando l'ID relativo nel contesto applicativo.
     *
     * @throws IOException errore caricamento scena successiva
     */
    @FXML
    private void viewRestaurantInfo() throws IOException {
        int index = restaurants_listview.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        EditingRestaurant.setEditing(Integer.parseInt(restaurants_ids[index]));
        SceneManager.setPreviousNavigation("ViewRestaurants");
        SceneManager.changeScene("ViewRestaurantInfo");
    }

    /**
     * Ripristina valori e filtri alla configurazione iniziale
     * e ricarica l'intera lista dei ristoranti (senza filtri).
     *
     * <p>Effetti:</p>
     * <ul>
     *     <li>reset grafico</li>
     *     <li>reset filtri logici</li>
     *     <li>aggiornamento UI</li>
     * </ul>
     */
    @FXML
    private void clearFilters() {
        hideNotification();

        field1_input.clear();
        field2_input.clear();
        field1 = "-";
        field2 = "-";
        
        range_field.clear();

        price_min_field.clear();
        price_max_field.clear();

        stars_min_field.clear();
        stars_max_field.clear();

        category_field.clear();

        delivery_check.setSelected(false);
        online_check.setSelected(false);

        no_restaurants_label.setVisible(false);
        restaurants_listview.getItems().clear();
        pages_label.setText("-/-");
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        view_info_btn.setDisable(true);

        searchMode = "all";
        pages = 0;
        current_page = 0;

        loadAllRestaurants();
    }

    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException errore nella navigazione scena
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    /**
     * Visualizza una notifica temporanea nella UI.
     *
     * @param msg testo della notifica
     */
    private void setNotification(String msg) {
        notification_label.setText(msg);
        notification_label.setVisible(true);
    }

    /**
     * Nasconde eventuali notifiche UI attive.
     */
    private void hideNotification() {
        notification_label.setVisible(false);
    }

    /**
     * Restituisce tutti i nodi sensibili allo stato di connessione
     * che verranno disabilitati automaticamente in caso di offline.
     *
     * @return array di nodi UI
     */
    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                field1_input, field2_input, range_field,
                price_min_field, price_max_field,
                stars_min_field, stars_max_field,
                category_field,
                delivery_check, online_check,
                restaurants_listview,
                prev_btn, next_btn, view_info_btn, clear_btn
        };
    }

    /**
     * Richiede al server la lista completa dei ristoranti ignorando i filtri
     * e avvia la prima pagina dei risultati.
     *
     * <p>Usato durante il reset dei filtri tramite {@link #clearFilters()}.</p>
     */
    private void loadAllRestaurants() {
        try {
            searchMode = "all";
            field1 = "-";
            field2 = "-";
            searchPage(0);
        } catch (Exception e) {
            ClientLogger.getInstance().error("Failed to load all restaurants");
            setNotification("Errore di connessione al server.");
        }
    }
}
