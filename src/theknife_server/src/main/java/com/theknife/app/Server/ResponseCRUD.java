package com.theknife.app.Server;

import java.sql.*;

public class ResponseCRUD
        extends RestaurateurCRUD
        implements QueryResponse {

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
