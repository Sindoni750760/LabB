package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer CRUD intermedio dedicato alla gestione dei ristoranti.
 * <p>
 * Implementa tutte le operazioni principali relative a:
 * <ul>
 *     <li>Creazione/modifica/eliminazione ristorante</li>
 *     <li>Recupero informazioni estese</li>
 *     <li>Ricerca con multipli filtri</li>
 *     <li>Gestione recensioni e risposte</li>
 *     <li>Preferiti</li>
 * </ul>
 *
 * Estende {@link RestaurateurCRUD} ereditando funzioni per l'accesso del proprietario.
 */
public abstract class RestaurantCRUD extends RestaurateurCRUD {

    /**
     * Inserisce un nuovo ristorante associato a un ristoratore.
     *
     * @param ownerId ID del proprietario
     * @param name nome ristorante
     * @param nation nazione
     * @param city città
     * @param address indirizzo
     * @param lat latitudine
     * @param lon longitudine
     * @param price fascia prezzo (0–3)
     * @param tipoCucina descrizione cucina
     * @param delivery true se disponibile consegna
     * @param online true se disponibile prenotazione online
     * @return true se inserito correttamente
     * @throws SQLException se fallisce l'inserimento
     * @throws InterruptedException se il thread viene interrotto
     */
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
     * Modifica le informazioni di un ristorante esistente.
     *
     * @param restId ID ristorante
     * @param name nuovo nome
     * @param nation nuova nazione
     * @param city nuova città
     * @param address nuovo indirizzo
     * @param lat nuova latitudine
     * @param lon nuova longitudine
     * @param price nuova fascia di prezzo
     * @param tipoCucina nuovo tipo di cucina
     * @param delivery nuovo stato delivery
     * @param online nuovo stato prenotazione
     * @return true se aggiornato, false se non esiste/errore
     * @throws SQLException errore DB
     * @throws InterruptedException operazione interrotta
     */
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
     * Elimina un ristorante.
     * <p>
     * NOTA: eventuali record correlati sono vincolati tramite FK.
     *
     * @param restId ID ristorante
     * @return true se eliminato
     * @throws SQLException errore DB
     * @throws InterruptedException operazione interrotta
    */
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
     * Restituisce informazioni complete del ristorante, comprendenti:
     * <pre>
     * [0] nome
     * [1] nazione
     * [2] città
     * [3] indirizzo
     * [4] latitudine
     * [5] longitudine
     * [6] fascia prezzo
     * [7] tipo cucina
     * [8] delivery: y/n
     * [9] prenotazione online: y/n
     * [10] media recensioni (double)
     * [11] numero recensioni
     * </pre>
     *
     * @param restId ID ristorante
     * @return array di 12 celle oppure null se inesistente
     * @throws SQLException errore DB
     * @throws InterruptedException operazione interrotta
     */
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
     * Restituisce un insieme paginato di ristoranti filtrati per:
     * <ul>
     *   <li>località (nazione/città)</li>
     *   <li>coordinate + raggio</li>
     *   <li>fascia prezzo</li>
     *   <li>delivery / prenotazione</li>
     *   <li>categorie parziali</li>
     *   <li>preferiti</li>
     *   <li>range stelle min/max</li>
     * </ul>
     *
     * La risposta contiene:
     * <pre>
     * result[0][0] = numero pagine
     * result[0][1] = numero risultati pagina
     * result[i][0] = id ristorante
     * result[i][1] = nome
     * </pre>
     *
     * @param page indice pagina (0-based)
     * @param nation filtro nazione (null per ignorare)
     * @param city filtro città (null per ignorare)
     * @param lat latitudine (opzionale)
     * @param lon longitudine (opzionale)
     * @param rangeKm raggio in km
     * @param priceMin prezzo minimo
     * @param priceMax prezzo massimo
     * @param delivery true se filtrare solo con delivery
     * @param online true se filtrare solo con prenotazione online
     * @param starsMin min stelle
     * @param starsMax max stelle
     * @param favouriteUserId se >0 mostra solo i preferiti di tale utente
     * @param category filtro testo su tipo cucina (%LIKE%)
     * @return matrice con info paginata
     * @throws SQLException errore DB
     * @throws InterruptedException operazione interrotta
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

        if (category != null) {
            sql.append(" AND LOWER(r.tipo_cucina) LIKE LOWER(?)");
            params.add("%" + category + "%");
            types.add(3);
        }

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

