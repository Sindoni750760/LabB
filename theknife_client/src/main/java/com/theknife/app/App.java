package com.theknife.app;

import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {
    public void start(Stage stage) {
        try {


            Communicator.init("127.0.0.1", 12345);
            
            SceneManager.init(stage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}