package com.theknife.app.controllers;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
/**
 * Controller della schermata di informazioni dettagliate di un ristorante.
 *
 * <p>Responsabilità principali:</p>
 * <ul>
 *     <li>Visualizzare i dati completi relativi al ristorante selezionato</li>
 *     <li>Permettere la navigazione verso le recensioni del ristorante</li>
 *     <li>Consentire all'utente autenticato di aggiungere o rimuovere il ristorante dai preferiti</li>
 *     <li>Adattare la UI in base allo stato di preferito</li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per la gestione automatica dei casi
 * di server non raggiungibile (fallback, UI disabilitata, riconnessione).</p>
*/

public class ViewRestaurantInfo implements OnlineChecker {

    /** Indica lo stato corrente di preferito del ristorante rispetto all'utente. */
    private boolean is_favourite;

    @FXML private Label name_label;
    @FXML private Label nation_label;
    @FXML private Label city_label;
    @FXML private Label address_label;
    @FXML private Label coordinates_label;
    @FXML private Label reviews_label;
    @FXML private Label price_label;
    @FXML private Label stars_label;
    @FXML private Label services_label;
    @FXML private Label categories_label;

    @FXML private Button fav_btn;

    /**
     * Inizializza la schermata caricando le informazioni relative al ristorante.
     *
     * <p>Il metodo esegue i seguenti passaggi:</p>
     * <ol>
     *     <li>Verifica lo stato del server tramite {@link #checkOnline()}</li>
     *     <li>Recupera i dati del ristorante tramite {@link EditingRestaurant#getInfo()}</li>
     *     <li>Mostra i dati all'interno dei campi UI</li>
     *     <li>Determina se il ristorante è già tra i preferiti</li>
     *     <li>Aggiorna il pulsante preferiti di conseguenza</li>
     * </ol>
     *
     * <p>In caso di errori o dati non disponibili, viene effettuato un fallback minimo
     * (visualizzazione parziale della pagina).</p>
     *
     * @throws IOException se la comunicazione con il server o il refresh scena fallisce
     */
    @FXML
    private void initialize() throws IOException {
        if(!checkOnline()) return;
        ClientLogger.getInstance().info("ViewRestaurantInfo initialized for restaurant: " + EditingRestaurant.getId());

        String[] info = EditingRestaurant.getInfo();
        if (info == null) {
            name_label.setText("Ristorante non trovato");
            return;
        }

        name_label.setText(info[0]);
        nation_label.setText(info[1]);
        city_label.setText(info[2]);
        address_label.setText(info[3]);
        coordinates_label.setText(info[4] + "," + info[5]);
        price_label.setText(info[6] + " €");
        reviews_label.setText(info[10]);
        stars_label.setText(info[9].equals("0") ? "Non disponibile" : info[9] + "/5");
        categories_label.setText(info[11]);

        boolean d = info[7].equals("y");
        boolean o = info[8].equals("y");

        if (d && o)      services_label.setText("Delivery e prenotazione online");
        else if (d)      services_label.setText("Solo delivery");
        else if (o)      services_label.setText("Solo prenotazione online");
        else             services_label.setText("Nessuno");

        if (User.getInfo() == null) {
            fav_btn.setVisible(false);
        } else {
            if (!checkOnline()) {
                fav_btn.setVisible(false);
            } else {
                Communicator.send("isFavourite");
                Communicator.send(Integer.toString(EditingRestaurant.getId()));

                String favResp = Communicator.read();
                if (favResp == null) {
                    fallback();
                    return;
                }

                is_favourite = favResp.equals("y");

                if (is_favourite)
                    fav_btn.setText("Rimuovi dai preferiti");
            }
        }
    }

    /**
     * Torna alla schermata precedente.
     *
     * <p>La destinazione dipende dalla navigazione effettuata in precedenza:</p>
     * <ul>
     *     <li>Se proveniamo dai preferiti → torna a "Favorites"</li>
     *     <li>Altrimenti → torna alla schermata lista ristoranti</li>
     * </ul>
     *
     * @throws IOException se la scena non può essere caricata
     */
    @FXML
    private void goBack() throws IOException {
        String ctx = SceneManager.getPreviousNavigation();
        if ("Favorites".equals(ctx)) {
            SceneManager.changeScene("Favorites");
        } else {
            SceneManager.changeScene("ViewRestaurants");
        }
    }

    /**
     * Naviga alla schermata delle recensioni del ristorante.
     *
     * @throws IOException se la nuova schermata non è caricabile
    */
    @FXML
    private void viewReviews() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }

    /**
     * Aggiunge o rimuove il ristorante dai preferiti, in base allo stato corrente.
     *
     * <p>Flusso logico:</p>
     * <ol>
     *     <li>Verifica stato server</li>
     *     <li>Send comando al server:
     *         <ul>
     *             <li>"addFavourite"</li>
     *             <li>"removeFavourite"</li>
     *         </ul>
     *     </li>
     *     <li>Mostra feedback tramite alert/warning globale</li>
     *     <li>Determina la schermata successiva coerentemente</li>
     * </ol>
     *
     * <p>Comportamenti speciali:</p>
     * <ul>
     *     <li>Se un ristorante viene aggiunto → redirect automatico a "Favorites"</li>
     *     <li>Se viene rimosso:
     *         <ul>
     *             <li>Se eravamo in "Favorites" → resta in "Favorites"</li>
     *             <li>Altrimenti → torna alla lista ristoranti</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @throws IOException se la navigazione successiva fallisce
     */
    @FXML
    private void addToFavourites() throws IOException {
        if (!checkOnline()) return;

        boolean wasFavourite = is_favourite;

        ClientLogger.getInstance().info(
                (wasFavourite ? "Removing" : "Adding") +
                        " restaurant from/to favourites: " + EditingRestaurant.getId()
        );

        Communicator.send(wasFavourite ? "removeFavourite" : "addFavourite");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        if (!"ok".equals(resp)) {
            SceneManager.setAppWarning("Errore nella gestione dei preferiti: " + resp);
        } else {
            SceneManager.setAppAlert(
                    wasFavourite ? "Rimosso dai preferiti" : "Aggiunto ai preferiti"
            );
        }

        if (!wasFavourite) {
            SceneManager.changeScene("Favorites");
            return;
        }

        String ctx = SceneManager.getPreviousNavigation();
        if ("Favorites".equals(ctx)) {
            SceneManager.changeScene("Favorites");
        } else {
            SceneManager.changeScene("ViewRestaurants");
        }
    }

    /**
     * Restituisce i nodi UI che devono essere disabilitati/riabilitati
     * durante la gestione dello stato di connessione del server.
     *
     * <p>In questo caso, l'unico elemento interattivo rilevante
     * è il pulsante per l'aggiunta/rimozione dai preferiti.</p>
     *
     * @return insieme dei nodi interattivi della schermata
    */
    @Override
    public Node[] getInteractiveNodes() {
        return new Node[]{fav_btn};
    }
}
