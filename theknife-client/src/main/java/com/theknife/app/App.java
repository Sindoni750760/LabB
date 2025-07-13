package main.java.com.theknife.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/scenes/App.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            Communicator comm = new Communicator("127.0.0.1", 12345);
            comm.sendStream("ping");
            String received = comm.readStream();
            System.out.println(received);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}