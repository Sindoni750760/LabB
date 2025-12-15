package com.theknife.app.Server;

import java.sql.*;

public class FavouriteCRUD
        extends GenericCRUD
        implements QueryFavourite {

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
