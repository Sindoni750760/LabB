package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler specializzato: gestione risposte del ristoratore alle recensioni.
 */
public class ResponseHandler implements CommandHandler {

    private static ResponseHandler instance = null;

    public static synchronized ResponseHandler getInstance() {
        if (instance == null)
            instance = new ResponseHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private ResponseHandler() {}

    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        switch (cmd) {
            case "getResponse"    -> { handleGetResponse(ctx); return true; }
            case "addResponse"    -> { handleAddResponse(ctx); return true; }
            case "editResponse"   -> { handleEditResponse(ctx); return true; }
            case "removeResponse" -> { handleRemoveResponse(ctx); return true; }
            default -> { return false; }
        }
    }

    private void handleGetResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("none");
            return;
        }

        String resp = db.getResponse(reviewId);

        if (resp == null) {
            ctx.write("none");
        } else {
            ctx.write("ok");
            ctx.write(resp);
        }
    }

    private void handleAddResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        String text = ctx.read();

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.addResponse(reviewId, text);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleEditResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        String text = ctx.read();

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.editResponse(reviewId, text);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleRemoveResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.removeResponse(reviewId);
        ctx.write(ok ? "ok" : "error");
    }
}
