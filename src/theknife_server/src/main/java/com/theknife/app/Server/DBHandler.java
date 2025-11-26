package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.theknife.app.ConnectionManager;


public class DBHandler implements QueryRestaurant, QueryUser {


    private static DBHandler instance = null;
    private final ConnectionManager connMgr = ConnectionManager.getInstance();

    private DBHandler() {}

    public static synchronized DBHandler getInstance() {
        if (instance == null) instance = new DBHandler();
        return instance;
    }

    // ============================================================
    //                        UTENTI
    // ============================================================

    public boolean addUser(
            String nome,
            String cognome,
            String username,
            String hashPassword,
            long birth,
            double lat,
            double lon,
            boolean isRist) throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO utenti (nome, cognome, username, password, data_nascita,
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
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, new java.sql.Date(birth));
            }

            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setBoolean(8, isRist);

            return ps.executeUpdate() == 1;
        }
    }

    public String[] getUserLoginInfo(String username)
            throws SQLException, InterruptedException {

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

    public String[] getUserInfo(int userId)
            throws SQLException, InterruptedException {

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

    // ============================================================
    //                RISTORANTI — CRUD COMPLETO
    // ============================================================

    public boolean addRestaurant(
            int ownerId,
            String nome,
            String nazione,
            String citta,
            String indirizzo,
            double lat,
            double lon,
            int fasciaPrezzo,
            String tipoCucina,
            boolean delivery,
            boolean online
    ) throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO "RistorantiTheKnife"
                (proprietario, nome, nazione, citta, indirizzo, latitudine,
                 longitudine, fascia_prezzo, servizio_delivery,
                 prenotazione_online, tipo_cucina)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);
            ps.setString(2, nome);
            ps.setString(3, nazione);
            ps.setString(4, citta);
            ps.setString(5, indirizzo);
            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setInt(8, fasciaPrezzo);
            ps.setBoolean(9, delivery);
            ps.setBoolean(10, online);
            ps.setString(11, tipoCucina);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean editRestaurant(
            int id,
            String nome,
            String nazione,
            String citta,
            String indirizzo,
            double lat,
            double lon,
            int fasciaPrezzo,
            String tipoCucina,
            boolean delivery,
            boolean online
    ) throws SQLException, InterruptedException {

        String sql = """
            UPDATE "RistorantiTheKnife"
            SET nome = ?, nazione = ?, citta = ?, indirizzo = ?,
                latitudine = ?, longitudine = ?, fascia_prezzo = ?,
                tipo_cucina = ?, servizio_delivery = ?, prenotazione_online = ?
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, nazione);
            ps.setString(3, citta);
            ps.setString(4, indirizzo);
            ps.setDouble(5, lat);
            ps.setDouble(6, lon);
            ps.setInt(7, fasciaPrezzo);
            ps.setString(8, tipoCucina);
            ps.setBoolean(9, delivery);
            ps.setBoolean(10, online);
            ps.setInt(11, id);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteRestaurant(int id)
            throws SQLException, InterruptedException {

        String sql = """
            DELETE FROM "RistorantiTheKnife"
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean hasAccess(int userId, int restId)
            throws SQLException, InterruptedException {

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

    public String[] getRestaurantInfo(int id)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT
             nome, nazione, citta, indirizzo,
             latitudine, longitudine,
             fascia_prezzo, tipo_cucina,
             servizio_delivery, prenotazione_online,
             COALESCE((SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id), 0),
             COALESCE((SELECT COUNT(*) FROM recensioni WHERE id_ristorante = r.id), 0)
            FROM "RistorantiTheKnife" r
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

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

    @Override
    public List<String[]> listRestaurants(int page, String lat, String lon, String rangeKm, String priceMin,
            String priceMax, String hasDelivery, String hasOnline, String starsMin, String starsMax, String category,
            String nearMe, Integer userId, boolean onlyFavourites) throws SQLException {
        
        try {
            // Converti i parametri String ai tipi appropriati
            Double latitude = (lat == null || lat.equals("-")) ? null : Double.parseDouble(lat);
            Double longitude = (lon == null || lon.equals("-")) ? null : Double.parseDouble(lon);
            Double range = (rangeKm == null || rangeKm.equals("-")) ? null : Double.parseDouble(rangeKm);
            Integer priceMinInt = (priceMin == null || priceMin.equals("-")) ? null : Integer.parseInt(priceMin);
            Integer priceMaxInt = (priceMax == null || priceMax.equals("-")) ? null : Integer.parseInt(priceMax);
            Double starsMinDouble = (starsMin == null || starsMin.equals("-")) ? null : Double.parseDouble(starsMin);
            Double starsMaxDouble = (starsMax == null || starsMax.equals("-")) ? null : Double.parseDouble(starsMax);
            
            boolean delivery = "y".equals(hasDelivery);
            boolean online = "y".equals(hasOnline);
            
            int favUserId = (userId != null && onlyFavourites) ? userId : -1;
            
            String[][] result = getRestaurantsWithFilter(
                    page,
                    latitude, longitude, range,
                    priceMinInt, priceMaxInt,
                    delivery, online,
                    starsMinDouble, starsMaxDouble,
                    favUserId,
                    category
            );
            
            // Converti il result da String[][] a List<String[]>
            List<String[]> list = new ArrayList<>();
            for (String[] row : result) {
                list.add(row);
            }
            return list;
        } catch (NumberFormatException e) {
            // Se c'è un errore nel parsing, restituisci una lista vuota con i numeri di pagina
            List<String[]> list = new ArrayList<>();
            list.add(new String[]{"0", "0"});
            return list;
        } catch (InterruptedException e) {
            throw new SQLException("Interrupted during query", e);
        }
    }

    // ============================================================
    //             LISTE E FILTRI (CON NUOVO SCHEMA)

    public String[][] getRestaurantsWithFilter(
            int page,
            Double lat, Double lon, Double rangeKm,
            Integer priceMin, Integer priceMax,
            boolean delivery, boolean online,
            Double starsMin, Double starsMax,
            int favouriteUserId,
            String tipoCucina
    ) throws SQLException, InterruptedException {

        StringBuilder sql = new StringBuilder("""
            SELECT id, nome
            FROM "RistorantiTheKnife"
            WHERE 1=1
        """);

        if (lat != null && lon != null && rangeKm != null) {
            sql.append("""
                AND earth_distance(
                        ll_to_earth(latitudine, longitudine),
                        ll_to_earth(?, ?)
                    ) <= ?
            """);
        }

        if (priceMin != null) sql.append(" AND fascia_prezzo >= ?");
        if (priceMax != null) sql.append(" AND fascia_prezzo <= ?");

        if (delivery) sql.append(" AND servizio_delivery = TRUE");
        if (online) sql.append(" AND prenotazione_online = TRUE");

        if (starsMin != null) {
            sql.append("""
                AND (SELECT AVG(stelle) FROM recensioni
                     WHERE id_ristorante = "RistorantiTheKnife".id) >= ?
            """);
        }

        if (starsMax != null) {
            sql.append("""
                AND (SELECT AVG(stelle) FROM recensioni
                     WHERE id_ristorante = "RistorantiTheKnife".id) <= ?
            """);
        }

        if (favouriteUserId > 0) {
            sql.append("""
                AND id IN (
                    SELECT id_ristorante FROM preferiti
                    WHERE id_utente = ?
                )
            """);
        }

        if (tipoCucina != null) sql.append(" AND tipo_cucina LIKE ?");

        sql.append(" ORDER BY nome LIMIT 10 OFFSET ?");

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            if (lat != null && lon != null && rangeKm != null) {
                ps.setDouble(idx++, lat);
                ps.setDouble(idx++, lon);
                ps.setDouble(idx++, rangeKm * 1000);
            }

            if (priceMin != null) ps.setInt(idx++, priceMin);
            if (priceMax != null) ps.setInt(idx++, priceMax);

            if (starsMin != null) ps.setDouble(idx++, starsMin);
            if (starsMax != null) ps.setDouble(idx++, starsMax);

            if (favouriteUserId > 0) ps.setInt(idx++, favouriteUserId);

            if (tipoCucina != null) ps.setString(idx++, "%" + tipoCucina + "%");

            ps.setInt(idx++, page * 10);

            List<String[]> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            Integer.toString(rs.getInt(1)),
                            rs.getString(2)
                    });
                }
            }

            int total = countRestaurantsWithFilter(
                    lat, lon, rangeKm,
                    priceMin, priceMax,
                    delivery, online,
                    starsMin, starsMax,
                    favouriteUserId,
                    tipoCucina
            );

            int pages = (int) Math.ceil(total / 10.0);

            String[][] result = new String[out.size() + 1][2];
            result[0][0] = Integer.toString(pages);
            result[0][1] = Integer.toString(out.size());

            for (int i = 0; i < out.size(); i++)
                result[i + 1] = out.get(i);

            return result;
        }
    }

    private int countRestaurantsWithFilter(
            Double lat, Double lon, Double rangeKm,
            Integer priceMin, Integer priceMax,
            boolean delivery, boolean online,
            Double starsMin, Double starsMax,
            int favouriteUserId,
            String tipoCucina
    ) throws SQLException, InterruptedException {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM "RistorantiTheKnife"
            WHERE 1=1
        """);

        if (lat != null && lon != null && rangeKm != null) {
            sql.append("""
                AND earth_distance(
                        ll_to_earth(latitudine, longitudine),
                        ll_to_earth(?, ?)
                    ) <= ?
            """);
        }

        if (priceMin != null) sql.append(" AND fascia_prezzo >= ?");
        if (priceMax != null) sql.append(" AND fascia_prezzo <= ?");

        if (delivery) sql.append(" AND servizio_delivery = TRUE");
        if (online) sql.append(" AND prenotazione_online = TRUE");

        if (starsMin != null) {
            sql.append("""
                AND (SELECT AVG(stelle)
                     FROM recensioni
                     WHERE id_ristorante = "RistorantiTheKnife".id) >= ?
            """);
        }

        if (starsMax != null) {
            sql.append("""
                AND (SELECT AVG(stelle)
                     FROM recensioni
                     WHERE id_ristorante = "RistorantiTheKnife".id) <= ?
            """);
        }

        if (favouriteUserId > 0) {
            sql.append("""
                AND id IN (
                    SELECT id_ristorante
                    FROM preferiti
                    WHERE id_utente = ?
                )
            """);
        }

        if (tipoCucina != null) sql.append(" AND tipo_cucina LIKE ?");

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            if (lat != null && lon != null && rangeKm != null) {
                ps.setDouble(idx++, lat);
                ps.setDouble(idx++, lon);
                ps.setDouble(idx++, rangeKm * 1000);
            }

            if (priceMin != null) ps.setInt(idx++, priceMin);
            if (priceMax != null) ps.setInt(idx++, priceMax);

            if (starsMin != null) ps.setDouble(idx++, starsMin);
            if (starsMax != null) ps.setDouble(idx++, starsMax);

            if (favouriteUserId > 0) ps.setInt(idx++, favouriteUserId);

            if (tipoCucina != null) ps.setString(idx++, "%" + tipoCucina + "%");

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ============================================================
    //                    RECENSIONI
    // ============================================================

    public int getReviewsPageCount(int restId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM recensioni
            WHERE id_ristorante = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int total = rs.getInt(1);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    public List<String[]> getReviews(int restId, int page)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT r.id, r.stelle, r.testo,
                   (SELECT testo FROM risposte WHERE id_recensione = r.id LIMIT 1)
            FROM recensioni r
            WHERE r.id_ristorante = ?
            ORDER BY r.id DESC
            LIMIT 10 OFFSET ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restId);
            ps.setInt(2, page * 10);

            List<String[]> out = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            Integer.toString(rs.getInt(1)),
                            Integer.toString(rs.getInt(2)),
                            rs.getString(3),
                            rs.getString(4) == null ? "-" : rs.getString(4)
                    });
                }
            }

            return out;
        }
    }

    public String[] getMyReview(int userId, int restId)
            throws SQLException, InterruptedException {

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
                        Integer.toString(rs.getInt(1)),
                        rs.getString(2)
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

    public boolean removeReview(int userId, int restId)
            throws SQLException, InterruptedException {

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

    // ============================================================
    //                      RISPOSTE
    // ============================================================

    public String getResponse(int reviewId)
            throws SQLException, InterruptedException {

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
                if (!rs.next()) return null;
                return rs.getString(1);
            }
        }
    }

    public boolean addResponse(int reviewId, String text)
            throws SQLException, InterruptedException {

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

    public boolean editResponse(int reviewId, String text)
            throws SQLException, InterruptedException {

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

    public boolean removeResponse(int reviewId)
            throws SQLException, InterruptedException {

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

    // ============================================================
    //                      PREFERITI
    // ============================================================

    public boolean isFavourite(int userId, int restId)
            throws SQLException, InterruptedException {

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

    public boolean addFavourite(int userId, int restId)
            throws SQLException, InterruptedException {

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

    public boolean removeFavourite(int userId, int restId)
            throws SQLException, InterruptedException {

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

    // ============================================================
    //                  LE MIE RECENSIONI
    // ============================================================

    public int getUserReviewsPages(int userId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM recensioni
            WHERE id_utente = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int total = rs.getInt(1);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    public String[][] getUserReviews(int userId, int page)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT t.nome, r.stelle, r.testo
            FROM recensioni r
            JOIN "RistorantiTheKnife" t ON r.id_ristorante = t.id
            WHERE r.id_utente = ?
            ORDER BY r.id DESC
            LIMIT 10 OFFSET ?
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

    // ============================================================
    //                  METODI PER I RISTORANTI DELL'UTENTE
    // ============================================================

    public int getMyRestaurantsPageCount(int userId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM "RistorantiTheKnife"
            WHERE id_ristoratore = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int total = rs.getInt(1);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    public List<String[]> getMyRestaurants(int userId, int page)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT id, nome
            FROM "RistorantiTheKnife"
            WHERE id_ristoratore = ?
            ORDER BY nome
            LIMIT 10 OFFSET ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, page * 10);

            List<String[]> out = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            Integer.toString(rs.getInt(1)),
                            rs.getString(2)
                    });
                }
            }

            return out;
        }
    }

    // Stub per metodi non implementati
    @Override
    public boolean addUser(String nome, String cognome, String username, String passwordHashed, Date dataNascita,
            double lat, double lon, boolean isRistoratore) throws SQLException, InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'addUser'");
    }

    @Override
    public boolean userExists(String username) throws SQLException, InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'userExists'");
    }

    @Override
    public String[] getUserInfoById(int id) throws SQLException, InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'getUserInfoById'");
    }

    @Override
    public boolean addRestaurant(int ownerId, String nome, String nazione, String citta, String indirizzo, double lat,
            double lon, int fasciaPrezzo, boolean delivery, boolean online, String tipoCucina) throws SQLException {
        throw new UnsupportedOperationException("Unimplemented method 'addRestaurant'");
    }

    @Override
    public boolean editRestaurant(int id, String nome, String nazione, String citta, String indirizzo, double lat,
            double lon, int fasciaPrezzo, boolean delivery, boolean online, String tipoCucina) throws SQLException {
        throw new UnsupportedOperationException("Unimplemented method 'editRestaurant'");
    }

    @Override
    public int getMyReviewsPageCount(int userId) throws SQLException, InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'getMyReviewsPageCount'");
    }

    @Override
    public List<String[]> getMyReviews(int userId, int page) throws SQLException, InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'getMyReviews'");
    }
}
