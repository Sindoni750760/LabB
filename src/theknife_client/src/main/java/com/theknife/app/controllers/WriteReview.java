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

    private static int stars;
    private static boolean is_restaurateur;
    private static boolean is_editing;

    @FXML private Button stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn;
    @FXML private Button publish_btn, delete_btn;
    @FXML private Label stars_label, max_chars_label, notification_label;
    @FXML private TextArea text_area;

    @FXML
    private void initialize() throws IOException {

        stars = 0;
        is_editing = false;

        is_restaurateur = User.getInfo() != null && User.getInfo()[2].equals("y");

        if (is_restaurateur) {
            setupRestaurateurMode();
        } else {
            setupUserMode();
        }
    }

    /* ---------------------- MODALITÀ RISTORATORE ---------------------- */

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
        if (resp == null) { fallback(); return; }

        if (resp.equals("ok")) {
            String text = Communicator.read();
            if (text == null) { fallback(); return; }

            text_area.setText(text);
            checkTextBox();

            publish_btn.setText("Modifica");
            delete_btn.setVisible(true);
            is_editing = true;
        }
    }


    /* ---------------------- MODALITÀ UTENTE ---------------------- */

    private void setupUserMode() throws IOException {

        Communicator.send("getMyReview");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));

        String starsStr = Communicator.read();
        if (starsStr == null) { fallback(); return; }

        stars = Integer.parseInt(starsStr);

        String text = Communicator.read();
        if (text == null) { fallback(); return; }

        text_area.setText(text);
        checkTextBox();

        is_editing = stars > 0;

        if (is_editing) {
            publish_btn.setText("Modifica");
            stars_label.setText(stars + " stelle");
            delete_btn.setVisible(true);
        }
    }


    /* ---------------------- INPUT LIMITS ---------------------- */

    @FXML
    private void checkTextBox() {
        String text = text_area.getText();

        if (text.length() > 255) {
            text = text.substring(0, 255);
            text_area.setText(text);
        }

        max_chars_label.setText(text.length() + "/255");
    }


    /* ---------------------- PUBBLICAZIONE ---------------------- */

    @FXML
    private void publish() throws IOException {

        if (is_restaurateur) {
            sendRestaurateurReview();
            return;
        }

        sendUserReview();
    }


    private void sendRestaurateurReview() throws IOException {
        Communicator.send(is_editing ? "editResponse" : "addResponse");
        Communicator.send(Integer.toString(EditingRestaurant.getReviewId()));
        Communicator.send(text_area.getText());

        Communicator.read();
        goBack();
    }


    private void sendUserReview() throws IOException {

        if (!is_editing && (stars < 1 || stars > 5)) {
            setNotification("Devi dare un voto in stelle!");
            return;
        }

        if (is_editing) {
            Communicator.send("editReview");
        } else {
            Communicator.send("addReview");
        }

        Communicator.send(Integer.toString(EditingRestaurant.getId()));
        Communicator.send(Integer.toString(stars));
        Communicator.send(text_area.getText());

        Communicator.read();
        goBack();
    }


    /* ---------------------- ELIMINAZIONE ---------------------- */

    @FXML
    private void delete() throws IOException {

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

        Communicator.read();
        goBack();
    }


    /* ---------------------- STAR SELECT ---------------------- */

    private void setStar(int num) {
        stars = num;
        stars_label.setText(stars + " stelle");
    }

    @FXML private void setStar1() { setStar(1); }
    @FXML private void setStar2() { setStar(2); }
    @FXML private void setStar3() { setStar(3); }
    @FXML private void setStar4() { setStar(4); }
    @FXML private void setStar5() { setStar(5); }


    /* ---------------------- UTIL ---------------------- */

    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    private void fallback() throws IOException {
        SceneManager.setAppWarning("Il server non è raggiungibile");
        SceneManager.changeScene("App");
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }
}
