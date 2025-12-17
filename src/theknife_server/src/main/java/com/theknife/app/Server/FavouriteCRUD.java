package com.theknife.app.Server;

import java.sql.*;

/**
 * Implementazione delle operazioni CRUD relative ai ristoranti preferiti
 * 
 * <p>
 * Questa classe gestisce i ristoranti segnalati come preferiti da un utente,
 * operando all'interno della tabella {@code preferiti}
 * </p>
 * 
 * <p>
 * Estende {@link GenericCRUD} per l'accesso condiviso alle risorse di connession e 
 * implementa l'interfaccia {@link QueryFavourite}, fornendo le query SQL specifiche
 * per il dominio "preferiti"
 * </p>
 * 
 * <p>
 * La classe viene utilizzata dal layer {@link com.theknife.app.Server.DBHandler}
 * </p>
 */
public class FavouriteCRUD
        extends GenericCRUD
        implements QueryFavourite {

    /**
     * Verifica se un ristorante è presente tra i preferiti di un utente.
     *
     * <p>
     * Esegue una query di selezione sulla tabella {@code preferiti}
     * verificando l'esistenza della coppia {@code (utente, ristorante)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se il ristorante è tra i preferiti dell'utente
     * @throws SQLException in caso di errore SQL
     * @throws InterruptedException in caso di interruzione del thread
     */
    @Override
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
     * Aggiunge un ristorante alla lista dei preferiti dell'utente.
     *
     * <p>
     * Inserisce una nuova riga nella tabella {@code preferiti}
     * associando l'utente al ristorante indicato.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se l'inserimento ha avuto successo
     * @throws SQLException in caso di errore SQL
     * @throws InterruptedException in caso di interruzione del thread
     */
    @Override
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
     * Rimuove un ristorante dalla lista dei preferiti dell'utente.
     *
     * <p>
     * Elimina la relazione tra utente e ristorante dalla tabella
     * {@code preferiti}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha avuto successo
     * @throws SQLException in caso di errore SQL
     * @throws InterruptedException in caso di interruzione del thread
     */
    @Override
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
