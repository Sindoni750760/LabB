package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;

/**
 * CRUD dedicato alla gestione dei ristoranti.
 *
 * <p>
 * Questa classe rappresenta l'implementazione concreta delle operazioni
 * di accesso ai dati per l'entità <i>Ristorante</i>.
 * </p>
 *
 * <p>
 * Fornisce funzionalità per:
 * </p>
 * <ul>
 *     <li>creazione, modifica e rimozione dei ristoranti</li>
 *     <li>recupero delle informazioni dettagliate di un ristorante</li>
 *     <li>ricerca avanzata dei ristoranti con filtri multipli</li>
 * </ul>
 *
 * <p>
 * Estende {@link RestaurateurCRUD} per riutilizzare
 * la gestione delle connessioni e i controlli di accesso.
 * </p>
 *
 * <p>
 * Implementa le interfacce:
 * </p>
 * <ul>
 *     <li>{@link QueryRestaurant}</li>
 *     <li>{@link QueryRestaurantSearch}</li>
 * </ul>
 *
 * <p>Pattern applicati:</p>
 * <ul>
 *     <li><b>DAO / CRUD</b></li>
 *     <li><b>Facade</b> (tramite {@link DBHandler})</li>
 * </ul>
 */
public class RestaurantCRUD
        extends RestaurateurCRUD
        implements QueryRestaurant, QueryRestaurantSearch{
    /**
     * Costruttore 
     */
    public RestaurantCRUD(){
        
    }

    /**
     * Inserisce un nuovo ristorante nel database.
     *
     * @param ownerId id del ristoratore proprietario
     * @param name nome del ristorante
     * @param nation nazione
     * @param city città
     * @param address indirizzo
     * @param lat latitudine geografica
     * @param lon longitudine geografica
     * @param price fascia di prezzo
     * @param tipoCucina categoria / tipo di cucina
     * @param delivery indica se è disponibile il servizio delivery
     * @param online indica se è disponibile la prenotazione online
     *
     * @return {@code true} se l'inserimento è avvenuto correttamente,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
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

    /**
     * Modifica i dati di un ristorante esistente.
     *
     * @param restId id del ristorante
     * @param name nuovo nome
     * @param nation nuova nazione
     * @param city nuova città
     * @param address nuovo indirizzo
     * @param lat nuova latitudine
     * @param lon nuova longitudine
     * @param price nuova fascia di prezzo
     * @param tipoCucina nuova categoria
     * @param delivery nuovo stato delivery
     * @param online nuovo stato prenotazione online
     *
     * @return {@code true} se la modifica ha successo,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */

    @Override
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

    /**
     * Elimina un ristorante dal database.
     *
     * @param restId id del ristorante da eliminare
     * @return {@code true} se la rimozione ha successo,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
    public boolean deleteRestaurant(int restId)
            throws SQLException, InterruptedException {

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

    /**
     * Recupera le informazioni complete di un ristorante.
     *
     * <p>
     * I dati restituiti includono:
     * </p>
     * <ul>
     *     <li>informazioni anagrafiche</li>
     *     <li>coordinate geografiche</li>
     *     <li>servizi disponibili</li>
     *     <li>media stelle e numero recensioni</li>
     * </ul>
     *
     * @param restId id del ristorante
     * @return array di stringhe contenente i dati del ristorante,
     *         oppure {@code null} se non esiste
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
    public String[] getRestaurantInfo(int restId)
            throws SQLException, InterruptedException {

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

    /**
     * Recupera una lista paginata di ristoranti applicando filtri avanzati.
     *
     * <p>
     * Supporta ricerche per:
     * </p>
     * <ul>
     *     <li>località o coordinate geografiche</li>
     *     <li>raggio chilometrico</li>
     *     <li>fascia di prezzo</li>
     *     <li>servizi disponibili</li>
     *     <li>valutazione media</li>
     *     <li>categoria</li>
     *     <li>preferiti dell'utente</li>
     * </ul>
     *
     * <p>
     * Il risultato include nella prima riga:
     * </p>
     * <ul>
     *     <li>numero totale di pagine</li>
     *     <li>numero di elementi nella pagina corrente</li>
     * </ul>
     *
     * @param page numero della pagina richiesta
     * @param nation nazione (opzionale)
     * @param city città (opzionale)
     * @param lat latitudine (opzionale)
     * @param lon longitudine (opzionale)
     * @param rangeKm raggio in chilometri (opzionale)
     * @param priceMin prezzo minimo (opzionale)
     * @param priceMax prezzo massimo (opzionale)
     * @param delivery filtro servizio delivery
     * @param online filtro prenotazione online
     * @param starsMin valutazione minima (opzionale)
     * @param starsMax valutazione massima (opzionale)
     * @param favouriteUserId id utente per filtro preferiti
     * @param category categoria cucina (opzionale)
     *
     * @return matrice di stringhe contenente paginazione e risultati
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
    public String[][] getRestaurantsWithFilter(
            int page,
            String nation,
            String city,
            Double lat,
            Double lon,
            Double rangeKm,
            Integer priceMin,
            Integer priceMax,
            boolean delivery,
            boolean online,
            Double starsMin,
            Double starsMax,
            int favouriteUserId,
            String category
    ) throws SQLException, InterruptedException {

        final int PAGE_SIZE = 10;
        int offset = page * PAGE_SIZE;

        StringBuilder sql = new StringBuilder("""
            SELECT
                r.id,
                r.nome,
                COUNT(*) OVER() AS total_count
            FROM "RistorantiTheKnife" r
            LEFT JOIN recensioni rec ON rec.id_ristorante = r.id
        """);

        if (favouriteUserId > 0) {
            sql.append("""
                INNER JOIN preferiti f
                    ON f.id_ristorante = r.id AND f.id_utente = ?
            """);
        }

        sql.append(" WHERE 1=1 ");

        if (nation != null && city != null) {
            sql.append(" AND r.nazione = ? AND r.citta = ? ");
        }

        if (delivery)
            sql.append(" AND r.servizio_delivery = true ");

        if (online)
            sql.append(" AND r.prenotazione_online = true ");

        if (priceMin != null)
            sql.append(" AND r.fascia_prezzo >= ? ");

        if (priceMax != null)
            sql.append(" AND r.fascia_prezzo <= ? ");

        if (category != null)
            sql.append(" AND r.tipo_cucina ILIKE ? ");

        if (starsMin != null)
            sql.append(" AND (SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id) >= ? ");

        if (starsMax != null)
            sql.append(" AND (SELECT AVG(stelle) FROM recensioni WHERE id_ristorante = r.id) <= ? ");

        if (lat != null && lon != null && rangeKm != null) {
            sql.append("""
                AND (
                    6371 * acos(
                        cos(radians(?)) *
                        cos(radians(r.latitudine)) *
                        cos(radians(r.longitudine) - radians(?)) +
                        sin(radians(?)) *
                        sin(radians(r.latitudine))
                    )
                ) <= ?
            """);
        }

        sql.append("""
            GROUP BY r.id, r.nome
            ORDER BY r.nome
            LIMIT ? OFFSET ?
        """);

        try (Connection conn = connMgr.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            if (favouriteUserId > 0)
                ps.setInt(idx++, favouriteUserId);

            if (nation != null && city != null) {
                ps.setString(idx++, nation);
                ps.setString(idx++, city);
            }

            if (priceMin != null)
                ps.setInt(idx++, priceMin);

            if (priceMax != null)
                ps.setInt(idx++, priceMax);

            if (category != null)
                ps.setString(idx++, "%" + category + "%");

            if (starsMin != null)
                ps.setDouble(idx++, starsMin);

            if (starsMax != null)
                ps.setDouble(idx++, starsMax);

            if (lat != null && lon != null && rangeKm != null) {
                ps.setDouble(idx++, lat);
                ps.setDouble(idx++, lon);
                ps.setDouble(idx++, lat);
                ps.setDouble(idx++, rangeKm);
            }

            ps.setInt(idx++, PAGE_SIZE);
            ps.setInt(idx, offset);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return new String[][] { { "0", "0" } };
                }

                int total = rs.getInt("total_count");
                int pages = (int) Math.ceil((double) total / PAGE_SIZE);

                ArrayList<String[]> rows = new ArrayList<>();
                rows.add(new String[] {
                        Integer.toString(pages),
                        Integer.toString(Math.min(PAGE_SIZE, total - offset))
                });

                do {
                    rows.add(new String[] {
                            Integer.toString(rs.getInt("id")),
                            rs.getString("nome")
                    });
                } while (rs.next());

                return rows.toArray(new String[0][]);
            }
        }
    }
}