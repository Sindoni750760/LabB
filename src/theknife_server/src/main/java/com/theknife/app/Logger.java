package com.theknife.app;

public interface Logger {

    void info(String msg);
    void warning(String msg);
    void error(String msg);
    void alert(String msg);

}
