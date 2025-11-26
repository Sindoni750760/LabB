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

/**
 * Controller per la schermata delle recensioni di un ristorante.
 * Gestisce la visualizzazione delle recensioni, la paginazione,
 * e consente agli utenti di aggiungere o modificare la propria recensione.
 * I ristoratori possono rispondere alle recensioni ricevute.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */

import java.io.IOException;

public class RestaurantReviews {

    private static boolean is_logged;
    private static boolean is_restaurateur;

    private static String[] reviews_ids;

    private static int total_pages;
    private static int current_page;

    @FXML private Label no_reviews_label;
    @FXML private Label page_label;
    @FXML private Label reviews_label;
    @FXML private Label stars_label;
    @FXML private Button prev_btn;
    @FXML private Button next_btn;
    @FXML private Button add_review_btn;
    @FXML private ListView<String> reviews_listview;

    @FXML
    private void initialize() throws IOException {

        prev_btn.setDisable(true);
        next_btn.setDisable(true);
        no_reviews_label.setVisible(false);
        add_review_btn.setDisable(true);
        current_page = 0;

        try {
            String[] user_info = User.getInfo();
            is_logged = user_info != null;

            if (is_logged) {
                add_review_btn.setVisible(true);
                is_restaurateur = user_info[2].equals("y");

                if (is_restaurateur) {
                    add_review_btn.setText("Rispondi / modifica risposta");
                } else {
                    Communicator.send("getMyReview");
                    Communicator.send(Integer.toString(EditingRestaurant.getId()));

                    String starsStr = Communicator.read();
                    if (starsStr == null) throw new IOException("server unreachable");
                    int stars = Integer.parseInt(starsStr);

                    Communicator.read(); // discard review text or marker

                    if (stars > 0)
                        add_review_btn.setText("Modifica recensione");
                }
            } else {
                add_review_btn.setVisible(false);
            }

            String[] restaurant_info = EditingRestaurant.getInfo();
            reviews_label.setText("Recensioni: " + restaurant_info[10]);

            String avgStars = restaurant_info[9];
            stars_label.setText("Valutazione media: " + (avgStars.equals("0") ? "-" : avgStars));

            Communicator.send("getReviewsPages");
            Communicator.send(Integer.toString(EditingRestaurant.getId()));

            String pagesStr = Communicator.read();
            if (pagesStr == null) throw new IOException("server unreachable");

            total_pages = Integer.parseInt(pagesStr);

            if (total_pages > 0)
                changePage(0);
            else
                no_reviews_label.setVisible(true);

        } catch (Exception e) {
            System.err.println("[RestaurantReviews] init error: " + e.getMessage());
            no_reviews_label.setVisible(true);
            add_review_btn.setVisible(false);
        }

        reviews_listview.setCellFactory(lv -> new ListCell<String>() {
            {
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });
    }


    private void changePage(int page) throws IOException {

        page_label.setText((page + 1) + "/" + total_pages);

        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.send("getReviews");
        Communicator.send(Integer.toString(EditingRestaurant.getId()));
        Communicator.send(Integer.toString(page));

        String sizeStr = Communicator.read();
        if (sizeStr == null) fallback();
        int size = Integer.parseInt(sizeStr);

        String[] stars = new String[size];
        String[] texts = new String[size];
        String[] replies = new String[size];
        reviews_ids = new String[size];

        for (int i = 0; i < size; i++) {
            reviews_ids[i] = Communicator.read();
            stars[i] = Communicator.read();
            texts[i] = Communicator.read();

            String hasReply = Communicator.read();
            if (hasReply.equals("y"))
                replies[i] = Communicator.read();
        }

        String[] compact = new String[size];
        for (int i = 0; i < size; i++) {
            compact[i] = stars[i] + "/5 " + texts[i] +
                    (replies[i] == null ? "" : "\nRisposta: " + replies[i]);
        }

        reviews_listview.getItems().setAll(compact);
        add_review_btn.setDisable(false);
    }


    private void fallback() throws IOException {
        SceneManager.setAppWarning("Il server non è raggiungibile");
        SceneManager.changeScene("App");
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

        if (is_restaurateur) {
            int index = reviews_listview.getSelectionModel().getSelectedIndex();
            if (index >= 0)
                EditingRestaurant.setReviewId(Integer.parseInt(reviews_ids[index]));
        }

        SceneManager.changeScene("WriteReview");
    }


    @FXML
    private void checkSelected() {
        add_review_btn.setDisable(
                reviews_listview.getSelectionModel().getSelectedIndex() < 0
        );
    }


    @FXML
    private void goBack() throws IOException {

        if (!is_logged) {
            SceneManager.changeScene("ViewRestaurants");
            return;
        }

        if (is_restaurateur)
            SceneManager.changeScene("MyRestaurants");
        else
            SceneManager.changeScene("ViewRestaurantInfo");
    }
}
