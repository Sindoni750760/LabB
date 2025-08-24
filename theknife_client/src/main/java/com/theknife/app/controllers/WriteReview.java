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

public class WriteReview {
    private static int stars;
    private static boolean is_restaurateur, is_editing;
    @FXML
    private Button stars_1_btn, stars_2_btn, stars_3_btn, stars_4_btn, stars_5_btn, publish_btn, delete_btn;
    @FXML
    private Label stars_label, max_chars_label, notification_label;
    @FXML
    private TextArea text_area;

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

    @FXML
    private void checkTextBox() {
        String text = text_area.getText();
        if(text.length() > 255) {
            text = text.substring(0, 255);
            text_area.setText(text);
        }
        max_chars_label.setText(Integer.toString(text.length()) + "/255");
    }

    @FXML
    private void publish() throws IOException {
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

    private void setStar(int num) {
        stars = num;
        stars_label.setText(Integer.toString(stars) + " stelle");
    }

    @FXML
    private void setStar1() {setStar(1);}
    @FXML
    private void setStar2() {setStar(2);}
    @FXML
    private void setStar3() {setStar(3);}
    @FXML
    private void setStar4() {setStar(4);}
    @FXML
    private void setStar5() {setStar(5);}

    private void setNotification(String msg) {
        notification_label.setVisible(true);
        notification_label.setText(msg);
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("RestaurantReviews");
    }
}