package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
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
        //ok, username, password
        Communicator.sendStream("login");
        Communicator.sendStream(username.getText());
        Communicator.sendStream(password.getText());

        switch(Communicator.readStream()) {
            case "ok":
                Communicator.sendStream("getUserInfo");
                String name = Communicator.readStream();
                String surname = Communicator.readStream();
                boolean is_restaurateur = Communicator.readStream().equals("y");
                User.login(name, surname, is_restaurateur);
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