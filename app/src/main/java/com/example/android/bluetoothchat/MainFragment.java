package com.example.android.bluetoothchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.socket.client.Socket;

public class MainFragment extends Fragment {

    public Socket mSocket;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    public void attemptSend(String type, String message) {
        if (mSocket != null && mSocket.connected()) mSocket.emit(type, message);
    }

    public void attemptSend(String type, int message) {
        if (mSocket != null && mSocket.connected()) mSocket.emit(type, message);
    }

    public void attemptSend(String type, int id, int raceType, String message) {
        if (mSocket != null && mSocket.connected()) mSocket.emit(type, id, raceType, message);
    }
}