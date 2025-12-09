package com.theknife.app.Server;

import java.sql.SQLException;

import com.theknife.app.ConnectionManager;

/**
 * Layer di base comune a tutte le classi CRUD del server.
 * <p>
 * Fornisce:
 * <ul>
 *     <li>Accesso al {@link ConnectionManager} per la gestione delle connessioni</li>
 *     <li>Metodi di utilità comuni ai livelli CRUD superiori</li>
 * </ul>
 * 
 * Questa classe è astratta e non viene istanziata direttamente.
 */
public abstract class GenericCRUD {

    /**
     * Gestore delle connessioni al database, usato da tutte le sottoclassi CRUD.
     */
    protected final ConnectionManager connMgr = ConnectionManager.getInstance();

    /**
     * Costruttore protetto, invocato dalle classi CRUD derivate.
     * <p>
     * Può essere usato per inizializzazioni comuni.
     */
    protected GenericCRUD() {
        // eventuali init comuni
    }

    /**
     * Estrae il valore numerico risultante da una query "SELECT COUNT(*) ..."
     * su una tabella o una vista.
     *
     * @param rs ResultSet ottenuto dall'esecuzione della query
     * @return il valore di COUNT(*) oppure 0 se non è presente alcuna riga
     * @throws SQLException se l'accesso al ResultSet genera errori
     */
    protected int extractCount(java.sql.ResultSet rs) throws SQLException {
        if (!rs.next()) return 0;
        return rs.getInt(1);
    }
}
