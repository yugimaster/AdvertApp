package com.grandartisans.advert.receiver;

import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.service.RemoteService;
import com.grandartisans.advert.service.UpgradeService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Date;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "AdverBootReceiver";

	/**
	 * 配置文件
	 */
	private SharedPreferences sharedPreferences;
	/**
	 * 更改配置文件的类实例
	 */
	private SharedPreferences.Editor editor;
	Context mContext = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "==++++++++++== AdverBootReceiver action : " + intent.getAction());
        String action = intent.getAction();
		if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
			//Intent intentService = new Intent(context,RemoteService.class);
			//context.startService(intentService);
			sharedPreferences = context.getSharedPreferences("SystemBootTime", Context.MODE_PRIVATE);
			editor = sharedPreferences.edit();
			editor.putLong("SystemBootTime", new Date().getTime());
			editor.commit();

			Intent intentService = new Intent(context,UpgradeService.class);
			context.startService(intentService);
		}else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
			/*
			NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
			if(activeNetInfo!=null && activeNetInfo.isConnected() ){
				Intent intentService = new Intent(context,UpgradeService.class);
				context.startService(intentService);
			}
			*/
		}
	}
}
