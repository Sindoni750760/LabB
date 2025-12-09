package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controller della schermata dedicata alla scrittura o modifica di una recensione
 * o alla risposta del ristoratore ad essa.
 *
 * <p>Si occupa di:</p>
 * <ul>
 *     <li>gestire la distinzione tra utente normale e ristoratore</li>
 *     <li>gestire aggiunta, modifica ed eliminazione di recensioni</li>
 *     <li>gestire aggiunta, modifica ed eliminazione delle risposte del ristoratore</li>
 *     <li>validare numero di stelle e lunghezza testo</li>
 *     <li>gestire fallback e disabilitazione UI se server offline</li>
 * </ul>
 *
 * <p>Modalità operative:</p>
 * <ul>
 *     <li><b>Utente</b> → recensisce con stelle e testo (max 255 caratteri)</li>
 *     <li><b>Ristoratore</b> → risponde ad una recensione esistente</li>
 * </ul>
 *
 * <p>Implementa {@link OnlineChecker} per automatizzare il comportamento
 * in caso di server non raggiungibile.</p>
 */

public class WriteReview implements OnlineChecker {
    /** Numero di stelle correnti selezionate dall'utente. */
    private static int stars;
    /** {@True} se l'utente corrente è ristoratore. */
    private static boolean is_restaurateur;
    /** {@True} se stiamo modificando una recensione/risposta esistente. */
    private static boolean is_editing;

    @FXML private Button stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn;
    @FXML private Button publish_btn, delete_btn;
    @FXML private Label stars_label, max_chars_label, notification_label;
    @FXML private TextArea text_area;
    
    /**
     * Inizializza la schermata determinando:
     * <ul>
     *     <li>il ruolo utente/ristoratore</li>
     *     <li>se esiste una recensione o risposta da modificare</li>
     *     <li>informazioni da pre-popolare</li>
     * </ul>
     *
     * <p>Flow di inizializzazione:</p>
     * <ol>
     *     <li>Verifica connessione server</li>
     *     <li>Identifica ruolo utente</li>
     *     <li>Configura UI appropriata</li>
     * </ol>
     *
     * @throws IOException se la comunicazione col server fallisce
     */
    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("WriteReview initialized");

        stars = 0;
        is_editing = false;

        is_restaurateur = User.getInfo() != null && User.getInfo()[2].equals("y");

        if (!checkOnline()) {
            return;
        }

