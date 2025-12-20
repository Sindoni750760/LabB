package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer CRUD dedicato alle operazioni relative ai ristoratori.
 * <p>
 * Fornisce funzionalità di alto livello per verificare i permessi
 * e recuperare i ristoranti gestiti da un utente.
 * </p>
 * <p>
 * Estende {@link UserCRUD} in quanto opera su ristoranti associati
 * a utenti proprietari.
 * </p>
 *
 * Funzionalità principali:
 * <ul>
 *     <li>Controllo accesso ai ristoranti (ownership)</li>
 *     <li>Paginazione dei ristoranti di un dato ristoratore</li>
 *     <li>Lettura elenco sintetico dei ristoranti posseduti</li>
 * </ul>
 */
public abstract class RestaurateurCRUD extends UserCRUD {
    /**
     * Costruttore 
     */
    public RestaurateurCRUD(){
        
    }

    /**
     * Verifica se un utente è autorizzato a modificare un ristorante.
     *
     * <p>Si basa sull'associazione diretta:</p>
     *
     * <pre>
     * "RistorantiTheKnife".proprietario = userId
     * </pre>
     *
     * @param userId ID dell'utente che tenta l'accesso
     * @param restId ID del ristorante oggetto dell'operazione
     * @return {@code true} se l'utente è proprietario del ristorante,
     *         {@code false} altrimenti
     *
     * @throws SQLException errore durante la query SQL
     * @throws InterruptedException se il thread viene interrotto
     */

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

    /**
     * Restituisce il numero di pagine contenenti i ristoranti
     * appartenenti all'utente indicato.
     * <p>
     * Convenzione applicativa → 10 elementi per pagina.
     * </p>
     *
     * @param userId ID del ristoratore
     * @return numero di pagine (>= 0)
     *
     * @throws SQLException errore durante la query SQL
     * @throws InterruptedException se il thread viene interrotto
     */
    public int getUserRestaurantsPages(int userId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM "RistorantiTheKnife"
            WHERE proprietario = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                int total = extractCount(rs);
                return (int) Math.ceil(total / 10.0);
            }
        }
    }

     /**
     * Restituisce una pagina dei ristoranti gestiti dall'utente indicato.
     *
     * <p>Formato risultato:</p>
     * <pre>
     * [
     *   [ idRistorante, nome ],
     *   [ idRistorante, nome ],
     *   ...
     * ]
     * </pre>
     *
     * <p>Convenzioni:</p>
     * <ul>
     *     <li>ordinamento alfabetico per nome</li>
     *     <li>paginazione basata su page (indice 0-based)</li>
     *     <li>limite massimo di 17 righe per query</li>
     * </ul>
     *
     * @param userId ID proprietario dei ristoranti
     * @param page numero di pagina (0-based)
     * @return matrice contenente ID e nome dei ristoranti
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */
    public String[][] getUserRestaurants(int userId, int page)
            throws SQLException, InterruptedException {

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
}
