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

    public static boolean addUser(String nome, String cognome, String username, String hash, long data_nascita_time, boolean is_ristoratore) throws SQLException {
        //checks if birth date is present
        String sql = data_nascita_time < 0 ?
            "INSERT INTO utenti(nome, cognome, username, password, is_ristoratore) VALUES (?, ?, ?, ?, ?)" :
            "INSERT INTO utenti(nome, cognome, username, password, is_ristoratore, data_nascita) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, nome);
        statement.setString(2, cognome);
        statement.setString(3, username);
        statement.setString(4, hash);
        statement.setBoolean(5, is_ristoratore);

        if(data_nascita_time >= 0)
            statement.setDate(6, new Date(data_nascita_time));

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

    public static boolean addRestaurant(int user_id, String name, String nation, String city, String address, double latitude, double longitude, int price, boolean has_delivery, boolean has_online) throws SQLException {
        String sql = "INSERT INTO \"RistorantiTheKnife\"(nome, nazione, citta, indirizzo, latitudine, longitudine, fascia_prezzo, servizio_delivery, prenotazione_online, proprietario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            String has_delivery = result.getBoolean("servizio_delivery") ? "y" : "n";
            String has_online = result.getBoolean("prenotazione_online") ? "y" : "n";

            return new String[]{name, nation, city, address, latitude, longitude, avg_price, has_delivery, has_online};
        }

        return null;
    }

    public static boolean hasAccess(int user_id, int restaurant_id) throws SQLException {
        String sql = "SELECT 1 FROM \"RistorantiTheKnife\" r JOIN utenti u ON proprietario = u.id WHERE r.id = ? AND u.id = ?";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, restaurant_id);
        statement.setInt(2, user_id);

        ResultSet result = statement.executeQuery();

        return result.next();
    }

    public static boolean editRestaurant(int restaurant_id, String name, String nation, String city, String address, double latitude, double longitude, int price, boolean has_delivery, boolean has_online) throws SQLException {
        String sql = "UPDATE \"RistorantiTheKnife\" SET nome = ?, nazione = ?, citta = ?, indirizzo = ?, latitudine = ?, longitudine = ?, fascia_prezzo = ?, servizio_delivery = ?, prenotazione_online = ? WHERE id = ?";

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
        statement.setInt(10, restaurant_id);
        
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
}