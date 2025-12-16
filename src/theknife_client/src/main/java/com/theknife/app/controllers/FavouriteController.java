package com.theknife.app.controllers;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
     * Controller della schermata dei ristoranti preferiti.
     * <p>Consente di:</p>
     * <ul>
     *     <li>Caricare l'elenco dei ristoranti preferiti dell'utente</li>
     *     <li>Rimuovere un ristorante dai preferiti</li>
     *     <li>Visualizzare i dettagli di un ristorante selezionato</li>
     * </ul>
     *
     * <p>Implementa {@link OnlineChecker} per abilitare/disabilitare i componenti UI
     * in base allo stato di connessione del server.</p>
     */
public class FavouriteController implements OnlineChecker {

    /** ID dei ristoranti preferiti caricati dal server. */
    @FXML private ListView<String> favourites_list;
    /** Nome dei ristoranti preferiti corrispondenti agli ID. */
    @FXML private Button remove_btn;
    @FXML private Button view_btn;
    @FXML private Label notification_label;

    private String[] fav_ids;
    private String[] fav_names;

    /**
     * Costruttore di default del controller {@code FavouriteController}.
     *
     * <p>Il costruttore non esegue inizializzazioni esplicite poiché
     * l'istanziazione del controller e l'iniezione dei campi {@code @FXML}
     * sono gestite automaticamente dal framework JavaFX tramite {@code FXMLLoader}.</p>
     */
    public FavouriteController() {
        super();
    }


    /**
     * Inizializza la schermata dei preferiti.
     * <p>Esegue:</p>
     * <ul>
     *     <li>Verifica della connessione al server</li>
     *     <li>Caricamento iniziale dei preferiti</li>
     * </ul>
     *
     * @throws IOException se il caricamento iniziale fallisce
     */
    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("FavouriteController initialized");
        if (!checkOnline()) return;
        loadFavorites();
    }

    /**
     * Carica la lista di ristoranti preferiti utilizzando il protocollo
     * {@code getRestaurants} con filtro esclusivo sulle preferenze dell'utente.
     * <br>Aggiorna la lista visibile nella UI.
     *
     * <p>In caso di disconnessione o risposta inconsistente da parte del server,
     * invoca il fallback.</p>
     *
     * @throws IOException se la comunicazione con il server fallisce
     */
    private void loadFavorites() throws IOException {
        if (!checkOnline()) return;

        notification_label.setVisible(false);
        favourites_list.getItems().clear();
        remove_btn.setDisable(true);
        view_btn.setDisable(true);

        Communicator.send("getRestaurants");
        Communicator.send("0");          // page
        Communicator.send("all");        // mode
        Communicator.send("-");          // first
        Communicator.send("-");          // second
        Communicator.send("-");          // range
        Communicator.send("-");          // price min
        Communicator.send("-");          // price max
        Communicator.send("-");          // category
        Communicator.send("n");          // delivery filter
        Communicator.send("n");          // online filter
        Communicator.send("-");          // stars min
        Communicator.send("-");          // stars max
        Communicator.send("y");          // ONLY FAVOURITES !!!

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        if (!"ok".equals(resp)) {
            notification_label.setVisible(true);
            notification_label.setText("Errore dal server: " + resp);
            return;
        }

        // pagine (non ci interessa)
        String pages = Communicator.read();
        String sizeStr = Communicator.read();

        if (pages == null || sizeStr == null) {
            fallback();
            return;
        }

        int size = Integer.parseInt(sizeStr);
        fav_ids   = new String[size];
        fav_names = new String[size];

        for (int i = 0; i < size; i++) {
            fav_ids[i]   = Communicator.read();
            fav_names[i] = Communicator.read();

            if (fav_ids[i] == null || fav_names[i] == null) {
                fallback();
                return;
            }
        }

        favourites_list.getItems().setAll(fav_names);
        checkSelected();
    }

    /**
     * Rimuove il ristorante selezionato dai preferiti.
     *
     * <p>Flusso logico:</p>
     * <ul>
     *     <li>Invia comando {@code removeFavourite} al server</li>
     *     <li>Aggiorna l’elenco locale</li>
     *     <li>Aggiorna la lista visibile nella UI</li>
     * </ul>
     *
     * @throws IOException se la comunicazione con il server fallisce
     */
    @FXML
    private void removeFavourite() throws IOException {
        int index = favourites_list.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Communicator.send("removeFavourite");
        Communicator.send(fav_ids[index]);

        String resp = Communicator.read();
        if (resp == null || !"ok".equals(resp)) {
            SceneManager.setAppWarning("Errore nella rimozione: " + resp);
            return;
        }

        // Rimuovi dall'elenco locale
        List<String> names = new ArrayList<>(Arrays.asList(fav_names));
        List<String> ids = new ArrayList<>(Arrays.asList(fav_ids));
        names.remove(index);
        ids.remove(index);

        fav_names = names.toArray(new String[0]);
        fav_ids = ids.toArray(new String[0]);

        favourites_list.getItems().setAll(fav_names);
        checkSelected();
    }

    /**
     * Apre la schermata di dettaglio del ristorante selezionato.
     * <br>Imposta {@link EditingRestaurant} con l'ID corrispondente
     * e naviga verso la view informativa.
     *
     * @throws IOException se la nuova schermata non può essere caricata
     */
    @FXML
    private void viewRestaurant() throws IOException {
        if (!checkOnline()) return;

        int index = favourites_list.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        EditingRestaurant.setEditing(Integer.parseInt(fav_ids[index]));
        SceneManager.setPreviousNavigation("Favorites");
        SceneManager.changeScene("ViewRestaurantInfo");
    }

     /**
     * Controlla l'elemento selezionato nella ListView.
     * <p>Disabilita i pulsanti {@code remove_btn} e {@code view_btn} se
     * non è selezionato alcun elemento.</p>
     */
    @FXML
    private void checkSelected() {
        int index = favourites_list.getSelectionModel().getSelectedIndex();
        boolean disable = index < 0;

        remove_btn.setDisable(disable);
        view_btn.setDisable(disable);
    }

    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @throws IOException se la schermata non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    /**
     * Restituisce i nodi interattivi della schermata,
     * utilizzati da {@link OnlineChecker} per disabilitare componenti
     * in caso di server offline.
     *
     * @return array contenente ListView e pulsanti interattivi
     */
    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{
                favourites_list, remove_btn, view_btn
        };
    }
}
