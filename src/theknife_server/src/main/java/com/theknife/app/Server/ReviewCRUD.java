package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD dedicato alla gestione delle recensioni dei ristoranti.
 *
 * <p>
 * Fornisce operazioni di lettura e scrittura sulle recensioni,
 * includendo:
 * </p>
 * <ul>
 *     <li>paginazione delle recensioni di un ristorante</li>
 *     <li>recupero della recensione dell'utente corrente</li>
 *     <li>inserimento, modifica e rimozione delle recensioni</li>
 * </ul>
 *
 * <p>
 * La classe estende {@link GenericCRUD} per l’accesso condiviso
 * al {@link com.theknife.app.ConnectionManager} e implementa
 * l’interfaccia {@link QueryReview}.
 * </p>
 */
public class ReviewCRUD
        extends GenericCRUD
        implements QueryReview {

    /**
     * Costruttore 
     */
    public ReviewCRUD(){
        
    }
    
    /**
     * Restituisce il numero di pagine di recensioni associate a un ristorante.
     *
     * <p>
     * La paginazione segue la convenzione applicativa di
     * <b>10 recensioni per pagina</b>.
     * </p>
     *
     * @param restId ID del ristorante
     * @return numero totale di pagine disponibili
     *
     * @throws SQLException errore durante l'accesso al database
     * @throws InterruptedException se il thread viene interrotto
     */
    @Override
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
     * Restituisce una pagina di recensioni per il ristorante indicato.
     *
     * <p>Formato del risultato:</p>
     * <pre>
     * [
     *   [ idRecensione, stelle, testo, risposta | null ],
     *   ...
     * ]
     * </pre>
     *
     * <p>
     * Le recensioni sono ordinate in modo decrescente per ID
     * (le più recenti prima).
     * </p>
     *
     * @param restId ID del ristorante
     * @param page indice della pagina (0-based)
     * @return matrice contenente le recensioni e l'eventuale risposta
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */

    @Override
    public String[][] getReviews(int restId, int page)
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
                            rs.getString(4)
                    });
                }
            }

            return out.toArray(new String[0][]);
        }
    }

    /**
     * Restituisce la recensione dell'utente corrente per un dato ristorante.
     *
     * <p>
     * Se l'utente non ha ancora recensito il ristorante,
     * il metodo restituisce {@code null}.
     * </p>
     *
     * @param userId ID dell'utente
     * @param restId ID del ristorante
     * @return array contenente [stelle, testo] oppure {@code null}
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */

    @Override
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

    /**
     * Inserisce una nuova recensione per un ristorante.
     *
     * @param userId ID dell'utente autore della recensione
     * @param restId ID del ristorante
     * @param stars numero di stelle assegnate
     * @param text testo della recensione
     * @return {@code true} se l'inserimento è avvenuto correttamente
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */
    @Override
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
     * Modifica una recensione esistente dell'utente per un ristorante.
     *
     * @param userId ID dell'utente autore
     * @param restId ID del ristorante
     * @param stars nuovo numero di stelle
     * @param text nuovo testo della recensione
     * @return {@code true} se la modifica è avvenuta correttamente
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */
    @Override
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
     * Cancella una recensione esistente dell'utente.
     *
     * @param userId ID dell'utente autore
     * @param restId ID del ristorante
     * @return {@code true} se la modifica è avvenuta correttamente     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */
    @Override
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
}
