package com.theknife.app.controllers;

import java.io.IOException;
import java.net.UnknownHostException;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AppController {
    private static int reconnection_tries;
    @FXML
    private Label user_info_label, notification_label;
    @FXML
    private Button register_btn, login_btn, logout_btn, view_btn, reconnect_btn;

    @FXML
    private void initialize() throws IOException {
        reconnection_tries = 1;

        //loads the App message
        String[] app_message = SceneManager.getAppMessage();
        if(app_message != null) {
            notification_label.setVisible(true);
            notification_label.setText(app_message[0]);
            notification_label.setStyle("-fx-text-fill: " + app_message[1]);
        }

        if(Communicator.isOnline()) {
            //loads logged in user data
            String[] user_info = User.getInfo();
            if(user_info != null) {
                user_info_label.setText("Login effettuato come " + user_info[0] + " " + user_info[1]);
                //shows/hides button if the user is logged in
                register_btn.setText("Vedi recensioni");
                register_btn.setLayoutX(252);
                login_btn.setVisible(false);
                logout_btn.setVisible(true);
            }
        } else
            handleButtonsForDisconnection(true);
    }

    @FXML
    private void click_view_restaurants() throws IOException {
        SceneManager.changeScene("ViewRestaurants");
    }

    @FXML
    private void click_register() throws IOException {
        //if the user is logged in, go to MyReviews page, else go to the registration page
        if(User.getInfo() == null)
            SceneManager.changeScene("Register");
        else
            SceneManager.changeScene("MyReviews");
    }

    @FXML
    private void click_login() throws IOException {
        SceneManager.changeScene("Login");
    }

    @FXML
    private void logout() throws Exception {
        User.logout();
        SceneManager.changeScene("App");
    }

    //used to disable/enable buttons after a disconnection/reconnection
    private void handleButtonsForDisconnection(boolean disable) {
        register_btn.setDisable(disable);
        login_btn.setDisable(disable);
        logout_btn.setDisable(disable);
        view_btn.setDisable(disable);
        reconnect_btn.setVisible(disable);
    }

    @FXML
    private void reconnect() throws UnknownHostException, IOException {
        //used to handle the reconnection messages
        if(Communicator.connect()) {
            reconnection_tries = 1;
            notification_label.setVisible(true);
            notification_label.setText("Riconnessione riuscita");
            notification_label.setStyle("-fx-text-fill: green");

            handleButtonsForDisconnection(false);
        } else {
            notification_label.setVisible(true);
            notification_label.setText("Errore nella riconnessione, tentativo numero " + reconnection_tries++);
            notification_label.setStyle("-fx-text-fill: red");
        }
    }
}