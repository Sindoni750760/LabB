package com.theknife.app.Handler;

import java.io.IOException;

public class ClientException extends IOException{
    public ClientException(String message){
        super(message);
    }
}
