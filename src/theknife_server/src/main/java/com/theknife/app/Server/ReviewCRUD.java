package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewCRUD
        extends GenericCRUD
        implements QueryReview {

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
