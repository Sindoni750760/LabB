package com.theknife.app.Handler;

import java.io.IOException;

public class DisconnectHandler implements CommandHandler {

    @Override
    public boolean handle(String cmd, ClientContext ctx) throws IOException {
        if (!"disconnect".equals(cmd)) return false;

        ctx.write("bye");
        ctx.deactivate();
        return true;
    }
}
