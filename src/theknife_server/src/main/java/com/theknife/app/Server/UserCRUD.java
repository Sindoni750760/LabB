package com.theknife.app.Server;

import java.sql.*;
import java.util.ArrayList;

/**
 * CRUD relativo agli UTENTI.
 * <p>
 * Contiene le query dirette sulla tabella {@code utenti} e tutte
 * le funzionalità legate all'utente come:
 * </p>
 * <ul>
 *     <li>Registrazione</li>
 *     <li>Recupero credenziali</li>
 *     <li>Recupero informazioni utente</li>
 *     <li>Lettura posizione geografica utente</li>
 *     <li>Lettura recensioni scritte dall'utente</li>
 * </ul>
 *
 * <p>
 * Estende {@link GenericCRUD}, ereditando:
 * </p>
 * <ul>
 *     <li>accesso thread-safe al {@link java.sql.Connection}</li>
 *     <li>metodi di utilità per query di conteggio</li>
 * </ul>
 */
public class UserCRUD 
    extends GenericCRUD {

    /**
     * Costruttore 
     */
    public UserCRUD(){
        
    }   

    /**
     * Inserisce un nuovo utente nella tabella {@code utenti}.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco
     * @param hashPassword password già hashata
     * @param birth timestamp della data di nascita oppure -1 se assente
     * @param lat latitudine del domicilio
     * @param lon longitudine del domicilio
     * @param isRist {@code true} se l'utente è ristoratore
     * @return {@code true} se l'inserimento ha avuto successo
     *
     * @throws SQLException problemi nella query SQL
     * @throws InterruptedException in caso l'operazione venga interrotta
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
     * Recupera le credenziali necessarie al login dell'utente.
     *
     * @param username username dell'utente
     *
     * @return array di lunghezza 2 con:
     *     <pre>
     *     [0] = id utente
     *     [1] = password hashata
     *     </pre>
     *     oppure {@code null} se l'utente non esiste
     *
     * @throws SQLException errore nella query SQL
     * @throws InterruptedException in caso di interruzione
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
     * Restituisce le informazioni di profilo utente.
     *
     * @param userId ID univoco dell'utente
     *
     * @return array:
     *     <pre>
     *     [0] = nome
     *     [1] = cognome
     *     [2] = "y" se ristoratore, "n" altrimenti
     *     </pre>
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
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
     * Restituisce la posizione geografica dell'utente.
     *
     * @param userId ID dell'utente
     *
     * @return vettore di lunghezza 2:
     *     <pre>
     *     [0] latitudine
     *     [1] longitudine
     *     </pre>
     *     oppure {@code null} se utente inesistente
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
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


    /**
     * Restituisce il numero di pagine contenenti le recensioni
     * scritte dall'utente.
     *
     * Convenzione applicativa → 10 recensioni per pagina.
     *
     * @param userId ID utente
     * @return numero di pagine >= 0
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException interruzione
     */
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
     * Recupera una pagina delle recensioni scritte dall’utente,
     * includendo anche il nome del ristorante.
     *
     * Formato ritorno:
     * <pre>
     * [
     *    [ nomeRistorante, stelle, testo ],
     *    ...
     * ]
     * </pre>
     *
     * @param userId ID dell'utente
     * @param page pagina (indice 0-based)
     * @return matrice di stringhe ordinata in ordine desc
     *         sulle recensioni più recenti
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
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
