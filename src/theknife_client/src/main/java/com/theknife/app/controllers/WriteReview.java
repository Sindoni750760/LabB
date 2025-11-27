package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controller per la schermata di scrittura/modifica recensione o risposta.
 * Gestisce:
 * - modalità utente (recensione con stelle)
 * - modalità ristoratore (risposta alla recensione)
 * - limite caratteri
 * - pubblicazione / eliminazione
 *
 * Implementa OnlineChecker per fallback unificato in caso di server offline.
 */
public class WriteReview implements OnlineChecker {

    private static int stars;
    private static boolean is_restaurateur;
    private static boolean is_editing;

    @FXML private Button stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn;
    @FXML private Button publish_btn, delete_btn;
    @FXML private Label stars_label, max_chars_label, notification_label;
    @FXML private TextArea text_area;

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


    @FXML
    private void checkTextBox() {
        String text = text_area.getText();

        if (text.length() > 255) {
            text = text.substring(0, 255);
            text_area.setText(text);
        }

        max_chars_label.setText(text.length() + "/255");
    }


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


    private void setStar(int num) {
        stars = num;
        stars_label.setText(stars + " stelle");
    }

    @FXML private void setStar1() { setStar(1); }
    @FXML private void setStar2() { setStar(2); }
    @FXML private void setStar3() { setStar(3); }
    @FXML private void setStar4() { setStar(4); }
    @FXML private void setStar5() { setStar(5); }


    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }

    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn,
                publish_btn, delete_btn,
                text_area, notification_label
        };
    }
}
