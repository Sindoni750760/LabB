package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class RestaurantReviews {
    private static boolean is_logged, is_restaurateur;
    private static String[] reviews_ids;
    private static int total_pages, current_page;
    @FXML
    private Label no_reviews_label, page_label;
    @FXML
    private Button prev_btn, next_btn, add_review_btn;
    @FXML
    private ListView<String> reviews_listview;

    @FXML
    private void initialize() throws IOException {
        prev_btn.setDisable(true); // VS
        next_btn.setDisable(true);
        no_reviews_label.setVisible(false);
        current_page = 0;
        String[] user_info = User.getInfo();

        is_logged = user_info != null;

        if(is_logged) {
            add_review_btn.setVisible(true);
            is_restaurateur = user_info[2].equals("y");

            if(is_restaurateur) {
                add_review_btn.setDisable(true);
                add_review_btn.setText("Rispondi/modifica risposta");
            } else {
                //checks if the user has set a review
                Communicator.sendStream("getMyReview");
                Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));

                int stars = Integer.parseInt(Communicator.readStream());
                Communicator.readStream();

                if(stars > 0)
                    add_review_btn.setText("Modifica recensione");
            }
        }

        Communicator.sendStream("getReviewsPages");
        Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
        total_pages = Integer.parseInt(Communicator.readStream());

        if(total_pages > 0)
            changePage(0);
        else
            no_reviews_label.setVisible(true);

        //https://stackoverflow.com/questions/53493111/javafx-wrapping-text-in-listview
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

    private void changePage(int page) throws IOException {
        page_label.setText(Integer.toString(page + 1) + '/' + total_pages);
        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);


        Communicator.sendStream("getReviews");
        Communicator.sendStream(Integer.toString(EditingRestaurant.getId()));
        Communicator.sendStream(Integer.toString(page));

        int size = Integer.parseInt(Communicator.readStream());
        String[] reviews_stars = new String[size];
        String[] reviews_texts = new String[size];
        String[] reviews_reply = new String[size];
        reviews_ids = new String[size];

        for(int i = 0; i < size; i++) {
            reviews_ids[i] = Communicator.readStream();
            reviews_stars[i] = Communicator.readStream();
            reviews_texts[i] = Communicator.readStream();

            if(Communicator.readStream().equals("y"))
                reviews_reply[i] = Communicator.readStream();
            else
                reviews_reply[i] = null;
        }

        String[] review_compact = new String[size];
        for(int i = 0; i < size; i++)
            review_compact[i] = reviews_stars[i] + "/5 " + reviews_texts[i] + (reviews_reply[i] == null ? "" : "\nRisposta del ristoratore: " + reviews_reply[i]);

        reviews_listview.getItems().setAll(review_compact);
    }

    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    @FXML
    private void addReview() throws IOException {
        if(is_restaurateur) {
            int review_id = Integer.parseInt(reviews_ids[reviews_listview.getSelectionModel().getSelectedIndex()]);
            EditingRestaurant.setReviewId(review_id);
        }
        SceneManager.changeScene("WriteReview");
    }

    @FXML
    private void checkSelected() {
        add_review_btn.setDisable(reviews_listview.getSelectionModel().getSelectedIndex() < 0);
    }

    @FXML
    private void goBack() throws IOException {
        if(is_restaurateur)
            SceneManager.changeScene("MyRestaurants");
        else
            SceneManager.changeScene("ViewRestaurantInfo");
    }
}
