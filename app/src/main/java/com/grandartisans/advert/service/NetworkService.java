package com.grandartisans.advert.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.grandartisans.advert.utils.NetUtil;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkService extends Service {

    private static final String TAG = NetworkService.class.getSimpleName();
    private int IntentId;
    private GetConnectState onGetConnectState;
    private Binder binder = new MyBinder();
    private boolean isConnected = true;

    // 实时监控网络状态改变
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Timer timer = new Timer();
                timer.schedule(new QunXTask(getApplicationContext()), new Date());
            }
        }
    };

    public void setOnGetConnectState(GetConnectState onGetConnectState) {
        this.onGetConnectState = onGetConnectState;
    }

    public interface GetConnectState {
        // 网络状态改变之后，通过此接口的实例通知当前网络的状态，此接口在Activity中注入实例对象
        public void GetState(int isConnected);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        // 注册广播
        IntentFilter intentFilter = new IntentFilter();
        // 添加接收网络连接状态改变的Action
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 删除广播
        unregisterReceiver(mReceiver);
    }

    class QunXTask extends TimerTask {

        private Context context;

        public QunXTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            IntentId = NetUtil.getNetworkState(context);

            if (onGetConnectState != null) {
                onGetConnectState.GetState(IntentId);
                Log.i(TAG, "Network state changed to: " + IntentId);
            }
        }
    }

    public class MyBinder extends Binder {

        public NetworkService getService() {
            return NetworkService.this;
        }
    }
}
