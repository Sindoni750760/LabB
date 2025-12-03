package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD relativo agli UTENTI.
 * Contiene le query che lavorano sulla tabella utenti
 * e le funzioni legate alle recensioni scritte dall'utente.
 */
public abstract class UserCRUD extends GenericCRUD {

    /* User */

    /**
     *  birth espresso in millisecondi (o -1 se assente).
     */
    public boolean addUser(String nome, String cognome, String username, String hashPassword,
                           long birth, double lat, double lon, boolean isRist)
            throws SQLException, InterruptedException {

        String sql = """
            INSERT INTO utenti
                (nome, cognome, username, password, data_nascita,
                 latitudine_domicilio, longitudine_domicilio, is_ristoratore)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username);
            ps.setString(4, hashPassword);

            if (birth <= 0) {
                ps.setDate(5, null);
            } else {
                ps.setDate(5, new java.sql.Date(birth));
            }

            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setBoolean(8, isRist);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Ritorna [id, hashPassword] oppure null se non trovato.
     */
    public String[] getUserLoginInfo(String username)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT id, password
            FROM utenti
            WHERE username = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new String[]{
                        Integer.toString(rs.getInt("id")),
                        rs.getString("password")
                };
            }
        }
    }

    /**
     * Ritorna [nome, cognome, "y"/"n" is_ristoratore].
     */
    public String[] getUserInfo(int userId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT nome, cognome, is_ristoratore
            FROM utenti
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new String[]{
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getBoolean("is_ristoratore") ? "y" : "n"
                };
            }
        }
    }

    /**
     * Ritorna [latitudine, longitudine] del domicilio utente.
     */
    public double[] getUserPosition(int userId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT latitudine_domicilio, longitudine_domicilio
            FROM utenti
            WHERE id = ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new double[]{ rs.getDouble(1), rs.getDouble(2) };
            }
        }
    }


    /* Recensioni dell'utente */

    public int getUserReviewsPages(int userId)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT COUNT(*)
            FROM recensioni
            WHERE id_utente = ?
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
     * Ritorna String[][] con:
     *  [i][0] = nome ristorante
     *  [i][1] = stelle
     *  [i][2] = testo recensione
     */
    public String[][] getUserReviews(int userId, int page)
            throws SQLException, InterruptedException {

        String sql = """
            SELECT r2.nome, r.stelle, r.testo
            FROM recensioni r
            JOIN "RistorantiTheKnife" r2 ON r.id_ristorante = r2.id
            WHERE r.id_utente = ?
            ORDER BY r.id DESC
            LIMIT 17 OFFSET ?
        """;

        try (Connection conn = connMgr.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, page * 10);

            java.util.List<String[]> out = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            rs.getString(1),
                            Integer.toString(rs.getInt(2)),
                            rs.getString(3)
                    });
                }
            }

            return out.toArray(new String[0][]);
        }
    }
}
