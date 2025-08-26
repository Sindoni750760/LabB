package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label notification_label;
    
    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    @FXML
    private void login() throws IOException {
        switch(User.login(username.getText(), password.getText())) {
            case "ok":
                //if the user is a restaurateur, changes the scene to MyRestaurants
                if(User.getInfo()[2].equals("y")) {
                    SceneManager.changeScene("MyRestaurants");
                    break;
                }
                SceneManager.setAppAlert("Login effettuato con successo");
                SceneManager.changeScene("App");
                break;
            case "username":
                setNotification("Utente inesistente");
                break;
            case "password":
                setNotification("Password errata");
                break;
        }
    }

    private void setNotification(String text) {
        notification_label.setVisible(true);
        notification_label.setText(text);
    }
}