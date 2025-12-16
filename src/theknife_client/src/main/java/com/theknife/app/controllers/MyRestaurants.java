package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controller della schermata dedicata alla gestione dei ristoranti dell'utente.
 *
 * <p>Consente di:</p>
 * <ul>
 *     <li>Visualizzare i ristoranti registrati dall’utente</li>
 *     <li>Navigare le pagine di ristoranti associati</li>
 *     <li>Modificare un ristorante selezionato</li>
 *     <li>Accedere alle recensioni del ristorante</li>
 *     <li>Effettuare il logout</li>
 *     <li>Aggiungere un nuovo ristorante</li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per la gestione unificata del fallback
 * quando il server non è raggiungibile o risponde in modo incoerente.</p>
 */
public class MyRestaurants implements OnlineChecker {

    @FXML private ListView<String> restaurants_container;
    @FXML private Label no_restaurants_label;
    @FXML private Label page_label;
    @FXML private Button edit_btn, reviews_btn, prev_btn, next_btn;
    
    /** Identificativi dei ristoranti caricati dalla pagina corrente. */
    private int[] restaurants_ids;
    /** Nomi dei ristoranti corrispondenti agli ID nella pagina corrente. */
    private String[] restaurants_names;
    /** Numero totale di pagine ricevuto dal server. */
    private int total_pages;
    /** Indice della pagina corrente, inizializzato a 0. */
    private int current_page = 0;

    /**
     * Costruttore di default del controller {@code MyRestaurants}.
     *
     * <p>Il costruttore non esegue inizializzazioni esplicite poiché
     * l'istanziazione del controller e l'iniezione dei campi {@code @FXML}
     * sono gestite automaticamente dal framework JavaFX tramite {@code FXMLLoader}.</p>
     */
    public MyRestaurants() {
        super();
    }

    /**
     * Inizializza la schermata caricando le pagine disponibili
     * e navigando alla prima, se presente.
     *
     * <p>Flusso logico:</p>
     * <ul>
     *     <li>Reset dell'editing del ristorante</li>
     *     <li>Disabilitazione della navigazione pagine</li>
     *     <li>Verifica connessione</li>
     *     <li>Lettura del numero totale di pagine</li>
     *     <li>Caricamento pagina iniziale se disponibile</li>
     * </ul>
     *
     * @throws IOException se la comunicazione con il server non va a buon fine
     */
    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("MyRestaurants initialized");
        EditingRestaurant.reset();

        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        if (!checkOnline()) {
            no_restaurants_label.setVisible(true);
            no_restaurants_label.setText("Il server non è raggiungibile");
            return;
        }

        Communicator.send("getMyRestaurantsPages");
        String pagesStr = Communicator.read();
        if (pagesStr == null) {
            fallback();
            return;
        }

        total_pages = Integer.parseInt(pagesStr);

        if (total_pages > 0)
            changePage(0);
        else
            no_restaurants_label.setVisible(true);
    }

    /**
     * Cambia pagina e ricarica i ristoranti dell’utente.
     *
     * @param page pagina da caricare (indice inizializzato a 0)
     *
     * @throws IOException se la comunicazione con il server fallisce
     */
    private void changePage(int page) throws IOException {
        if (!checkOnline()) return;
        

        current_page = page;

        page_label.setText((page + 1) + "/" + total_pages);

        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.send("getMyRestaurants");
        Communicator.send(Integer.toString(page));

        String sizeStr = Communicator.read();
        if (sizeStr == null) {
            fallback();
            return;
        }

        int size = Integer.parseInt(sizeStr);

        restaurants_ids = new int[size];
        restaurants_names = new String[size];

        for (int i = 0; i < size; i++) {
            String idStr = Communicator.read();
            String nameStr = Communicator.read();

            if (idStr == null || nameStr == null) {
                fallback();
                return;
            }

            restaurants_ids[i] = Integer.parseInt(idStr);
            restaurants_names[i] = nameStr;
        }

        restaurants_container.getItems().setAll(restaurants_names);
        checkSelected();
    }

    /**
     * Naviga alla pagina precedente, se disponibile.
     *
     * @throws IOException se la comunicazione con il server fallisce
     */
    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    /**
     * Naviga alla pagina successiva, se disponibile.
     *
     * @throws IOException se la comunicazione con il server fallisce
     */
    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    /**
     * Aggiorna lo stato dei pulsanti relativi all'elemento selezionato.
     *
     * <p>Disabilita i pulsanti di modifica e recensioni
     * quando non è selezionato alcun ristorante.</p>
     */
    @FXML
    private void checkSelected() {
        int index = restaurants_container.getSelectionModel().getSelectedIndex();
        boolean disable = index < 0;

        edit_btn.setDisable(disable);
        reviews_btn.setDisable(disable);
    }

    /**
     * Apre la schermata di modifica per il ristorante selezionato.
     *
     * @throws IOException se la nuova scena non può essere caricata
     */
    @FXML
    private void editSelected() throws IOException {
        if (!checkOnline()) return;
        int id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(id);
        SceneManager.changeScene("EditRestaurant");
    }

    /**
     * Apre la schermata che mostra le recensioni del ristorante selezionato.
     *
     * @throws IOException se la nuova scena non può essere caricata
     */
    @FXML
    private void viewReviews() throws IOException {
        if(!checkOnline()) return;
        int id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(id);
        SceneManager.changeScene("RestaurantReviews");
    }

    /**
     * Effettua il logout dell’utente e torna alla schermata principale.
     *
     * @throws IOException se la schermata non viene caricata correttamente
     */
    @FXML
    private void logout() throws IOException {
        User.logout();
        SceneManager.changeScene("App");
    }

    /**
     * Avvia la creazione di un nuovo ristorante.
     * <p>Reimposta lo stato di editing e passa alla schermata dedicata.</p>
     *
     * @throws IOException se la schermata non viene caricata correttamente
     */
    @FXML
    private void addRestaurant() throws IOException {
        EditingRestaurant.reset();
        SceneManager.changeScene("EditRestaurant");
    }

    /**
     * Restituisce i nodi interattivi della view, utilizzati da {@link OnlineChecker}.
     * <br>Vengono disabilitati automaticamente quando il server risulta offline.
     *
     * @return array contenente elementi della UI modificabili dall'utente
     */
    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                restaurants_container,
                edit_btn, reviews_btn, prev_btn, next_btn
        };
    }
}
