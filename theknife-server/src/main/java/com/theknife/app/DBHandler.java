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
}