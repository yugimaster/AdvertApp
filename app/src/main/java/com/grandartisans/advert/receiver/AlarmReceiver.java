package com.grandartisans.advert.receiver;

import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.service.UpgradeService;
import com.ljy.devring.DevRing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "AlarmReceiver";
	Context mContext = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "=== AlarmReceiver action : " + intent.getAction());
        String action = intent.getAction();
		EventBus.getDefault().postSticky(new AppEvent(AppEvent.POWER_SET_ALARM_EVENT,action));
		;

	}
}
