package com.theknife.app.Server;

import java.sql.SQLException;

public interface QueryUser {

    boolean addUser(
            String nome,
            String cognome,
            String username,
            String passwordHashed,
            java.sql.Date dataNascita,
            double lat,
            double lon,
            boolean isRistoratore
    ) throws SQLException, InterruptedException;

    boolean userExists(String username) throws SQLException, InterruptedException;

    String[] getUserLoginInfo(String username) throws SQLException, InterruptedException;

    String[] getUserInfoById(int id) throws SQLException, InterruptedException;

}
