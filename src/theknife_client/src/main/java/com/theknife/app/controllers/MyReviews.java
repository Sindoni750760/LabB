package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.ClientLogger;
import com.theknife.app.Communicator;
import com.theknife.app.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Controller per la schermata "MyReviews".
 * Gestisce la visualizzazione delle recensioni lasciate dall'utente,
 * con supporto alla paginazione e formattazione del testo.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */

public class MyReviews implements OnlineChecker {

    private static int current_page;
    private static int total_pages;

    @FXML private ListView<String> reviews_listview;
    @FXML private Label no_reviews_label;
    @FXML private Label pages_label;
    @FXML private Button prev_btn, next_btn;

    @FXML
    private void initialize() throws IOException {
        ClientLogger.getInstance().info("MyReviews initialized");
        current_page = 0;

        prev_btn.setDisable(true);
        next_btn.setDisable(true);

        if (!checkOnline()) {
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Il server non è raggiungibile");
            return;
        }

        Communicator.send("getMyReviewsPages");

        String res = Communicator.read();
        if (res == null) {
            fallback();
            return;
        }

        if (!res.equals("ok")) {
            ClientLogger.getInstance().warning("Server returned error for getMyReviewsPages");
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Errore dal server");
            return;
        }

        String pagesStr = Communicator.read();
        if (pagesStr == null) {
            fallback();
            return;
        }

        total_pages = Integer.parseInt(pagesStr);

        if (total_pages > 0)
            changePage(0);
        else {
            no_reviews_label.setVisible(true);
            no_reviews_label.setText("Non hai ancora recensioni");
        }

        reviews_listview.setCellFactory(lv -> new ListCell<String>() {
            {
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null)
                    setText(null);
                else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.changeScene("App");
    }

    private void changePage(int page) throws IOException {
        if (!checkOnline()) {
            return;
        }

        current_page = page;

        pages_label.setText((page + 1) + "/" + total_pages);

        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.send("getMyReviews");
        Communicator.send(Integer.toString(page));

        String sizeStr = Communicator.read();
        if (sizeStr == null) {
            fallback();
            return;
        }

        int size = Integer.parseInt(sizeStr);

        String[] reviews_compact = new String[size];

        for (int i = 0; i < size; i++) {
            String restaurant_name = Communicator.read();
            String stars = Communicator.read();
            String text = Communicator.read();

            if (restaurant_name == null || stars == null || text == null) {
                fallback();
                return;
            }

            reviews_compact[i] =
                    "Nome ristorante: " + restaurant_name +
                            "\nValutazione: " + stars + "/5" +
                            "\nRecensione: " + text;
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

    @Override
    public javafx.scene.Node[] getInteractiveNodes() {
        return new javafx.scene.Node[]{
                reviews_listview, prev_btn, next_btn
        };
    }
}
