package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AppController {
    @FXML
    private Label user_info_label, notification_label;

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
        if(user_info != null)
            user_info_label.setText("Login effettuato come " + user_info[0] + " " + user_info[1]);
    }

    @FXML
    private void click_register() throws IOException {
        SceneManager.changeScene("Register");
    }

    @FXML
    private void click_login() throws IOException {
        SceneManager.changeScene("Login");
    }
}