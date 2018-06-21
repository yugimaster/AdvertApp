package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.grandartisans.advert.server.RemoteServer;

import java.io.IOException;

public class RemoteService extends Service {
    private final String TAG = "RemoteService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand" );
        RemoteServer myServer = new RemoteServer(9996);
        try {
            Log.i(TAG, "start remote server " );
            myServer.start();
            Log.i(TAG, "start remote server sucefull" );
        } catch (IOException e) {
            Log.i(TAG, "start remote server eror" );
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
