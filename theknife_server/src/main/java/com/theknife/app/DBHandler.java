//https://www.tembo.io/docs/getting-started/postgres_guides/connecting-to-postgres-with-java
package com.theknife.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DBHandler {
    private static Connection connection = null;

    //handles the connection to the database
    public static boolean connect(String jdbcUrl, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection(jdbcUrl, username, password);

            System.out.println("Successfully connected to the database");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Connection to database failed: " + e.getMessage());
            return false;
        }
    }

    public static int initDB() throws SQLException, IOException {
        //checks if the db is initialized
        boolean initialized = true;
        try {
            connection.createStatement().execute("SELECT 1 FROM utenti");
        } catch (SQLException e) {
            initialized = false;
        }

        if(initialized)
            return 2;

        //loads the sql file
        InputStream is = DBHandler.class.getResourceAsStream("/init-db.sql");

        if(is == null) {
            System.err.println("Sql file \"init-db.sql\" not found");
            return 1;
        }

        String statement = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        //creates the tables
        connection.createStatement().execute(statement);
        return 0;
    }

    public static boolean addUser(String nome, String cognome, String username, String hash, long data_nascita_time, double latitude, double longitude, boolean is_ristoratore) throws SQLException {
        //checks if birth date is present
        String sql = data_nascita_time < 0 ?
            "INSERT INTO utenti(nome, cognome, username, password, latitudine_domicilio, longitudine_domicilio, is_ristoratore) VALUES (?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO utenti(nome, cognome, username, password, latitudine_domicilio, longitudine_domicilio, is_ristoratore, data_nascita) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, nome);
        statement.setString(2, cognome);
        statement.setString(3, username);
        statement.setString(4, hash);
        statement.setDouble(5, latitude);
        statement.setDouble(6, longitude);
        statement.setBoolean(7, is_ristoratore);

        if(data_nascita_time >= 0)
            statement.setDate(8, new Date(data_nascita_time));

        try {
            statement.executeUpdate();
        } catch (SQLException e) {
            //username is already in use
            return false;
        }

        //returns true if the insertion was succesfull
        return true;
    }

    public static String[] getUserLoginInfo(String username) throws SQLException {
        String sql = "SELECT id, password FROM utenti WHERE username = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, username);

        ResultSet result = statement.executeQuery();

        if(result.next()) {
            int id = result.getInt("id");
            String password = result.getString("password");

            return new String[]{Integer.toString(id), password};
        }

        //the username was not found in the database
        return null;
    }

    public static String[] getUserInfo(int id) throws SQLException {
        String sql = "SELECT nome, cognome, is_ristoratore FROM utenti WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, id);;

        ResultSet result = statement.executeQuery();

        if(result.next()) {
            String name = result.getString("nome");
            String surname = result.getString("cognome");
            boolean is_restaurateur = result.getBoolean("is_ristoratore");

            return new String[]{name, surname, is_restaurateur ? "y" :"n"};
        }

        //the user was not found in the database
        return null;
    }

    public static boolean addRestaurant(int user_id, String name, String nation, String city, String address, double latitude, double longitude, int price, String categories, boolean has_delivery, boolean has_online) throws SQLException {
        String sql = "INSERT INTO \"RistorantiTheKnife\"(nome, nazione, citta, indirizzo, latitudine, longitudine, fascia_prezzo, servizio_delivery, prenotazione_online, proprietario, tipo_cucina) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, name);
        statement.setString(2, nation);
        statement.setString(3, city);
        statement.setString(4, address);
        statement.setDouble(5, latitude);
        statement.setDouble(6, longitude);
        statement.setInt(7, price);
        statement.setBoolean(8, has_delivery);
        statement.setBoolean(9, has_online);
        statement.setInt(10, user_id);
        statement.setString(11, categories);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //unknown error (shouldn't happen)
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static int getUserRestaurantsPages(int user_id) throws SQLException {
        String sql = "SELECT COUNT(*) AS num_restaurants FROM \"RistorantiTheKnife\" WHERE proprietario = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);

        ResultSet result = statement.executeQuery();

        result.next();

        int n_restaurants = result.getInt("num_restaurants");

        return n_restaurants > 0 ? (n_restaurants - 1) / 10 + 1: 0;
    }

    public static String[][] getUserRestaurants(int user_id, int page) throws SQLException {
        int offset = page * 10;
        String sql = "SELECT id, nome FROM \"RistorantiTheKnife\" WHERE proprietario = ? LIMIT 10 OFFSET ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, offset);

        ResultSet result = statement.executeQuery();

        List<String[]> restaurants = new LinkedList<String[]>();

        while(result.next()) {
            String id = result.getString("id");
            String name = result.getString("nome");
            restaurants.add(new String[]{id, name});
        }

        return restaurants.toArray(new String[][]{});
    }

    public static String[] getRestaurantInfo(int id) throws SQLException {
        String sql = "SELECT * FROM \"RistorantiTheKnife\" WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, id);

        ResultSet result = statement.executeQuery();

        if(result.next()) {
            String name = result.getString("nome");
            String nation = result.getString("nazione");
            String city = result.getString("citta");
            String address = result.getString("indirizzo");
            String latitude = result.getString("latitudine");
            String longitude = result.getString("longitudine");
            String avg_price = result.getString("fascia_prezzo");
            String categories = result.getString("tipo_cucina");
            String has_delivery = result.getBoolean("servizio_delivery") ? "y" : "n";
            String has_online = result.getBoolean("prenotazione_online") ? "y" : "n";

            //getting average stars and number of reviews
            sql = "SELECT ROUND(AVG(stelle), 1) AS stars_avg, COUNT(*) AS n_reviews from recensioni WHERE id_ristorante = ? GROUP BY id_ristorante";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            result = statement.executeQuery();

            String avg_stars = "0", n_reviews = "0";
            if(result.next()) {
                avg_stars = result.getString("stars_avg");
                n_reviews = result.getString("n_reviews");
            }

            return new String[]{name, nation, city, address, latitude, longitude, avg_price, categories, has_delivery, has_online, avg_stars, n_reviews};
        }

        return null;
    }

    //checks if user is owner of a restaurant
    public static boolean hasAccess(int user_id, int restaurant_id) throws SQLException {
        String sql = "SELECT 1 FROM \"RistorantiTheKnife\" r JOIN utenti u ON proprietario = u.id WHERE r.id = ? AND u.id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, restaurant_id);
        statement.setInt(2, user_id);

        ResultSet result = statement.executeQuery();

        return result.next();
    }

    public static boolean editRestaurant(int restaurant_id, String name, String nation, String city, String address, double latitude, double longitude, int price, String categories, boolean has_delivery, boolean has_online) throws SQLException {
        String sql = "UPDATE \"RistorantiTheKnife\" SET nome = ?, nazione = ?, citta = ?, indirizzo = ?, latitudine = ?, longitudine = ?, fascia_prezzo = ?, servizio_delivery = ?, prenotazione_online = ?, tipo_cucina = ? WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, name);
        statement.setString(2, nation);
        statement.setString(3, city);
        statement.setString(4, address);
        statement.setDouble(5, latitude);
        statement.setDouble(6, longitude);
        statement.setInt(7, price);
        statement.setBoolean(8, has_delivery);
        statement.setBoolean(9, has_online);
        statement.setString(10, categories);
        statement.setInt(11, restaurant_id);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //unknown error (shouldn't happen)
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean deleteRestaurant(int restaurant_id) throws SQLException {
        String sql = "DELETE FROM \"RistorantiTheKnife\" WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, restaurant_id);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //unknown error (shouldn't happen)
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //function used to set variable length and type parameters in a prepared statement
    private static void setParameters(PreparedStatement statement, List<String> parameters, List<String> parameters_types) throws NumberFormatException, SQLException {
        for(int i = 0; i < parameters.size(); i++) {
            switch(parameters_types.get(i)) {
                case "double":
                    statement.setDouble(i + 1, Double.parseDouble(parameters.get(i)));
                    break;
                case "int":
                    statement.setInt(i + 1, Integer.parseInt(parameters.get(i)));
                    break;
                case "string":
                    statement.setString(i + 1, parameters.get(i));
                    break;
            }
        }
    }

    public static String[][] getRestaurantsWithFilter(int page, double latitude, double longitude, double range_km, int price_min, int price_max, boolean has_delivery, boolean has_online, double stars_min, double stars_max, int favourite_id, String category) throws SQLException {
        int offset = page * 10;
        String sql = " FROM \"RistorantiTheKnife\" r";
        List<String> parameters = new LinkedList<String>();
        List<String> parameters_types = new LinkedList<String>();

        if(favourite_id > 0) {
            sql += " JOIN preferiti ON id = id_ristorante WHERE id_utente = ?";
            parameters.add(Integer.toString(favourite_id));
            parameters_types.add("int");
        } else
            sql += " WHERE 1 = 1";

        if(latitude >= 0) {
            //converting km to degrees
            range_km /= 111;

            sql += " AND SQRT((latitudine - ?)*(latitudine - ?) + (longitudine - ?)*(longitudine - ?)) < ?";
            parameters.add(Double.toString(latitude));
            parameters.add(Double.toString(latitude));
            parameters.add(Double.toString(longitude));
            parameters.add(Double.toString(longitude));
            parameters.add(Double.toString(range_km));

            parameters_types.add("double");
            parameters_types.add("double");
            parameters_types.add("double");
            parameters_types.add("double");
            parameters_types.add("double");
        }

        if(price_min >= 0) {
            sql += " AND fascia_prezzo >= ?";
            parameters.add(Integer.toString(price_min));
            parameters_types.add("int");
        }

        if(price_max >= 0) {
            sql += " AND fascia_prezzo <= ?";
            parameters.add(Integer.toString(price_max));
            parameters_types.add("int");
        }

        if(has_delivery)
            sql += " AND servizio_delivery = true";

        if(has_online)
            sql += " AND prenotazione_online = true";

        String stars_query = "(SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id GROUP BY id_ristorante)";
        if(stars_min >= 0) {
            sql += " AND " + stars_query + " >= ?";
            parameters.add(Double.toString(stars_min));
            parameters_types.add("double");
        }

        if(stars_max >= 0) {
            sql += " AND " + stars_query + " <= ?";
            parameters.add(Double.toString(stars_max));
            parameters_types.add("double");
        }

        if(category != null) {
            sql += " AND LOWER(tipo_cucina) LIKE LOWER(?)";
            parameters.add('%' + category + '%');
            parameters_types.add("string");
        }

        //to obtain the number of pages
        String sql_unlimited = "SELECT COUNT(*) AS num" + sql;

        PreparedStatement statement = connection.prepareStatement(sql_unlimited);
        setParameters(statement, parameters, parameters_types);

        ResultSet result = statement.executeQuery();
        result.next();
        int results = result.getInt("num");
        int pages = results > 0 ? (results - 1) / 10 + 1 : 0;

        sql += " LIMIT 10 OFFSET ?";
        parameters.add(Integer.toString(offset));
        parameters_types.add("int");

        statement = connection.prepareStatement("SELECT id, nome" + sql);

        setParameters(statement, parameters, parameters_types);

        result = statement.executeQuery();
        List<String[]> restaurants = new LinkedList<String[]>();
        restaurants.add(new String[]{"", ""});

        while(result.next()) {
            String id = result.getString("id");
            String nome = result.getString("nome");
            restaurants.add(new String[]{id, nome});
        }

        restaurants.set(0, new String[]{Integer.toString(pages), Integer.toString(restaurants.size() - 1)});

        return restaurants.toArray(new String[][]{});
    }

    public static double[] getUserPosition(int user_id) throws SQLException {
        String sql = "SELECT latitudine_domicilio AS la, longitudine_domicilio AS lo FROM utenti WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);

        ResultSet result = statement.executeQuery();

        if(result.next())
            return new double[]{result.getDouble("la"), result.getDouble("lo")};

        return null;
    }

    public static boolean setFavourite(int user_id, int id_restaurant, boolean set_favourite) throws SQLException {
        //adds or removes the favourite based on the passed parameter value
        String sql = set_favourite ? "INSERT INTO preferiti(id_utente, id_ristorante) VALUES(?, ?)" : "DELETE FROM preferiti WHERE id_utente = ? AND id_ristorante = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, id_restaurant);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //if the user adds/removes a favourite he cannot add/remove
            return false;
        }

        return true;
    }

    public static boolean isFavourite(int user_id, int id_restaurant) throws SQLException {
        String sql = "SELECT 1 FROM preferiti WHERE id_utente = ? AND id_ristorante = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, id_restaurant);

        ResultSet result = statement.executeQuery();

        return result.next();
    }

    public static boolean addReview(int user_id, int rest_id, int rating, String text) throws SQLException {
        String sql = "INSERT INTO recensioni(id_utente, id_ristorante, stelle, testo) VALUES(?, ?, ?, ?)";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, rest_id);
        statement.setInt(3, rating);
        statement.setString(4, text);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //shouldn't happen
            return false;
        }

        return true;
    }

    public static boolean removeReview(int user_id, int rest_id) throws SQLException {
        String sql = "DELETE FROM recensioni WHERE id_utente = ? AND id_ristorante = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, rest_id);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //shouldn't happen
            return false;
        }

        return true;
    }

    public static String[] getUserReview(int user_id, int rest_id) throws SQLException {
        String sql = "SELECT * FROM recensioni WHERE id_utente = ? AND id_ristorante = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, rest_id);

        ResultSet result = statement.executeQuery();

        if(result.next()) {
            String stars = result.getString("stelle");
            String text = result.getString("testo");
            return new String[]{stars, text};
        }

        //no review was found
        return new String[]{"0", ""};
    }

    public static boolean editReview(int user_id, int rest_id, int rating, String text) throws SQLException {
        String sql = "UPDATE recensioni SET stelle = ?, testo = ? WHERE id_utente = ? AND id_ristorante = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, rating);
        statement.setString(2, text);
        statement.setInt(3, user_id);
        statement.setInt(4, rest_id);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //shouldn't happen
            return false;
        }

        return true;
    }

    public static int getReviewsPages(int id) throws SQLException {
        String sql = "SELECT COUNT(*) AS num FROM recensioni WHERE id_ristorante = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, id);

        ResultSet result = statement.executeQuery();
        result.next();

        int num = result.getInt("num");

        return num > 0 ? (num - 1) / 10 + 1 : 0;
    }

    public static String[][] getReviews(int id, int page) throws SQLException {
        int offset = page * 1;
        String sql = "SELECT *, (SELECT testo FROM risposte WHERE id_recensione = r.id) AS risposta FROM recensioni r WHERE id_ristorante = ? LIMIT 10 OFFSET ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, id);
        statement.setInt(2, offset);

        ResultSet result = statement.executeQuery();

        List<String[]> reviews = new LinkedList<String[]>();

        while(result.next()) {
            String review_id = result.getString("id"),
            stars = result.getString("stelle"),
            testo = result.getString("testo"),
            risposta = result.getString("risposta");
            reviews.add(new String[]{review_id, stars, testo, risposta});
        }

        return reviews.toArray(new String[][]{});
    }

    //checks if the restaurator is owner of the reviewd restaurant
    public static boolean canRespond(int user_id, int review_id) throws SQLException {
        String sql = "SELECT 1 FROM recensioni re JOIN \"RistorantiTheKnife\" ri ON id_ristorante = ri.id WHERE re.id = ? AND proprietario = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, review_id);
        statement.setInt(2, user_id);

        ResultSet result = statement.executeQuery();

        return result.next();
    }

    public static boolean addResponse(int review_id, String text) throws SQLException {
        String sql = "INSERT INTO risposte(id_recensione, testo) VALUES(?, ?)";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, review_id);
        statement.setString(2, text);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //shouldn't happen
            return false;
        }

        return true;
    }

    public static String getResponse(int review_id) throws SQLException {
        String sql = "SELECT testo FROM risposte WHERE id_recensione = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, review_id);

        ResultSet result = statement.executeQuery();

        if(result.next())
            return result.getString("testo");

        return null;
    }

    public static boolean editResponse(int review_id, String text) throws SQLException {
        String sql = "UPDATE risposte SET testo = ? WHERE id_recensione = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, text);
        statement.setInt(2, review_id);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //shouldn't happen
            return false;
        }

        return true;
    }

    public static boolean removeResponse(int review_id) throws SQLException {
        String sql = "DELETE FROM risposte WHERE id_recensione = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, review_id);

        try {
            statement.executeUpdate();
        } catch(SQLException e) {
            //shouldn't happen
            return false;
        }

        return true;
    }

    public static int getUserReviewsPages(int user_id) throws SQLException {
        String sql = "SELECT COUNT(*) AS num FROM recensioni WHERE id_utente = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);

        ResultSet result = statement.executeQuery();

        if(result.next()) {
            int num_pages = result.getInt("num");
            return num_pages > 0 ? (num_pages - 1) / 10 + 1 : 0;
        }

        //shouldn't reach this part of the code
        return -1;
    }

    public static String[][] getUserReviews(int user_id, int page) throws SQLException {
        int offset = page * 10;
        String sql = "SELECT nome, stelle, testo FROM \"RistorantiTheKnife\" ri JOIN recensioni re ON ri.id = id_ristorante WHERE id_utente = ? LIMIT 10 OFFSET ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, user_id);
        statement.setInt(2, offset);

        ResultSet result = statement.executeQuery();

        List<String[]> reviews = new LinkedList<String[]>();

        while(result.next()) {
            String restaurant_name = result.getString("nome");
            String stars = result.getString("stelle");
            String text = result.getString("testo");
            reviews.add(new String[]{restaurant_name, stars, text});
        }

        return reviews.toArray(new String[][]{});
    }
}