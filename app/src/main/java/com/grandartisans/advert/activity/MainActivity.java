package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.grandartisans.advert.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
        startActivity(intent);

    }
}
