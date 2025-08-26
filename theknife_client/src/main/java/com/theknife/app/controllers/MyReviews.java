package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class MyReviews {
    private static int current_page, total_pages;
    @FXML
    private ListView<String> reviews_listview;
    @FXML
    private Label no_reviews_label, pages_label;
    @FXML
    private Button prev_btn, next_btn;

    @FXML
    private void initialize() throws IOException {
        current_page = total_pages = 0;
        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        Communicator.sendStream("getMyReviewsPages");
        if(Communicator.readStream().equals("ok")) {
            total_pages = Integer.parseInt(Communicator.readStream());
            changePage(0);
        } else {
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Errore nel server");
        }

        //https://stackoverflow.com/questions/53493111/javafx-wrapping-text-in-listview
        //function used to wrap the text for every cell of the listview
        reviews_listview.setCellFactory(lv -> new ListCell<String>() {
            {
                setPrefWidth(0); // forces the cell to size itself based on the ListView
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true); // the magic line
                }
            }
        });
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    //function used to change the page of the reviews
    private void changePage(int page) throws IOException {
        pages_label.setText(Integer.toString(page + 1) + "/" + Integer.toString(total_pages));
        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.sendStream("getMyReviews");
        Communicator.sendStream(Integer.toString(page));
        int size = Integer.parseInt(Communicator.readStream());

        String[] reviews_compact = new String[size];

        for(int i = 0; i < size; i++) {
            String restaurant_name = Communicator.readStream();
            String given_stars = Communicator.readStream();
            String review_text = Communicator.readStream();
            reviews_compact[i] = "Nome ristorante: " + restaurant_name + "\nValutazione: " + given_stars + "/5\nRecensione: " + review_text;
        }

        reviews_listview.getItems().setAll(reviews_compact);
    }

    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }
}