package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.entity.DownloadInfo;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private final int PLAYER_START_CMD = 100001;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message paramMessage)
        {
            switch (paramMessage.what)
            {
                case PLAYER_START_CMD:

                    break;
            }
            super.handleMessage(paramMessage);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");
        mHandler.sendEmptyMessageDelayed(PLAYER_START_CMD,1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        mHandler.removeMessages(PLAYER_START_CMD);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void startAdPlayer(){
        Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
        startActivity(intent);
    }
}