    /**
     * Versione interna per calcolare il numero totale di risultati
     * con gli stessi filtri di {@link #getRestaurantsWithFilter}.
     *
     * @return numero totale di ristoranti filtrati
    */
    private int countRestaurantsWithFilter(
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

   /**
     * Recupera la recensione dell'utente per un dato ristorante.
     * Ogni utente ha al massimo una review per ristorante.
     *
     * @param userId ID utente
     * @param restId ID ristorante
     * @return array contenente:
     * <pre>
     * [0] stelle (String)
     * [1] testo recensione
     * </pre>
     * oppure {@code null} se non esiste
     *
     * @throws SQLException errore DB
     * @throws InterruptedException thread interrotto
     */
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
                int total = extractCount(rs);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

    /**
     * Restituisce la lista di recensioni del ristorante nella pagina selezionata.
     *
     * @param restId ID ristorante
     * @param page pagina richiesta, 0-based
     *
     * @return matrice tale che:
     * <pre>
     * [i][0] = id recensione
     * [i][1] = numero stelle
     * [i][2] = testo recensione
     * [i][3] = testo risposta del ristoratore (o null)
     * </pre>
     *
     * @throws SQLException errore DB
     * @throws InterruptedException thread interrotto
     */
    public String[][] getReviews(int restId, int page)
            throws SQLException, InterruptedException {

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
    
    /**
     * Recupera la recensione dell'utente per un dato ristorante.
     * Ogni utente ha al massimo una review per ristorante.
     *
     * @param userId ID utente
     * @param restId ID ristorante
     * @return array contenente:
     * <pre>
     * [0] stelle (String)
     * [1] testo recensione
     * </pre>
     * oppure {@code null} se non esiste
     *
     * @throws SQLException errore DB
     * @throws InterruptedException thread interrotto
     */
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
                        Integer.toString(rs.getInt("stelle")),
                        rs.getString("testo")
                };
            }
        }
    }

     /**
     * Inserisce una nuova recensione associata a un ristorante.
     * <p>
     * Se esiste già una recensione dell'utente su quel ristorante, NON viene gestito qui:
     * la responsabilità è di livello superiore (Handler o UI).
     * </p>
     *
     * @param userId ID dell'utente autore della recensione
     * @param restId ID del ristorante recensito
     * @param stars numero stelle assegnate (0-5)
     * @param text testo della recensione
     * @return true se l'inserimento ha avuto successo
     * @throws SQLException errore di database
     * @throws InterruptedException thread interrotto
     */    
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
    
    /**
     * Modifica una recensione inserita in precedenza dallo stesso utente.
     * <p>
     * Aggiorna esclusivamente:
     * <ul>
     *   <li>stelle</li>
     *   <li>testo recensione</li>
     * </ul>
     * L'utente deve coincidere con l'autore per rispettare i vincoli applicativi.
     * </p>
     *
     * @param userId ID dell'autore
     * @param restId ID del ristorante
     * @param stars nuove stelle assegnate
     * @param text nuovo commento
     * @return true se è stata modificata almeno 1 riga, false se la review non esiste
     * @throws SQLException errore di database
     * @throws InterruptedException thread interrotto
     */
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

     /**
     * Rimuove una recensione appartenente a un determinato utente.
     * <p>
     * La rimozione è vincolata da:
     * <ul>
     *   <li>ID utente deve corrispondere all'autore</li>
     *   <li>ID ristorante deve essere quello corretto</li>
     * </ul>
     * </p>
     *
     * @param userId ID autore
     * @param restId ID ristorante
     * @return true se eliminata, false se non esistente
     * @throws SQLException errore di database
     * @throws InterruptedException thread interrotto
     */
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


    /**
     * Verifica se l'utente è autorizzato a rispondere a una determinata recensione.
     * <p>
     * Condizione:
     * <ul>
     *     <li>l'utente deve essere proprietario del ristorante associato alla recensione</li>
     * </ul>
     * </p>
     *
     * @param userId utente autenticato
     * @param reviewId recensione
     * @return true se può rispondere
     * @throws SQLException errore DB
     * @throws InterruptedException thread interrotto
    */
    public boolean canRespond(int userId, int reviewId)
            throws SQLException, InterruptedException {

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
    /**
     * Restituisce la risposta del ristoratore a una recensione, se presente.
     *
     * @param reviewId ID recensione
     * @return stringa contenente risposta oppure null
     * @throws SQLException errore DB
     * @throws InterruptedException thread interrotto
     */
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
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }
    
    /**
     * Inserisce una risposta a una recensione.
     * Nota: non vengono gestite risposte multiple — la logica applicativa
     * garantisce una sola risposta per recensione.
     *
     * @param reviewId ID recensione
     * @param text testo risposta
     * @return true se registrata
     */
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

    /**
     * Aggiorna la risposta associata alla recensione.
     *
     * @param reviewId ID recensione
     * @param text testo aggiornato
     * @return true se modificata
     */
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
    
    /**
     * Rimuove la risposta associata alla recensione.
     *
     * @param reviewId ID recensione
     * @return true se eliminata
     */
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


    /* Preferiti */

     /**
     * Verifica se un ristorante appartiene ai preferiti dell'utente.
     *
     * @param userId utente
     * @param restId ristorante
     * @return true se presente nei preferiti
     */
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

    /**
     * Aggiunge un ristorante ai preferiti dell'utente.
     *
     * @param userId utente
     * @param restId ristorante
     * @return true se inserito correttamente
     */
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
    
    /**
     * Rimuove un ristorante dai preferiti dell'utente.
     *
     * @param userId utente
     * @param restId ristorante
     * @return true se rimosso correttamente
     */
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
}
