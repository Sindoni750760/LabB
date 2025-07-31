package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AppController {
    @FXML
    private Label user_info_label, notification_label;
    @FXML
    private Button register_btn, login_btn, logout_btn;

    @FXML
    private void initialize() {
        //loads the App message
        String[] app_message = SceneManager.getAppMessage();
        if(app_message != null) {
            notification_label.setVisible(true);
            notification_label.setText(app_message[0]);
            notification_label.setStyle("-fx-text-fill: " + app_message[1]);
        }

        //loads logged in user data
        String[] user_info = User.getInfo();
        if(user_info != null) {
            user_info_label.setText("Login effettuato come " + user_info[0] + " " + user_info[1]);
            //shows/hides button if the user is logged in
            register_btn.setVisible(false);
            login_btn.setVisible(false);
            logout_btn.setVisible(true);
        }
    }

    @FXML
    private void click_register() throws IOException {
        SceneManager.changeScene("Register");
    }

    @FXML
    private void click_login() throws IOException {
        SceneManager.changeScene("Login");
    }

    @FXML
    private void logout() throws Exception {
        register_btn.setVisible(true);
        login_btn.setVisible(true);
        logout_btn.setVisible(false);

        user_info_label.setText("Utente ospite");

        User.logout();
    }
}