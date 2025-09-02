package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controller per la schermata di scrittura o modifica di una recensione.
 * Gestisce sia le recensioni degli utenti che le risposte dei ristoratori.
 * Permette di aggiungere, modificare o eliminare contenuti testuali e valutazioni in stelle.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class WriteReview {

    /** Valutazione in stelle assegnata alla recensione. */
    private static int stars;

    /** Indica se l'utente è un ristoratore. */
    private static boolean is_restaurateur;

    /** Indica se si sta modificando una recensione o risposta esistente. */
    private static boolean is_editing;

   /** Pulsanti per selezionare il numero di stelle. */
    @FXML private Button stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn;

    /** Pulsante per pubblicare o modificare la recensione/risposta. */
    @FXML private Button publish_btn;

    /** Pulsante per eliminare la recensione/risposta. */
    @FXML private Button delete_btn;

    /** Etichetta che mostra il numero di stelle selezionate. */
    @FXML private Label stars_label;

    /** Etichetta che mostra il numero massimo di caratteri consentiti. */
    @FXML private Label max_chars_label;

    /** Etichetta per notifiche o messaggi di errore. */
    @FXML private Label notification_label;

    /** Area di testo per scrivere la recensione o la risposta. */
    @FXML private TextArea text_area;

    /**
     * Inizializza la schermata in base al ruolo dell'utente.
     * Carica eventuali contenuti esistenti e imposta lo stato iniziale.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void initialize() throws IOException {
        is_editing = false;
        stars = 0;
        is_restaurateur = User.getInfo()[2].equals("y");

        if(is_restaurateur) {
            stars_1_btn.setVisible(false);
            stars_2_btn.setVisible(false);
            stars_3_btn.setVisible(false);
            stars_4_btn.setVisible(false);
            stars_5_btn.setVisible(false);
            stars_label.setVisible(false);

            Communicator.sendStream("getResponse");
            Communicator.sendStream(Integer.toString(EditingRestaurant.getReviewId()));
            
            if(Communicator.readStream().equals("ok")) {
                text_area.setText(Communicator.readStream());
                checkTextBox();
                publish_btn.setText("Modifica");
                delete_btn.setVisible(true);
                is_editing = true;
            }
        } else {
            Communicator.sendStream("getMyReview");
            Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));

            stars = Integer.parseInt(Communicator.readStream());
            text_area.setText(Communicator.readStream());
            checkTextBox();

            is_editing = stars > 0;

            if(is_editing) {
                publish_btn.setText("Modifica");
                stars_label.setText(Integer.toString(stars) + " stelle");
                delete_btn.setVisible(true);
            }
        }
    }

    /**
     * Controlla e limita il testo inserito a un massimo di 255 caratteri.
     * Aggiorna l'etichetta che mostra il conteggio dei caratteri.
     */
    @FXML
    private void checkTextBox() {
        //to limitate the review text to max 255 chars
        String text = text_area.getText();
        if(text.length() > 255) {
            text = text.substring(0, 255);
            text_area.setText(text);
        }
        max_chars_label.setText(Integer.toString(text.length()) + "/255");
    }

    /**
     * Pubblica o modifica una recensione o una risposta.
     * Valida l'input e invia i dati al server.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void publish() throws IOException {
        //edits/adds a review/response
        if(is_restaurateur) {
            Communicator.sendStream(is_editing ? "editResponse" : "addResponse");
            Communicator.sendStream(Integer.toString(EditingRestaurant.getReviewId()));
            Communicator.sendStream(text_area.getText());
            Communicator.readStream();
            goBack();
        } else {
            if(is_editing) {
                //editing the review
                Communicator.sendStream("editReview");
                Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
                Communicator.sendStream(Integer.toString(stars));
                Communicator.sendStream(text_area.getText());
                Communicator.readStream();
                goBack();
            } else if(stars < 1 || stars > 5)
                setNotification("Devi dare un voto in stelle!");
            else {
                //creation of the review
                Communicator.sendStream("addReview");
                Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
                Communicator.sendStream(Integer.toString(stars));
                Communicator.sendStream(text_area.getText());
                Communicator.readStream();
                goBack();
            }
        }
    }

    /**
     * Elimina la recensione o la risposta dopo conferma dell'utente.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    @FXML
    private void delete() throws IOException {
        String text = is_restaurateur ? "Sei sicuro di voler eliminare questa risposta?" : "Sei sicuro di voler eliminare questa recensione?";
        Alert alert = new Alert(AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if(is_restaurateur) {
                Communicator.sendStream("removeResponse");
                Communicator.sendStream(Integer.toString(EditingRestaurant.getReviewId()));
            } else {
                Communicator.sendStream("removeReview");
                Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
            }
            Communicator.readStream();
            goBack();
        }
    }

    /**
     * Imposta il numero di stelle selezionato e aggiorna l'etichetta.
     *
     * @param num numero di stelle da assegnare
     */
    private void setStar(int num) {
        stars = num;
        stars_label.setText(Integer.toString(stars) + " stelle");
    }

    /** Seleziona 1 stella. */ @FXML private void setStar1() { setStar(1); }
    /** Seleziona 2 stelle. */ @FXML private void setStar2() { setStar(2); }
    /** Seleziona 3 stelle. */ @FXML private void setStar3() { setStar(3); }
    /** Seleziona 4 stelle. */ @FXML private void setStar4() { setStar(4); }
    /** Seleziona 5 stelle. */ @FXML private void setStar5() { setStar(5); }

    /**
     * Mostra un messaggio di notifica all'utente.
     *
     * @param msg testo della notifica da visualizzare
     */
    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    /**
     * Torna alla schermata delle recensioni del ristorante.
     *
     * @throws IOException se si verifica un errore nel cambio scena
     */
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }
}