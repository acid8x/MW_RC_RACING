package com.example.android.bluetoothchat;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication extends Application {

    public Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://robo-warz2.com:5000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
