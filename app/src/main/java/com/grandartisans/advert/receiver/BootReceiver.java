package com.grandartisans.advert.receiver;

import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.service.RemoteService;
import com.grandartisans.advert.service.UpgradeService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "AdverBootReceiver";
	Context mContext = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "==++++++++++== AdverBootReceiver action : " + intent.getAction());
        String action = intent.getAction();
		if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
			//Intent intentService = new Intent(context,RemoteService.class);
			//context.startService(intentService);
		}else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
			NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
			if(activeNetInfo!=null && activeNetInfo.isConnected() ){
				Intent intentService = new Intent(context,UpgradeService.class);
				context.startService(intentService);
			}
		}
	}
}
