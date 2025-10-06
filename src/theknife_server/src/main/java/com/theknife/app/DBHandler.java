package com.theknife.app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * DBHandler rifattorizzato per usare HikariCP.
 * Ogni metodo prende una Connection dal pool e chiude tutte le risorse con try-with-resources.
 */
public class DBHandler {
    private static HikariDataSource ds = null;
    private static final String TARGET_DB = "theknife";
    private static String sql = "";
    private DBHandler() { /* utility class */ }

    /**
     * Connette al database. Se necessario, crea il database TARGET_DB tramite maintenance URL.
     *
     * @param jdbcUrl full JDBC URL (es. jdbc:postgresql://host:5432/theknife)
     * @param username DB username
     * @param password DB password
     * @return true se il DataSource è inizializzato correttamente
     */
    public static boolean connect(String jdbcUrl, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found: " + e.getMessage());
            return false;
        }

        boolean urlTargetsTheKnife = jdbcUrl.matches(".*/" + TARGET_DB + "(\\?.*)?$");
        if (!urlTargetsTheKnife) {
            int idx = jdbcUrl.lastIndexOf('/');
            if (idx < 0) {
                System.err.println("JDBC URL non valido: " + jdbcUrl);
                return false;
            }
            String base = jdbcUrl.substring(0, idx);
            String maintenanceUrl = base + "/postgres";
            String targetUrl = base + "/" + TARGET_DB;

            if (!ensureDatabaseExists(maintenanceUrl, username, password)) {
                return false;
            }

            return initDataSource(targetUrl, username, password);
        }

