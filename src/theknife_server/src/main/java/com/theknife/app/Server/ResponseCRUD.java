package com.theknife.app.Server;

import java.sql.*;

/**
 * CRUD specializzato per la gestione delle risposte del ristoratore alle recensioni.
 *
 * <p>
 * Questa classe implementa tutte le operazioni di accesso ai dati
 * relative alle risposte associate alle recensioni:
 * </p>
 * <ul>
 *     <li>verifica dei permessi di risposta</li>
 *     <li>lettura della risposta</li>
 *     <li>inserimento, modifica e rimozione della risposta</li>
 * </ul>
 *
 * <p>
 * Estende {@link RestaurateurCRUD} per riutilizzare
 * i controlli di accesso e la gestione delle connessioni.
 * </p>
 *
 * <p>
 * Implementa l'interfaccia {@link QueryResponse},
 * fornendo un'implementazione concreta delle query SQL.
 * </p>
 *
 * <p>Pattern applicati:</p>
 * <ul>
 *     <li><b>CRUD</b></li>
 * </ul>
 */
public class ResponseCRUD
        extends RestaurateurCRUD
        implements QueryResponse {

    /**
     * Verifica se un ristoratore ha il permesso di rispondere a una recensione.
     *
     * <p>
     * Il controllo avviene verificando che la recensione
     * sia associata a un ristorante di cui l'utente è proprietario.
     * </p>
     *
     * @param userId id dell'utente ristoratore
     * @param reviewId id della recensione
     * @return {@code true} se l'utente può rispondere,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
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
     * Recupera la risposta associata a una recensione.
     *
     * <p>
     * Se la recensione non ha una risposta,
     * il metodo restituisce {@code null}.
     * </p>
     *
     * @param reviewId id della recensione
     * @return testo della risposta oppure {@code null}
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
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
     * Inserisce una nuova risposta associata a una recensione.
     *
     * <p>
     * Ogni recensione può avere al massimo una risposta.
     * </p>
     *
     * @param reviewId id della recensione
     * @param text testo della risposta
     * @return {@code true} se l'inserimento ha successo,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
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
     * Modifica una risposta già esistente associata a una recensione.
     *
     * @param reviewId id della recensione
     * @param text nuovo testo della risposta
     * @return {@code true} se la modifica ha successo,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
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
     * Rimuove la risposta associata a una recensione.
     *
     * @param reviewId id della recensione
     * @return {@code true} se la rimozione ha successo,
     *         {@code false} altrimenti
     *
     * @throws SQLException errori di accesso al database
     * @throws InterruptedException gestione concorrenza
     */
    @Override
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
}
