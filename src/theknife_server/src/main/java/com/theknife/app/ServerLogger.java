package com.theknife.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLogger implements Logger {

    private static ServerLogger instance = null;

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ServerLogger() {}

    public static synchronized ServerLogger getInstance() {
        if (instance == null)
            instance = new ServerLogger();
        return instance;
    }

    // ===========================================
    //  CORE
    // ===========================================
    private synchronized void log(String level, String msg) {
        String time = sdf.format(new Date());
        System.out.println("[" + time + "][" + level + "] " + msg);
    }

    @Override
    public void info(String msg) {
        log("INFO", msg);
    }

    @Override
    public void warning(String msg) {
        log("WARNING", msg);
    }

    @Override
    public void error(String msg) {
        log("ERROR", msg);
    }

    @Override
    public void alert(String msg) {
        log("ALERT", msg);
    }
}
