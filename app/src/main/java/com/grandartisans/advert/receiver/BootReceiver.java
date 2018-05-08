package com.grandartisans.advert.receiver;

import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.service.UpgradeService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "AdverBootReceiver";
	Context mContext = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "==++++++++++== AdverBootReceiver action : " + intent.getAction());
        String action = intent.getAction();
		if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
			Intent it=new Intent(context,MediaPlayerActivity.class);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(it);

			//Intent intentService = new Intent(context,UpgradeService.class);
			//context.startService(intentService);
		}
	}
}
