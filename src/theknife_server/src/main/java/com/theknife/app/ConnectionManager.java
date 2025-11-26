package com.theknife.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ConnectionManager {

    private static ConnectionManager instance = null;

    private final LinkedList<Connection> cache = new LinkedList<>();
    private final Semaphore semaphore;
    private final int MAX_CONNECTIONS = 5;

    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/theknife";
    private final String user = "postgres";
    private final String pass = "Federico.2006";

    private ConnectionManager() {
        this.semaphore = new Semaphore(MAX_CONNECTIONS, true);
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

    public Connection getConnection() throws SQLException, InterruptedException {
        semaphore.acquire();

        synchronized (cache) {
            if (!cache.isEmpty())
                return cache.removeFirst();
        }
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    public void releaseConnection(Connection c) {
        if (c != null) {
            synchronized (cache) {
                cache.addLast(c);
            }
            semaphore.release();
        }
    }

    public void flush() {
        synchronized (cache) {
            for (Connection c : cache) {
                try { c.close(); } catch (SQLException ignored) {}
            }
            cache.clear();
        }
    }
}
