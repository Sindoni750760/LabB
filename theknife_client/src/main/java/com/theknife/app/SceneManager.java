package com.theknife.app;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage stage;
    //message to be displayed in the App scene
    private static String appMessage = null;
    private static String appMessageColor = null;

    public static void init(Stage s) throws IOException {
        stage = s;
        changeScene("App");
    }

    public static void changeScene(String sceneName) throws IOException {
        //clears the message displayed in the App scene when changing scene
        if(!sceneName.equals("App"))
            appMessage = null;

        String scene_path = "/scenes/" + sceneName + ".fxml";
        Parent root = FXMLLoader.load(SceneManager.class.getResource(scene_path));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    public static String[] getAppMessage() {
        if(appMessage == null)
            return null;

        return new String[]{appMessage, appMessageColor};
    }

    public static void setAppAlert(String text) {
        appMessage = text;
        appMessageColor = "green";
    }
}