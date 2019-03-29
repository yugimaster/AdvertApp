package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.grandartisans.advert.sdk.LoginNettyServer;

public class LoginNettyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("LoginNettyServer Init");
        LoginNettyServer loginNettyServer = new LoginNettyServer(getApplicationContext());
        loginNettyServer.initLogin();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
