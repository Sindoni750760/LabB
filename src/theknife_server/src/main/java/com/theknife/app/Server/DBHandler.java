package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.theknife.app.ConnectionManager;

/**
 * Handler singleton per l'accesso al database.
 * Gestisce tutte le operazioni CRUD per utenti, ristoranti, recensioni e preferiti.
 * Utilizza prepared statements per prevenire SQL injection e ConnectionManager per la gestione delle connessioni.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class DBHandler{

    /** Istanza singleton dell'handler database. */
    private static DBHandler instance = null;
    
    /** Manager per la gestione delle connessioni al database. */
    private final ConnectionManager connMgr = ConnectionManager.getInstance();

    /**
     * Costruttore privato per il pattern singleton.
     */
    private DBHandler() {}

    /**
     * Restituisce l'istanza singleton dell'handler database.
     *
     * @return istanza singleton di DBHandler
     */
    public static synchronized DBHandler getInstance() {
        if (instance == null) instance = new DBHandler();
        return instance;
    }

    /**
     * Inserisce un nuovo utente nel database.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco
     * @param hashPassword hash bcrypt della password
     * @param birth timestamp della data di nascita (-1 se non specificata)
     * @param lat latitudine del domicilio
     * @param lon longitudine del domicilio
     * @param isRist true se l'utente è un ristoratore
     * @return true se l'inserimento ha successo, false altrimenti
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    public boolean addUser(String nome, String cognome, String username, String hashPassword,
                           long birth, double lat, double lon, boolean isRist)
            throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO utenti
                (nome, cognome, username, password, data_nascita,
                 latitudine_domicilio, longitudine_domicilio, is_ristoratore)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username);
            ps.setString(4, hashPassword);

            if (birth <= 0) {
                ps.setDate(5, null);
            } else {
                ps.setDate(5, new java.sql.Date(birth));
            }

            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setBoolean(8, isRist);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Recupera le credenziali di login per un utente.
     *
     * @param username username dell'utente
     * @return array contenente [id, hashPassword], oppure null se l'utente non esiste
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    public String[] getUserLoginInfo(String username) throws SQLException, InterruptedException {

        String sql = """
            SELECT id, password
            FROM utenti
            WHERE username = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new String[]{
                        Integer.toString(rs.getInt("id")),
                        rs.getString("password")
                };
            }
        }
    }

    /**
     * Ritorna [nome, cognome, "y"/"n" is_ristoratore]
     */
    public String[] getUserInfo(int userId) throws SQLException, InterruptedException {

        String sql = """
            SELECT nome, cognome, is_ristoratore
            FROM utenti
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) return null;

                return new String[]{
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getBoolean("is_ristoratore") ? "y" : "n"
                };
            }
        }
    }

    public double[] getUserPosition(int userId) throws SQLException, InterruptedException {

        String sql = """
            SELECT latitudine_domicilio, longitudine_domicilio
            FROM utenti
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new double[]{ rs.getDouble(1), rs.getDouble(2) };
            }
        }
    }


    public boolean addRestaurant(int ownerId, String name, String nation, String city,
                                 String address, double lat, double lon,
                                 int price, String tipoCucina,
                                 boolean delivery, boolean online)
            throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO "RistorantiTheKnife"
                (proprietario, nome, nazione, citta, indirizzo,
                 latitudine, longitudine, fascia_prezzo,
                 tipo_cucina, servizio_delivery, prenotazione_online)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);
            ps.setString(2, name);
            ps.setString(3, nation);
            ps.setString(4, city);
            ps.setString(5, address);
            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setInt(8, price);
            ps.setString(9, tipoCucina);
            ps.setBoolean(10, delivery);
            ps.setBoolean(11, online);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean editRestaurant(int restId, String name, String nation, String city,
                                  String address, double lat, double lon,
                                  int price, String tipoCucina,
                                  boolean delivery, boolean online)
            throws SQLException, InterruptedException {

        String sql = """
            UPDATE "RistorantiTheKnife"
            SET nome = ?, nazione = ?, citta = ?, indirizzo = ?,
                latitudine = ?, longitudine = ?, fascia_prezzo = ?,
                tipo_cucina = ?, servizio_delivery = ?, prenotazione_online = ?
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, nation);
            ps.setString(3, city);
            ps.setString(4, address);
            ps.setDouble(5, lat);
            ps.setDouble(6, lon);
            ps.setInt(7, price);
            ps.setString(8, tipoCucina);
            ps.setBoolean(9, delivery);
            ps.setBoolean(10, online);
            ps.setInt(11, restId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteRestaurant(int restId) throws SQLException, InterruptedException {

        String sql = """
            DELETE FROM "RistorantiTheKnife"
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean hasAccess(int userId, int restId) throws SQLException, InterruptedException {

        String sql = """
            SELECT 1
            FROM "RistorantiTheKnife"
            WHERE id = ? AND proprietario = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String[] getRestaurantInfo(int restId) throws SQLException, InterruptedException {

        String sql = """
            SELECT r.nome, r.nazione, r.citta, r.indirizzo,
                   r.latitudine, r.longitudine,
                   r.fascia_prezzo, r.tipo_cucina,
                   r.servizio_delivery, r.prenotazione_online,
                   COALESCE((SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id), 0),
                   COALESCE((SELECT COUNT(*) FROM recensioni WHERE id_ristorante = r.id), 0)
            FROM "RistorantiTheKnife" r
            WHERE r.id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new String[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        Double.toString(rs.getDouble(5)),
                        Double.toString(rs.getDouble(6)),
                        Integer.toString(rs.getInt(7)),
                        rs.getString(8),
                        rs.getBoolean(9) ? "y" : "n",
                        rs.getBoolean(10) ? "y" : "n",
                        Double.toString(rs.getDouble(11)),
                        Integer.toString(rs.getInt(12))
                };
            }
        }
    }


    public int getUserRestaurantsPages(int userId) throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM "RistorantiTheKnife"
            WHERE proprietario = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                int total = rs.getInt(1);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    public String[][] getUserRestaurants(int userId, int page) throws SQLException, InterruptedException {

            String sql = """
                SELECT id, nome
                FROM "RistorantiTheKnife"
                WHERE proprietario = ?
                ORDER BY nome
                LIMIT 17 OFFSET ?
            """;

            try (Connection conn = connMgr.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, page * 10);

                List<String[]> out = new ArrayList<>();

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new String[]{
                                Integer.toString(rs.getInt("id")),
                                rs.getString("nome")
                        });
                    }
                }

                return out.toArray(new String[0][]);
            }
        }

    /**
     * Restituisce una pagina di ristoranti filtrati.
     */
    public String[][] getRestaurantsWithFilter(
            int page,
            String nation,
            String city,
            Double lat, Double lon, Double rangeKm,
            Integer priceMin, Integer priceMax,
            boolean delivery, boolean online,
            Double starsMin, Double starsMax,
            int favouriteUserId,
            String category
    ) throws SQLException, InterruptedException {

        StringBuilder sql = new StringBuilder("""
            SELECT r.id, r.nome
            FROM "RistorantiTheKnife" r
        """);

        List<Object> params = new ArrayList<>();
        List<Integer> types = new ArrayList<>(); // 1=int, 2=double, 3=string

        if (favouriteUserId > 0) {
            sql.append("""
                JOIN preferiti p ON p.id_ristorante = r.id
                WHERE p.id_utente = ?
            """);
            params.add(favouriteUserId);
            types.add(1);
        } else {
            sql.append(" WHERE 1 = 1");
        }

        // Nazione / Città
        if (nation != null && !nation.isBlank()) {
            sql.append(" AND LOWER(r.nazione) = LOWER(?)");
            params.add(nation);
            types.add(3);
        }
        if (city != null && !city.isBlank()) {
            sql.append(" AND LOWER(r.citta) = LOWER(?)");
            params.add(city);
            types.add(3);
        }

        // Distanza
        if (lat != null && lon != null && rangeKm != null && rangeKm > 0) {
            double rangeDegree = rangeKm / 111.0;
            sql.append("""
                AND SQRT(
                    (r.latitudine - ?) * (r.latitudine - ?) +
                    (r.longitudine - ?) * (r.longitudine - ?)
                ) <= ?
            """);
            params.add(lat);  types.add(2);
            params.add(lat);  types.add(2);
            params.add(lon);  types.add(2);
            params.add(lon);  types.add(2);
            params.add(rangeDegree); types.add(2);
        }

        // Prezzo
        if (priceMin != null) {
            sql.append(" AND r.fascia_prezzo >= ?");
            params.add(priceMin);
            types.add(1);
        }
        if (priceMax != null) {
            sql.append(" AND r.fascia_prezzo <= ?");
            params.add(priceMax);
            types.add(1);
        }

        // Flag booleani
        if (delivery) sql.append(" AND r.servizio_delivery = TRUE");
        if (online)   sql.append(" AND r.prenotazione_online = TRUE");

        // Stelle
        String starsSubquery = "(SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id)";
        if (starsMin != null && starsMin > 0) {
            sql.append(" AND ").append(starsSubquery).append(" >= ?");
            params.add(starsMin);
            types.add(2);
        }
        if (starsMax != null && starsMax > 0) {
            sql.append(" AND ").append(starsSubquery).append(" <= ?");
            params.add(starsMax);
            types.add(2);
        }

        // Categoria
        if (category != null) {
            sql.append(" AND LOWER(r.tipo_cucina) LIKE LOWER(?)");
            params.add("%" + category + "%");
            types.add(3);
        }

        // PAGINAZIONE: 17 per pagina
        sql.append(" ORDER BY r.nome LIMIT 17 OFFSET ?");
        params.add(page * 17);
        types.add(1);

        try (Connection conn = connMgr.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (int i = 0; i < params.size(); i++) {
                switch (types.get(i)) {
                    case 1 -> ps.setInt(idx++, (int) params.get(i));
                    case 2 -> ps.setDouble(idx++, (double) params.get(i));
                    case 3 -> ps.setString(idx++, (String) params.get(i));
                }
            }

            List<String[]> results = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new String[]{
                            Integer.toString(rs.getInt("id")),
                            rs.getString("nome")
                    });
                }
            }

            int total = countRestaurantsWithFilter(
                    nation, city,
                    lat, lon, rangeKm,
                    priceMin, priceMax,
                    delivery, online,
                    starsMin, starsMax,
                    favouriteUserId,
                    category
            );

            int pages = (int) Math.ceil(total / 17.0);

            String[][] out = new String[results.size() + 1][2];
            out[0][0] = Integer.toString(pages);
            out[0][1] = Integer.toString(results.size());

            for (int i = 0; i < results.size(); i++) {
                out[i + 1] = results.get(i);
            }

            return out;
        }
    }

    private int countRestaurantsWithFilter(
            String nation,
            String city,
            Double lat, Double lon, Double rangeKm,
            Integer priceMin, Integer priceMax,
            boolean delivery, boolean online,
            Double starsMin, Double starsMax,
            int favouriteUserId,
            String category
    ) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) AS total
            FROM "RistorantiTheKnife" r
        """);

        List<Object> params = new ArrayList<>();
        List<Integer> types = new ArrayList<>();

        if (favouriteUserId > 0) {
            sql.append("""
                JOIN preferiti p ON p.id_ristorante = r.id
                WHERE p.id_utente = ?
            """);
            params.add(favouriteUserId);
            types.add(1);
        } else {
            sql.append(" WHERE 1 = 1");
        }

        if (nation != null && !nation.isBlank()) {
            sql.append(" AND LOWER(r.nazione) = LOWER(?)");
            params.add(nation);
            types.add(3);
        }
        if (city != null && !city.isBlank()) {
            sql.append(" AND LOWER(r.citta) = LOWER(?)");
            params.add(city);
            types.add(3);
        }

        if (lat != null && lon != null && rangeKm != null && rangeKm > 0) {
            double rangeDegree = rangeKm / 111.0;
            sql.append("""
                AND SQRT(
                    (r.latitudine - ?) * (r.latitudine - ?) +
                    (r.longitudine - ?) * (r.longitudine - ?)
                ) <= ?
            """);
            params.add(lat);  types.add(2);
            params.add(lat);  types.add(2);
            params.add(lon);  types.add(2);
            params.add(lon);  types.add(2);
            params.add(rangeDegree); types.add(2);
        }

        if (priceMin != null) {
            sql.append(" AND r.fascia_prezzo >= ?");
            params.add(priceMin);
            types.add(1);
        }
        if (priceMax != null) {
            sql.append(" AND r.fascia_prezzo <= ?");
            params.add(priceMax);
            types.add(1);
        }

        if (delivery) sql.append(" AND r.servizio_delivery = TRUE");
        if (online)   sql.append(" AND r.prenotazione_online = TRUE");

        String starsSub = "(SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id)";
        if (starsMin != null && starsMin > 0) {
            sql.append(" AND ").append(starsSub).append(" >= ?");
            params.add(starsMin);
            types.add(2);
        }
        if (starsMax != null && starsMax > 0) {
            sql.append(" AND ").append(starsSub).append(" <= ?");
            params.add(starsMax);
            types.add(2);
        }

        if (category != null) {
            sql.append(" AND LOWER(r.tipo_cucina) LIKE LOWER(?)");
            params.add("%" + category + "%");
            types.add(3);
        }

        try (Connection conn = connMgr.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (int i = 0; i < params.size(); i++) {
                switch (types.get(i)) {
                    case 1 -> ps.setInt(idx++, (int) params.get(i));
                    case 2 -> ps.setDouble(idx++, (double) params.get(i));
                    case 3 -> ps.setString(idx++, (String) params.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }


    public int getReviewsPageCount(int restId) throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM recensioni
            WHERE id_ristorante = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                int total = rs.getInt(1);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    public String[][] getReviews(int restId, int page) throws SQLException, InterruptedException {

        String sql = """
            SELECT r.id, r.stelle, r.testo,
                   COALESCE(
                       (SELECT testo
                        FROM risposte
                        WHERE id_recensione = r.id
                        LIMIT 1),
                       NULL
                   ) AS risposta
            FROM recensioni r
            WHERE r.id_ristorante = ?
            ORDER BY r.id DESC
            LIMIT 17 OFFSET ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);
            ps.setInt(2, page * 10);

            List<String[]> out = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String reply = rs.getString("risposta");
                    out.add(new String[]{
                            Integer.toString(rs.getInt("id")),
                            Integer.toString(rs.getInt("stelle")),
                            rs.getString("testo"),
                            reply
                    });
                }
            }

            return out.toArray(new String[0][]);
        }
    }

    public String[] getMyReview(int userId, int restId) throws SQLException, InterruptedException {

        String sql = """
            SELECT stelle, testo
            FROM recensioni
            WHERE id_utente = ? AND id_ristorante = ?
            LIMIT 1
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new String[]{
                        Integer.toString(rs.getInt("stelle")),
                        rs.getString("testo")
                };
            }
        }
    }

    public boolean addReview(int userId, int restId, int stars, String text)
            throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO recensioni (id_utente, id_ristorante, stelle, testo)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restId);
            ps.setInt(3, stars);
            ps.setString(4, text);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean editReview(int userId, int restId, int stars, String text)
            throws SQLException, InterruptedException {

        String sql = """
            UPDATE recensioni
            SET stelle = ?, testo = ?
            WHERE id_utente = ? AND id_ristorante = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stars);
            ps.setString(2, text);
            ps.setInt(3, userId);
            ps.setInt(4, restId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean removeReview(int userId, int restId) throws SQLException, InterruptedException {

        String sql = """
            DELETE FROM recensioni
            WHERE id_utente = ? AND id_ristorante = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restId);

            return ps.executeUpdate() == 1;
        }
    }


    public boolean canRespond(int userId, int reviewId) throws SQLException, InterruptedException {

        String sql = """
            SELECT 1
            FROM recensioni r
            JOIN "RistorantiTheKnife" t ON r.id_ristorante = t.id
            WHERE r.id = ? AND t.proprietario = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String getResponse(int reviewId) throws SQLException, InterruptedException {

        String sql = """
            SELECT testo
            FROM risposte
            WHERE id_recensione = ?
            LIMIT 1
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public boolean addResponse(int reviewId, String text) throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO risposte (id_recensione, testo)
            VALUES (?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            ps.setString(2, text);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean editResponse(int reviewId, String text) throws SQLException, InterruptedException {

        String sql = """
            UPDATE risposte
            SET testo = ?
            WHERE id_recensione = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, text);
            ps.setInt(2, reviewId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean removeResponse(int reviewId) throws SQLException, InterruptedException {

        String sql = """
            DELETE FROM risposte
            WHERE id_recensione = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);

            return ps.executeUpdate() == 1;
        }
    }


    public boolean isFavourite(int userId, int restId) throws SQLException, InterruptedException {

        String sql = """
            SELECT 1
            FROM preferiti
            WHERE id_utente = ? AND id_ristorante = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean addFavourite(int userId, int restId) throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO preferiti (id_utente, id_ristorante)
            VALUES (?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean removeFavourite(int userId, int restId) throws SQLException, InterruptedException {

        String sql = """
            DELETE FROM preferiti
            WHERE id_utente = ? AND id_ristorante = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restId);

            return ps.executeUpdate() == 1;
        }
    }

    public int getUserReviewsPages(int userId) throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM recensioni
            WHERE id_utente = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                int total = rs.getInt(1);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    public String[][] getUserReviews(int userId, int page) throws SQLException, InterruptedException {

        String sql = """
            SELECT r2.nome, r.stelle, r.testo
            FROM recensioni r
            JOIN "RistorantiTheKnife" r2 ON r.id_ristorante = r2.id
            WHERE r.id_utente = ?
            ORDER BY r.id DESC
            LIMIT 17 OFFSET ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, page * 10);

            List<String[]> out = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            rs.getString(1),
                            Integer.toString(rs.getInt(2)),
                            rs.getString(3)
                    });
                }
            }

            return out.toArray(new String[0][]);
        }
    }
}