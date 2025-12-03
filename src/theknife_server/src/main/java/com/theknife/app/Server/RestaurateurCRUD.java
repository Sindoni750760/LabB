package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD specifico per il RISTORATORE:
 *   - controllo accesso ai ristoranti
 *   - elenco dei ristoranti di cui è proprietario
 */
public abstract class RestaurateurCRUD extends UserCRUD {

    /**
     * Verifica se l'utente ha accesso (è proprietario) del ristorante.
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
     * Numero di pagine di ristoranti gestiti dall'utente.
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
     * Lista (pagina) di ristoranti gestiti dall'utente.
     * Ogni elemento: [ id_ristorante, nome ].
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
