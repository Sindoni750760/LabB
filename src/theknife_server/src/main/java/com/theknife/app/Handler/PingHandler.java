package com.theknife.app.Handler;

import java.io.IOException;

public class PingHandler implements CommandHandler {

    @Override
    public boolean handle(String cmd, ClientContext ctx) throws IOException {
        if (!"ping".equals(cmd)) return false;

        ctx.write("pong");
        return true;
    }
}
