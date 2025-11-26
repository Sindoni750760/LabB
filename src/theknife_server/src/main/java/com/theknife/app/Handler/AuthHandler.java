package com.theknife.app.Handler;


import java.io.IOException;
import java.sql.SQLException;

import com.theknife.app.User;

public class AuthHandler implements CommandHandler {

    private final User userService = User.getInstance();
    @Override
    public boolean handle(String cmd, ClientContext ctx) throws IOException, SQLException {
        try{
            switch (cmd) {
            case "login"        -> handleLogin(ctx);
            case "register"     -> handleRegister(ctx);
            case "logout"       -> handleLogout(ctx);
            case "getUserInfo"  -> handleGetUserInfo(ctx);

            default -> { return false; }
        }
    } catch(InterruptedException ignored){

        }finally{
            return true;
        }
    }


    // ============================================================
    //                         LOGIN
    // ============================================================

    private void handleLogin(ClientContext ctx) throws IOException, SQLException, InterruptedException {

        String username = ctx.read();
        String password = ctx.read();

        int id = userService.loginUser(username, password);

        if (id == -1) {          // username non trovato
            ctx.write("username");
            return;
        }

        if (id <= 0) {           // password errata
            ctx.write("password");
            return;
        }

        ctx.setLoggedUserId(id);
        ctx.write("ok");
    }

    // ============================================================
    //                         REGISTER
    // ============================================================

    private void handleRegister(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        String nome     = ctx.read();
        String cognome  = ctx.read();
        String username = ctx.read();
        String password = ctx.read();
        String data     = ctx.read();
        String latStr   = ctx.read();
        String lonStr   = ctx.read();
        boolean rist    = "y".equals(ctx.read());

        String esito = userService.registerUser(
                nome, cognome, username, password,
                data, latStr, lonStr, rist
        );

        ctx.write(esito);
    }


    // ============================================================
    //                         LOGOUT
    // ============================================================

    private void handleLogout(ClientContext ctx) throws IOException {
        ctx.setLoggedUserId(-1);
        ctx.write("ok");
    }

    // ============================================================
    //                      GET USER INFO
    // ============================================================

    private void handleGetUserInfo(ClientContext ctx) throws IOException, SQLException, InterruptedException {

        int id = ctx.getLoggedUserId();

        // Utente non loggato
        if (id <= 0) {
            ctx.write("");
            ctx.write("");
            ctx.write("n");
            return;
        }

        // Recupera info dal DB
        // (nome, cognome, y/n)
        String[] info = userService.getUserInfo(id);

        if (info == null) {
            ctx.write("");
            ctx.write("");
            ctx.write("n");
            return;
        }

        ctx.write(info[0]); // nome
        ctx.write(info[1]); // cognome
        ctx.write(info[2]); // "y" o "n"
    }
}
