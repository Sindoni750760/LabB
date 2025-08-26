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

    //function used to change scene passing the scene name
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

    //function used to retreive the message to be displayed in the App scene
    public static String[] getAppMessage() {
        if(appMessage == null)
            return null;

        return new String[]{appMessage, appMessageColor};
    }

    //function used to set an alert displayed in the App scen
    public static void setAppAlert(String text) {
        appMessage = text;
        appMessageColor = "green";
    }

    //function used to set a warning displayed in the App scene
    public static void setAppWarning(String text) {
        appMessage = text;
        appMessageColor = "red";
    }
}
