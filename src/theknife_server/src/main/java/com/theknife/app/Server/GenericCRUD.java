package com.theknife.app.Server;

import java.sql.SQLException;

import com.theknife.app.ConnectionManager;

/**
 * Layer di base per tutti i CRUD.
 * Espone il ConnectionManager e (se vuoi) utilità comuni.
 */
public abstract class GenericCRUD {

    /** Gestore delle connessioni al database. */
    protected final ConnectionManager connMgr = ConnectionManager.getInstance();

    protected GenericCRUD() {
        // eventuali init comuni
    }

    /**
     * Utility opzionale per contare i record di una query "SELECT COUNT(*)".
     */
    protected int extractCount(java.sql.ResultSet rs) throws SQLException {
        if (!rs.next()) return 0;
        return rs.getInt(1);
    }
}