        if (is_restaurateur) {
            setupRestaurateurMode();
        } else {
            setupUserMode();
        }
    }

    /**
     * Configura modalità ristoratore:
     * <ul>
     *     <li>Nasconde la scelta delle stelle</li>
     *     <li>Pre-carica eventuale risposta già presente</li>
     *     <li>Aggiorna pulsanti in base al contesto</li>
     * </ul>
     *
     * Il comportamento avviene tramite protocollo:
     * <pre>
     * getResponse
     * └─ idRecensione
     * </pre>
     *
     * @throws IOException se server non risponde
     */
    private void setupRestaurateurMode() throws IOException {

        stars_1_btn.setVisible(false);
        stars_2_btn.setVisible(false);
        stars_3_btn.setVisible(false);
        stars_4_btn.setVisible(false);
        stars_5_btn.setVisible(false);
        stars_label.setVisible(false);

        Communicator.send("getResponse");
        Communicator.send(Integer.toString(EditingRestaurant.getReviewId()));

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        if (resp.equals("ok")) {
            String text = Communicator.read();
            if (text == null) {
                fallback();
                return;
            }

            text_area.setText(text);
            checkTextBox();

            publish_btn.setText("Modifica");
            delete_btn.setVisible(true);
            is_editing = true;
        }
    }

     /**
     * Configura modalità utente:
     * <ul>
     *     <li>Carica stelle e testo eventuali già presenti</li>
     *     <li>Determina se stiamo modificando o creando una recensione</li>
     * </ul>
     *
     * Protocollo:
     * <pre>
     * getMyReview
     * └─ idRistorante
     * → stars
     * → text
     * </pre>
     *
     * @throws IOException se server non risponde
     */
    private void setupUserMode() throws IOException {

        Communicator.send("getMyReview");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String starsStr = Communicator.read();
        if (starsStr == null) {
            fallback();
            return;
        }

        stars = Integer.parseInt(starsStr);

        String text = Communicator.read();
        if (text == null) {
            fallback();
            return;
        }

        text_area.setText(text);
        checkTextBox();

        is_editing = stars > 0;

        if (is_editing) {
            publish_btn.setText("Modifica");
            stars_label.setText(stars + " stelle");
            delete_btn.setVisible(true);
        }
    }

    /**
     * Verifica limite caratteri del campo testo,
     * tronca eventuale input > 255,
     * e aggiorna il contatore UI.
     */
    @FXML
    private void checkTextBox() {
        String text = text_area.getText();

        if (text.length() > 255) {
            text = text.substring(0, 255);
            text_area.setText(text);
        }

        max_chars_label.setText(text.length() + "/255");
    }

    /**
     * Gestisce la pubblicazione della recensione o risposta.
     *
     * <p>Scelta della logica:</p>
     * <ul>
     *     <li>utente normale → {@link #sendUserReview()}</li>
     *     <li>ristoratore → {@link #sendRestaurateurReview()}</li>
     * </ul>
     *
     * @throws IOException se server non risponde
     */
    @FXML
    private void publish() throws IOException {

        if (!checkOnline()) {
            return;
        }

        if (is_restaurateur) {
            sendRestaurateurReview();
            return;
        }

        sendUserReview();
    }
    /**
     * Invio/modifica/eliminazione della risposta lato ristoratore.
     *
     * Protocollo utilizzato:
     * <pre>
     * addResponse  | editResponse
     * removeResponse
     * </pre>
     *
     * @throws IOException in caso di errore comunicazione
     */
    private void sendRestaurateurReview() throws IOException {
        Communicator.send(is_editing ? "editResponse" : "addResponse");
        Communicator.send(Integer.toString(EditingRestaurant.getReviewId()));
        Communicator.send(text_area.getText());

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        goBack();
    }

    /**
     * Invio/modifica/eliminazione recensione lato utente.
     *
     * <p>Controlla inoltre validità delle stelle quando si crea una nuova recensione.</p>
     *
     * Protocollo:
     * <pre>
     * addReview
     * editReview
     * removeReview
     * </pre>
     *
     * @throws IOException in caso di errore comunicazione
     */

    private void sendUserReview() throws IOException {

        if (!is_editing && (stars < 1 || stars > 5)) {
            ClientLogger.getInstance().warning("Review submission failed: invalid star rating");
            setNotification("Devi dare un voto in stelle!");
            return;
        }

        if (is_editing) {
            ClientLogger.getInstance().info("Editing user review for restaurant: " + EditingRestaurant.getId());
            Communicator.send("editReview");
        } else {
            ClientLogger.getInstance().info("Adding new user review for restaurant: " + EditingRestaurant.getId());
            Communicator.send("addReview");
        }

        Communicator.send(Integer.toString(EditingRestaurant.getId()));
        Communicator.send(Integer.toString(stars));
        Communicator.send(text_area.getText());

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        goBack();
    }

    /**
     * Elimina recensione (utente) o risposta (ristoratore),
     * previa conferma tramite alert.
     *
     * Inserisce il comando appropriato
     * in base al ruolo utente.
     *
     * @throws IOException in caso di errore lato server
     */
    @FXML
    private void delete() throws IOException {

        if (!checkOnline()) {
            return;
        }

        String msg = is_restaurateur
                ? "Sei sicuro di voler eliminare questa risposta?"
                : "Sei sicuro di voler eliminare questa recensione?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES)
            return;

        if (is_restaurateur) {
            Communicator.send("removeResponse");
            Communicator.send(Integer.toString(EditingRestaurant.getReviewId()));
        } else {
            Communicator.send("removeReview");
            Communicator.send(Integer.toString(EditingRestaurant.getId()));
        }

        String resp = Communicator.read();
        if (resp == null) {
            fallback();
            return;
        }

        goBack();
    }

    /**
     * Imposta il numero di stelle selezionato dall'utente
     * aggiornando l'etichetta UI.
     *
     * @param num valore stelle selezionato (1–5)
     */
    private void setStar(int num) {
        stars = num;
        stars_label.setText(stars + " stelle");
    }

    /** Wrapper UI 1 stella */ @FXML private void setStar1() { setStar(1); }
    /** Wrapper UI 2 stelle */ @FXML private void setStar2() { setStar(2); }
    /** Wrapper UI 3 stelle */ @FXML private void setStar3() { setStar(3); }
    /** Wrapper UI 4 stelle */ @FXML private void setStar4() { setStar(4); }
    /** Wrapper UI 5 stelle */ @FXML private void setStar5() { setStar(5); }

    /**
     * Mostra un messaggio all'utente nella schermata corrente.
     *
     * @param msg testo da mostrare
     */
    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    /**
     * Torna alla schermata precedente delle recensioni.
     *
     * @throws IOException se cambio scena fallisce
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }

    /**
     * Restituisce i nodi interattivi che devono essere disabilitati
     * in caso di server offline.
     *
     * @return controlli interattivi della schermata
     */
    @Override
    public Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn,
                publish_btn, delete_btn,
                text_area, notification_label
        };
    }
}
