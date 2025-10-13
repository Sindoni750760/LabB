package com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Gestisce la comunicazione tra il server e un singolo client tramite socket.
 * Ogni istanza di {@code ClientThread} rappresenta una connessione attiva.
 * Supporta comandi come login, registrazione, gestione ristoranti e ping.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ClientThread extends Thread {
    private String ip;
    private BufferedReader reader;
    private OutputStream os;
    private Socket socket;
    private int user_id = -1;

    /**
     * Inizializza il thread client con il socket fornito.
     * Imposta gli stream di input/output e avvia il thread.
     *
     * @param socket il socket connesso al client
     * @throws IOException se si verifica un errore durante la creazione degli stream
     */
    public ClientThread(Socket socket) throws IOException {
        //sets up the input/output streams
        ip = socket.getInetAddress().toString();
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        os = socket.getOutputStream();
        //start();
    }
    /**
     * Metodo principale del thread. Esegue la logica di comunicazione con il client.
     * Gestisce eccezioni come disconnessioni e errori di I/O o SQL.
     */
    public void run() {
        try {
            exec();
        } catch(InterruptedException | IOException | SQLException e) {
            log("client disconnected: " + ip + "(" + e.getMessage() + ")");
        } catch(Exception e) {
            // Catch-all to avoid unexpected runtime exceptions killing the server thread
            log("unexpected error: " + ip + "(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        } finally {
            try{
                if(reader != null) reader.close();
            } catch(IOException ignored){}
            try{
                if(os != null) os.close();
            } catch(IOException ignored){}
            try{
                if(socket != null && !socket.isClosed()) socket.close();
            } catch(IOException ignored){}
        }
    }

    /**
     * Registra un messaggio nel log della console, associato all'indirizzo IP del client.
     *
     * @param msg il messaggio da loggare
     */
    private void log(String msg) {
        System.out.println("[" + ip + "]" + msg);
    }

    /**
     * Legge una stringa dal client tramite lo stream di input.
     *
     * @return la stringa ricevuta
     * @throws IOException se si verifica un errore di lettura
     */
    private String readStream() throws IOException {
        String msg = reader.readLine();
        // reader.readLine() returns null if the client closed the connection
        if (msg == null) {
            log("client closed connection");
            return null;
        }
        log("[In<--]" + msg);
        return msg;
    }

    /**
     * Invia una stringa al client tramite lo stream di output.
     *
     * @param msg il messaggio da inviare
     * @throws IOException se si verifica un errore di scrittura
     */
    private void sendStream(String msg) throws IOException {
        log("[Out->]" + msg);
        os.write((msg + '\n').getBytes(StandardCharsets.UTF_8));
    }

    /**
    * Esegue la logica principale del thread, gestendo i comandi del client.
    * @throws InterruptedException se il thread viene interrotto
    * @throws IOException se si verifica un errore di I/O
    * @throws SQLException se si verifica un errore SQL
    */
    private void exec() throws InterruptedException, IOException, SQLException {
        log("Connected");

        String cmd = readStream();
        // if client closed connection, readStream() returns null -> exit
        if (cmd == null) return;
        //keeps reading the user command until it's "disconnect"
        while(!cmd.equals("disconnect")) {
            //operates based on the received command
            switch (cmd) {
                case "ping":
                    sendStream("pong");
                    break;
                case "register": //to register a user
                    String nome = readStream();
                    String cognome = readStream();
                    String username = readStream();
                    String password = readStream();
                    String data_nascita = readStream();
                    String latitude_string = readStream();
                    String longitude_string = readStream();
                    boolean is_ristoratore = readStream().equals("y");

                    sendStream(User.registerUser(nome, cognome, username, password, data_nascita, latitude_string, longitude_string, is_ristoratore));
                    break;
                case "login": //to authenticate the socket
                    username = readStream();
                    password = readStream();

                    int logged_user_id = User.loginUser(username, password);
                    if(logged_user_id > 0) {
                        sendStream("ok");
                        user_id = logged_user_id;
                    }
                    else if(logged_user_id == -1) //no user was found with given username
                        sendStream("username");
                    else if(logged_user_id == -2) //password mismatch
                        sendStream("password");
                    break;
                case "getUserInfo": //send current logged in user info to the client
                    if(user_id < 1)
                        sendStream("unauthorized");
                    else {
                        String[] user_info = DBHandler.getUserInfo(user_id);
                        sendStream(user_info[0]);
                        sendStream(user_info[1]);
                        sendStream(user_info[2]);
                    }
                    break;
                case "addRestaurant":
                    //if the user isn't a restaurator, he cannot add a restaurant
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    String name = readStream(),
                    nation = readStream(),
                    city = readStream(),
                    address = readStream();
                    latitude_string = readStream();
                    longitude_string = readStream();
                    String avg_price_string = readStream(), categories = readStream();
                    boolean has_delivery = readStream().equals("y"), has_online = readStream().equals("y");

                    //if some data is empty, replies with the "missing" error code
                    if(name.trim().isEmpty() || nation.trim().isEmpty() || city.trim().isEmpty() || address.trim().isEmpty() || categories.trim().isEmpty()) {
                        sendStream("missing");
                        break;
                    }

                    double latitude, longitude;
                    try {
                        latitude = Double.parseDouble(latitude_string);
                        longitude = Double.parseDouble(longitude_string);
                    } catch (NumberFormatException e) {
                        //if the format of the coordinates is incorrect, replies with the "coordinates" error code
                        sendStream("coordinates");
                        break;
                    }

                    int price;
                    try {
                        price = Integer.parseInt(avg_price_string);
                    } catch (NumberFormatException e) {
                        //if the format of the price is incorrect, replies with the "price_format" error code
                        sendStream("price_format");
                        break;
                    }

                    if(price < 0) {
                        //if the price is negative, replies with the "price_negative" error code
                        sendStream("price_negative");
                        break;
                    }

                    //tries to add a restaurant, replies with the "error" error code if something goes wrong
                    if(DBHandler.addRestaurant(user_id, name, nation, city, address, latitude, longitude, price, categories, has_delivery, has_online))
                        sendStream("ok");
                    else
                        sendStream("error");
                    break;
                case "editRestaurant": //same input control and error codes from "addRestaurant"
                    //if the user isn't a restaurator, he cannot edit a restaurant
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    int id = Integer.parseInt(readStream());

                    if(!DBHandler.hasAccess(user_id, id))
                        sendStream("no access");
                    else {
                        name = readStream();
                        nation = readStream();
                        city = readStream();
                        address = readStream();
                        latitude_string = readStream();
                        longitude_string = readStream();
                        avg_price_string = readStream();
                        categories = readStream();
                        has_delivery = readStream().equals("y");
                        has_online = readStream().equals("y");

                        if(name.trim().isEmpty() || nation.trim().isEmpty() || city.trim().isEmpty() || address.trim().isEmpty() || categories.trim().isEmpty()) {
                            sendStream("missing");
                            break;
                        }

                        try {
                            latitude = Double.parseDouble(latitude_string);
                            longitude = Double.parseDouble(longitude_string);
                        } catch (NumberFormatException e) {
                            sendStream("coordinates");
                            break;
                        }

                        try {
                            price = Integer.parseInt(avg_price_string);
                        } catch (NumberFormatException e) {
                            sendStream("price_format");
                            break;
                        }

                        if(price < 0) {
                            sendStream("price_negative");
                            break;
                        }

                        if(DBHandler.editRestaurant(id, name, nation, city, address, latitude, longitude, price, categories, has_delivery, has_online))
                            sendStream("ok");
                        else
                            sendStream("error");
                    }
                    break;
                case "deleteRestaurant":
                    //if the user isn't a restaurator, he cannot delete a restaurants
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());

                    //deletes the selected restaurant if the restaurator is the owner
                    if(!DBHandler.hasAccess(user_id, id))
                        sendStream("no access");
                    else {
                        if(DBHandler.deleteRestaurant(id))
                            sendStream("ok");
                        else
                            sendStream("error");
                    }
                    break;
                case "getMyRestaurants":
                    //if the user isn't a restaurator, he cannot get his restaurants
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    //fetches restaurants data from the db
                    int page = Integer.parseInt(readStream());
                    String[][] restaurants_info = DBHandler.getUserRestaurants(user_id, page);
                    sendStream(Integer.toString(restaurants_info.length));

                    //send the data sequentially to the client
                    for(String[] restaurant_info : restaurants_info) {
                        sendStream(restaurant_info[0]);
                        sendStream(restaurant_info[1]);
                    }

                    break;
                case "getMyRestaurantsPages": //send the number of MyRestaurants pages to the client
                    //if the user isn't a restaurator, he cannot get his restaurants
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    sendStream(Integer.toString(DBHandler.getUserRestaurantsPages(user_id)));
                    break;
                case "getRestaurantInfo": //sends single restaurant info to the client
                    id = Integer.parseInt(readStream());

                    String[] restaurant_info = DBHandler.getRestaurantInfo(id);

                    if(restaurant_info == null)
                        sendStream("not found");
                    else for(String info : restaurant_info)
                        sendStream(info);
                    break;
                case "getRestaurants":
                    //if the user is a restaurator, he cannot get all the restaurants
                    if(user_id > 0 && DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    //reads the search filters and uses it to get the restaurants with those filters
                    page = Integer.parseInt(readStream());
                    latitude_string = readStream();
                    longitude_string = readStream();
                    String range_km = readStream();
                    String price_min = readStream();
                    String price_max = readStream();
                    has_delivery = readStream().equals("y");
                    has_online = readStream().equals("y");
                    String stars_min = readStream();
                    String stars_max = readStream();
                    String category = null;
                    if(readStream().equals("y"))
                        category = readStream();
                    boolean near_me = readStream().equals("y");

                    boolean favourites = false;
                    if(user_id > 0)
                        favourites = readStream().equals("y");

                    String[][] restaurants = Restaurant.getRestaurantsWithFilter(page, latitude_string, longitude_string, range_km, price_min, price_max, has_delivery, has_online, stars_min, stars_max, favourites ? user_id : -1, category, near_me ? user_id : -1);

                    if(restaurants[0][0].equals("error")) {
                        sendStream(restaurants[0][1]);
                        break;
                    }

                    sendStream("ok");

                    for(String[] restaurant : restaurants) {
                        sendStream(restaurant[0]);
                        sendStream(restaurant[1]);
                    }
                    break;
                case "addFavourite":
                    //if the user isn't a customer, he cannot add favourites
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    sendStream(DBHandler.setFavourite(user_id, id, true) ? "ok" : "error");

                    break;
                case "removeFavourite":
                    //if the user isn't a customer, he cannot remove favourites
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    sendStream(DBHandler.setFavourite(user_id, id, false) ? "ok" : "error");

                    break;
                case "isFavourite":
                    //if the user isn't a customer, he cannot look out if a restaurant is in his favourites
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    sendStream(DBHandler.isFavourite(user_id, id) ? "y" : "n");

                    break;
                case "addReview":
                    //if the user isn't a customer, he cannot review a restaurant
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    int stars = Integer.parseInt(readStream());
                    String text = readStream();

                    if(stars < 1 || stars > 5) {
                        sendStream("stars");
                        break;
                    }

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    //if the restaurant isn't found
                    if(DBHandler.getRestaurantInfo(id) == null) {
                        sendStream("not found");
                        break;
                    }

                    sendStream(DBHandler.addReview(user_id, id, stars, text) ? "ok" : "error");

                    break;
                case "getMyReview": //send review info of the indicated restaurant to the client
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());

                    for(String s : DBHandler.getUserReview(user_id, id))
                        sendStream(s);

                    break;
                case "editReview": //edits the review of the indicated review
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    stars = Integer.parseInt(readStream());
                    text = readStream();

                    if(stars < 1 || stars > 5) {
                        sendStream("stars");
                        break;
                    }

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    sendStream(DBHandler.editReview(user_id, id, stars, text) ? "ok" : "error");
                    break;
                case "removeReview": //deletes the indicated review
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());

                    sendStream(DBHandler.removeReview(user_id, id) ? "ok" : "error");
                    break;
                case "getReviewsPages":
                    id = Integer.parseInt(readStream());
                    sendStream(Integer.toString(DBHandler.getReviewsPages(id)));
                    break;
                case "getReviews": //send the reviews of the indicated restaurant to the client
                    id = Integer.parseInt(readStream());
                    page = Integer.parseInt(readStream());

                    String[][] reviews = DBHandler.getReviews(id, page);

                    sendStream(Integer.toString(reviews.length));

                    for(String[] review : reviews) {
                        sendStream(review[0]);
                        sendStream(review[1]);
                        sendStream(review[2]);
                        if(review[3] == null)
                            sendStream("n");
                        else {
                            sendStream("y");
                            sendStream(review[3]);
                        }
                    }

                    break;
                case "addResponse": //adds a response to a review (restaurator)
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    text = readStream();

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    sendStream(DBHandler.addResponse(id, text) ? "ok" : "error");
                    break;
                case "getResponse": //sends indicated response info to the client (restaurator)
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    String response_text = DBHandler.getResponse(id);
                    if(response_text == null)
                        sendStream("empty");
                    else {
                        sendStream("ok");
                        sendStream(response_text);
                    }
                    break;
                case "editResponse": //edits the indicated response (restaurator)
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    text = readStream();

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    sendStream(DBHandler.editResponse(id, text) ? "ok" : "error");
                    break;
                case "removeResponse": //deletes the response (restaurator)
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    sendStream(DBHandler.removeResponse(id) ? "ok" : "error");
                    break;
                case "getMyReviewsPages": //send the number of pages of the reviews written by the user to the client
                    //if the user isn't a customer, he cannot get his reviews
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    int num_pages = DBHandler.getUserReviewsPages(user_id);

                    if(num_pages < 0)
                        sendStream("error");
                    else {
                        sendStream("ok");
                        sendStream(Integer.toString(num_pages));
                    }

                    break;
                case "getMyReviews": //sends the info of the reviews written by the user to the client
                    //if the user isn't a customer, he cannot get his reviews
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    page = Integer.parseInt(readStream());

                    //review_format: {"restaurant name", "given stars", "review text"}
                    String[][] my_reviews = DBHandler.getUserReviews(user_id, page);

                    sendStream(Integer.toString(my_reviews.length));

                    for(String[] review : my_reviews) {
                        sendStream(review[0]);
                        sendStream(review[1]);
                        sendStream(review[2]);
                    }

                    break;
                case "logout": //deauthenticates the socket
                    user_id = -1;
                    sendStream("ok");
                    break;
                default:
                    sendStream("Unknown command");
                    break;
            }
            cmd = readStream();
            if (cmd == null) return; // client disconnected
        }
    }
}