        // Se l'URL punta a theknife, testiamo la connessione e, se necessario, creiamo il DB
        try (Connection test = DriverManager.getConnection(jdbcUrl, username, password)) {
            // connessione OK -> inizializza pool
            return initDataSource(jdbcUrl, username, password);
        } catch (SQLException e) {
            // se DB non esiste (invalid_catalog_name = 3D000), proviamo a crearne uno tramite postgres
            if ("3D000".equals(e.getSQLState())) {
                int idx = jdbcUrl.lastIndexOf('/');
                String maintenanceUrl = (idx > 0 ? jdbcUrl.substring(0, idx) : jdbcUrl) + "/postgres";
                if (!ensureDatabaseExists(maintenanceUrl, username, password)) {
                    return false;
                }
                return initDataSource(jdbcUrl, username, password);
            }
            System.err.println("Connessione fallita: " + e.getMessage());
            return false;
        }
    }

    private static boolean initDataSource(String jdbcUrl, String username, String password) {
        try {
            if (ds != null && !ds.isClosed()) ds.close();

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(jdbcUrl);
            cfg.setUsername(username);
            cfg.setPassword(password);
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setPoolName("theknife-pool");
            cfg.addDataSourceProperty("cachePrepStmts", "true");
            cfg.addDataSourceProperty("prepStmtCacheSize", "250");
            cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            ds = new HikariDataSource(cfg);
            System.out.println("HikariCP pool inizializzato verso " + jdbcUrl);
            return true;
        } catch (Exception e) {
            System.err.println("Errore inizializzazione DataSource: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica o crea il database TARGET_DB utilizzando un maintenance URL (postgres).
     *
     * @param maintenanceUrl jdbc url del DB postgres di maintenance
     * @param username      username
     * @param password      password
     * @return true se il DB esiste o è stato creato correttamente
     */
    private static boolean ensureDatabaseExists(String maintenanceUrl, String username, String password) {
        try (Connection c = DriverManager.getConnection(maintenanceUrl, username, password)) {
            try (PreparedStatement check = c.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                check.setString(1, TARGET_DB);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) return true;
                }
            }

            if (!c.getAutoCommit()) c.setAutoCommit(true);
            try (PreparedStatement create = c.prepareStatement("CREATE DATABASE " + TARGET_DB)) {
                create.executeUpdate();
                System.out.println("Creato database '" + TARGET_DB + "'");
            }
            return true;
        } catch (SQLException e) {
            if ("42501".equals(e.getSQLState())) {
                System.err.println("Permessi insufficienti per creare database. Servono privilegi CREATEDB.");
            } else {
                System.err.println("Errore during ensureDatabaseExists: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Esegue lo script init-db.sql se necessario.
     *
     * @return 0 init eseguita, 1 file SQL mancante, 2 già inizializzato, -1 errore DataSource
     */
    public static int initDB() throws SQLException, IOException {
        if (ds == null) {
            System.err.println("DataSource non inizializzato. Chiama connect() prima di initDB().");
            return -1;
        }

        boolean initialized = true;
        try (Connection conn = ds.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.executeQuery("SELECT 1 FROM utenti");
            } catch (SQLException e) {
                initialized = false;
            }
        }

        if (initialized) return 2;

        InputStream is = DBHandler.class.getResourceAsStream("/init-db.sql");
        if (is == null) {
            System.err.println("Sql file \"init-db.sql\" not found");
            return 1;
        }

        String statement = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(statement);
        }

        return 0;
    }

    // ----------------- Helper per connessione -----------------

    @FunctionalInterface
    private interface SQLOp<T> { T run(Connection conn) throws SQLException; }

    private static <T> T withConnection(SQLOp<T> op) throws SQLException {
        if (ds == null) throw new SQLException("DataSource non inizializzato");
        try (Connection conn = ds.getConnection()) {
            return op.run(conn);
        }
    }

    // ----------------- Metodi DB (rifattorizzati) -----------------

    public static boolean addUser(String nome, String cognome, String username, String hash, long data_nascita_time, double latitude, double longitude, boolean is_ristoratore) throws SQLException {
        sql = data_nascita_time < 0 ?
                "INSERT INTO utenti(nome, cognome, username, password, latitudine_domicilio, longitudine_domicilio, is_ristoratore) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                "INSERT INTO utenti(nome, cognome, username, password, latitudine_domicilio, longitudine_domicilio, is_ristoratore, data_nascita) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, nome);
                st.setString(2, cognome);
                st.setString(3, username);
                st.setString(4, hash);
                st.setDouble(5, latitude);
                st.setDouble(6, longitude);
                st.setBoolean(7, is_ristoratore);
                if (data_nascita_time >= 0) st.setDate(8, new Date(data_nascita_time));
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false; // username già in uso o altro conflitto
            }
        });
    }

    public static String[] getUserLoginInfo(String username) throws SQLException {
        sql = "SELECT id, password FROM utenti WHERE username = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, username);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        return new String[]{Integer.toString(rs.getInt("id")), rs.getString("password")};
                    }
                    return null;
                }
            }
        });
    }

    public static String[] getUserInfo(int id) throws SQLException {
        sql = "SELECT nome, cognome, is_ristoratore FROM utenti WHERE id = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        return new String[]{rs.getString("nome"), rs.getString("cognome"), rs.getBoolean("is_ristoratore") ? "y" : "n"};
                    }
                    return null;
                }
            }
        });
    }

    public static boolean addRestaurant(int user_id, String name, String nation, String city, String address, double latitude, double longitude, int price, String categories, boolean has_delivery, boolean has_online) throws SQLException {
        sql = "INSERT INTO \"RistorantiTheKnife\"(nome, nazione, citta, indirizzo, latitudine, longitudine, fascia_prezzo, servizio_delivery, prenotazione_online, proprietario, tipo_cucina) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, name);
                st.setString(2, nation);
                st.setString(3, city);
                st.setString(4, address);
                st.setDouble(5, latitude);
                st.setDouble(6, longitude);
                st.setInt(7, price);
                st.setBoolean(8, has_delivery);
                st.setBoolean(9, has_online);
                st.setInt(10, user_id);
                st.setString(11, categories);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static int getUserRestaurantsPages(int user_id) throws SQLException {
        sql = "SELECT COUNT(*) AS num_restaurants FROM \"RistorantiTheKnife\" WHERE proprietario = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                try (ResultSet rs = st.executeQuery()) {
                    rs.next();
                    int n = rs.getInt("num_restaurants");
                    return n > 0 ? (n - 1) / 10 + 1 : 0;
                }
            }
        });
    }

    public static String[][] getUserRestaurants(int user_id, int page) throws SQLException {
        int offset = page * 10;
        sql = "SELECT id, nome FROM \"RistorantiTheKnife\" WHERE proprietario = ? LIMIT 10 OFFSET ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, offset);
                try (ResultSet rs = st.executeQuery()) {
                    List<String[]> list = new LinkedList<>();
                    while (rs.next()) list.add(new String[]{rs.getString("id"), rs.getString("nome")});
                    return list.toArray(new String[0][0]);
                }
            }
        });
    }

    public static String[] getRestaurantInfo(int id) throws SQLException {
        sql = "SELECT * FROM \"RistorantiTheKnife\" WHERE id = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("nome");
                        String nation = rs.getString("nazione");
                        String city = rs.getString("citta");
                        String address = rs.getString("indirizzo");
                        String lat = rs.getString("latitudine");
                        String lon = rs.getString("longitudine");
                        String avg_price = rs.getString("fascia_prezzo");
                        String categories = rs.getString("tipo_cucina");
                        String has_delivery = rs.getBoolean("servizio_delivery") ? "y" : "n";
                        String has_online = rs.getBoolean("prenotazione_online") ? "y" : "n";

                        String sql2 = "SELECT ROUND(AVG(stelle), 1) AS stars_avg, COUNT(*) AS n_reviews FROM recensioni WHERE id_ristorante = ? GROUP BY id_ristorante";
                        try (PreparedStatement st2 = conn.prepareStatement(sql2)) {
                            st2.setInt(1, id);
                            try (ResultSet r2 = st2.executeQuery()) {
                                String avg_stars = "0", n_reviews = "0";
                                if (r2.next()) {
                                    avg_stars = r2.getString("stars_avg");
                                    n_reviews = r2.getString("n_reviews");
                                }
                                return new String[]{name, nation, city, address, lat, lon, avg_price, categories, has_delivery, has_online, avg_stars, n_reviews};
                            }
                        }
                    }
                    return null;
                }
            }
        });
    }

    public static boolean hasAccess(int user_id, int restaurant_id) throws SQLException {
        sql = "SELECT 1 FROM \"RistorantiTheKnife\" r JOIN utenti u ON proprietario = u.id WHERE r.id = ? AND u.id = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, restaurant_id);
                st.setInt(2, user_id);
                try (ResultSet rs = st.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    public static boolean editRestaurant(int restaurant_id, String name, String nation, String city, String address, double latitude, double longitude, int price, String categories, boolean has_delivery, boolean has_online) throws SQLException {
        sql = "UPDATE \"RistorantiTheKnife\" SET nome = ?, nazione = ?, citta = ?, indirizzo = ?, latitudine = ?, longitudine = ?, fascia_prezzo = ?, servizio_delivery = ?, prenotazione_online = ?, tipo_cucina = ? WHERE id = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, name);
                st.setString(2, nation);
                st.setString(3, city);
                st.setString(4, address);
                st.setDouble(5, latitude);
                st.setDouble(6, longitude);
                st.setInt(7, price);
                st.setBoolean(8, has_delivery);
                st.setBoolean(9, has_online);
                st.setString(10, categories);
                st.setInt(11, restaurant_id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static boolean deleteRestaurant(int restaurant_id) throws SQLException {
        sql = "DELETE FROM \"RistorantiTheKnife\" WHERE id = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, restaurant_id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    private static void setParameters(PreparedStatement statement, List<String> parameters, List<String> parameters_types) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            switch (parameters_types.get(i)) {
                case "double":
                    statement.setDouble(i + 1, Double.parseDouble(parameters.get(i)));
                    break;
                case "int":
                    statement.setInt(i + 1, Integer.parseInt(parameters.get(i)));
                    break;
                case "string":
                default:
                    statement.setString(i + 1, parameters.get(i));
            }
        }
    }

    public static String[][] getRestaurantsWithFilter(int page, double latitude, double longitude, double range_km, int price_min, int price_max, boolean has_delivery, boolean has_online, double stars_min, double stars_max, int favourite_id, String category) throws SQLException {
        int offset = page * 10;
        sql = " FROM \"RistorantiTheKnife\" r";
        List<String> parameters = new LinkedList<>();
        List<String> parameters_types = new LinkedList<>();

        if (favourite_id > 0) {
            sql += " JOIN preferiti ON id = id_ristorante WHERE id_utente = ?";
            parameters.add(Integer.toString(favourite_id));
            parameters_types.add("int");
        } else sql += " WHERE 1 = 1";

        if (latitude >= 0) {
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

        if (price_min >= 0) {
            sql += " AND fascia_prezzo >= ?";
            parameters.add(Integer.toString(price_min));
            parameters_types.add("int");
        }

        if (price_max >= 0) {
            sql += " AND fascia_prezzo <= ?";
            parameters.add(Integer.toString(price_max));
            parameters_types.add("int");
        }

        if (has_delivery) sql += " AND servizio_delivery = true";
        if (has_online) sql += " AND prenotazione_online = true";

        String stars_query = "(SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id GROUP BY id_ristorante)";
        if (stars_min >= 0) {
            sql += " AND " + stars_query + " >= ?";
            parameters.add(Double.toString(stars_min));
            parameters_types.add("double");
        }
        if (stars_max >= 0) {
            sql += " AND " + stars_query + " <= ?";
            parameters.add(Double.toString(stars_max));
            parameters_types.add("double");
        }

        if (category != null) {
            sql += " AND LOWER(tipo_cucina) LIKE LOWER(?)";
            parameters.add('%' + category + '%');
            parameters_types.add("string");
        }

        // count and page
        return withConnection(conn -> {
            try (PreparedStatement countSt = conn.prepareStatement("SELECT COUNT(*) AS num" + sql)) {
                setParameters(countSt, parameters, parameters_types);
                try (ResultSet rs = countSt.executeQuery()) {
                    rs.next();
                    int results = rs.getInt("num");
                    int pages = results > 0 ? (results - 1) / 10 + 1 : 0;

                    parameters.add(Integer.toString(offset));
                    parameters_types.add("int");

                    try (PreparedStatement pageSt = conn.prepareStatement("SELECT id, nome" + sql + " LIMIT 10 OFFSET ?")) {
                        setParameters(pageSt, parameters, parameters_types);
                        try (ResultSet r2 = pageSt.executeQuery()) {
                            List<String[]> restaurants = new LinkedList<>();
                            restaurants.add(new String[]{"", ""});
                            while (r2.next()) {
                                restaurants.add(new String[]{r2.getString("id"), r2.getString("nome")});
                            }
                            restaurants.set(0, new String[]{Integer.toString(pages), Integer.toString(restaurants.size() - 1)});
                            return restaurants.toArray(new String[0][0]);
                        }
                    }
                }
            }
        });
    }

    public static double[] getUserPosition(int user_id) throws SQLException {
        sql = "SELECT latitudine_domicilio AS la, longitudine_domicilio AS lo FROM utenti WHERE id = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) return new double[]{rs.getDouble("la"), rs.getDouble("lo")};
                    return null;
                }
            }
        });
    }

    public static boolean setFavourite(int user_id, int id_restaurant, boolean set_favourite) throws SQLException {
        sql = set_favourite ? "INSERT INTO preferiti(id_utente, id_ristorante) VALUES(?, ?)" : "DELETE FROM preferiti WHERE id_utente = ? AND id_ristorante = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, id_restaurant);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static boolean isFavourite(int user_id, int id_restaurant) throws SQLException {
        sql = "SELECT 1 FROM preferiti WHERE id_utente = ? AND id_ristorante = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, id_restaurant);
                try (ResultSet rs = st.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    public static boolean addReview(int user_id, int rest_id, int rating, String text) throws SQLException {
        sql = "INSERT INTO recensioni(id_utente, id_ristorante, stelle, testo) VALUES(?, ?, ?, ?)";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, rest_id);
                st.setInt(3, rating);
                st.setString(4, text);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static boolean removeReview(int user_id, int rest_id) throws SQLException {
        sql = "DELETE FROM recensioni WHERE id_utente = ? AND id_ristorante = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, rest_id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static String[] getUserReview(int user_id, int rest_id) throws SQLException {
        sql = "SELECT * FROM recensioni WHERE id_utente = ? AND id_ristorante = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, rest_id);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) return new String[]{rs.getString("stelle"), rs.getString("testo")};
                    return new String[]{"0", ""};
                }
            }
        });
    }

    public static boolean editReview(int user_id, int rest_id, int rating, String text) throws SQLException {
        sql = "UPDATE recensioni SET stelle = ?, testo = ? WHERE id_utente = ? AND id_ristorante = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, rating);
                st.setString(2, text);
                st.setInt(3, user_id);
                st.setInt(4, rest_id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static int getReviewsPages(int id) throws SQLException {
        sql = "SELECT COUNT(*) AS num FROM recensioni WHERE id_ristorante = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    rs.next();
                    int num = rs.getInt("num");
                    return num > 0 ? (num - 1) / 10 + 1 : 0;
                }
            }
        });
    }

    public static String[][] getReviews(int id, int page) throws SQLException {
        int offset = page * 10;
        sql = "SELECT r.id, stelle, testo, (SELECT testo FROM risposte WHERE id_recensione = r.id) AS risposta FROM recensioni r WHERE id_ristorante = ? LIMIT 10 OFFSET ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, id);
                st.setInt(2, offset);
                try (ResultSet rs = st.executeQuery()) {
                    List<String[]> list = new LinkedList<>();
                    while (rs.next()) {
                        list.add(new String[]{rs.getString("id"), rs.getString("stelle"), rs.getString("testo"), rs.getString("risposta")});
                    }
                    return list.toArray(new String[0][0]);
                }
            }
        });
    }

    public static boolean canRespond(int user_id, int review_id) throws SQLException {
        sql = "SELECT 1 FROM recensioni re JOIN \"RistorantiTheKnife\" ri ON id_ristorante = ri.id WHERE re.id = ? AND proprietario = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, review_id);
                st.setInt(2, user_id);
                try (ResultSet rs = st.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    public static boolean addResponse(int review_id, String text) throws SQLException {
        sql = "INSERT INTO risposte(id_recensione, testo) VALUES(?, ?)";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, review_id);
                st.setString(2, text);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static String getResponse(int review_id) throws SQLException {
        sql = "SELECT testo FROM risposte WHERE id_recensione = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, review_id);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) return rs.getString("testo");
                    return null;
                }
            }
        });
    }

    public static boolean editResponse(int review_id, String text) throws SQLException {
        sql = "UPDATE risposte SET testo = ? WHERE id_recensione = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, text);
                st.setInt(2, review_id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static boolean removeResponse(int review_id) throws SQLException {
        sql = "DELETE FROM risposte WHERE id_recensione = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, review_id);
                st.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public static int getUserReviewsPages(int user_id) throws SQLException {
        sql = "SELECT COUNT(*) AS num FROM recensioni WHERE id_utente = ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        int num = rs.getInt("num");
                        return num > 0 ? (num - 1) / 10 + 1 : 0;
                    }
                    return -1;
                }
            }
        });
    }

    public static String[][] getUserReviews(int user_id, int page) throws SQLException {
        int offset = page * 10;
        sql = "SELECT nome, stelle, testo FROM \"RistorantiTheKnife\" ri JOIN recensioni re ON ri.id = id_ristorante WHERE id_utente = ? LIMIT 10 OFFSET ?";
        return withConnection(conn -> {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, user_id);
                st.setInt(2, offset);
                try (ResultSet rs = st.executeQuery()) {
                    List<String[]> list = new LinkedList<>();
                    while (rs.next()) {
                        list.add(new String[]{rs.getString("nome"), rs.getString("stelle"), rs.getString("testo")});
                    }
                    return list.toArray(new String[0][0]);
                }
            }
        });
    }

    /**
     * Chiude il pool Hikari.
     */
    public static void disconnect() {
        try {
            if (ds != null && !ds.isClosed()) {
                ds.close();
                System.out.println("Successfully disconnected from the database");
            }
        } catch (Exception e) {
            System.err.println("Errore durante la disconnessione dal DB: " + e.getMessage());
        }
    }
}
