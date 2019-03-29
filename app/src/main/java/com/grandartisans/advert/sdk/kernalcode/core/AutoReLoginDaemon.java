
package com.grandartisans.advert.sdk.kernalcode.core;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.grandartisans.advert.sdk.kernalcode.ClientCoreSDK;

public class AutoReLoginDaemon
{
	private final static String TAG = AutoReLoginDaemon.class.getSimpleName();
	
	private static AutoReLoginDaemon instance = null;
	
	public static int AUTO_RE$LOGIN_INTERVAL = 2000;

	private Handler handler = null;
	private Runnable runnable = null;
	private boolean autoReLoginRunning = false;
	private boolean _excuting = false;
	private Context context = null;
    private boolean init = false;
	
	public static AutoReLoginDaemon getInstance(Context context)
	{
		if(instance == null)
			instance = new AutoReLoginDaemon(context);
		return instance;
	}
	
	private AutoReLoginDaemon(Context context)
	{
		this.context = context;
		init();
	}
	
	private void init()
	{
        if(init)
            return;

		handler = new Handler();
		runnable = new Runnable(){
			@Override
			public void run()
			{
				if(!_excuting)
				{
					new AsyncTask<Object, Integer, Integer>(){
						@Override
						protected Integer doInBackground(Object... params)
						{
							_excuting = true;
							if(ClientCoreSDK.DEBUG)
								Log.d(TAG, "【IMCORE】自动重新登陆线程执行中, autoReLogin?"+ClientCoreSDK.autoReLogin+"...");
							int code = -1;
							
							if(ClientCoreSDK.autoReLogin)
							{
								code = LocalUDPDataSender.getInstance(context).sendLogin(
										ClientCoreSDK.getInstance().getCurrentLoginUserId()
										, ClientCoreSDK.getInstance().getCurrentLoginToken()
										, ClientCoreSDK.getInstance().getCurrentLoginExtra());
							}
							return code;
						}

						@Override
						protected void onPostExecute(Integer result)
						{
							if(result == 0)
							{
								LocalUDPDataReciever.getInstance(context).startup();
							}

							_excuting = false;
							handler.postDelayed(runnable, AUTO_RE$LOGIN_INTERVAL);
						}
					}.execute();
				}
			}
		};

        init = true;
	}
	
	public void stop()
	{
		handler.removeCallbacks(runnable);
		autoReLoginRunning = false;
	}
	
	public void start(boolean immediately)
	{
		stop();
		
		handler.postDelayed(runnable, immediately ? 0 : AUTO_RE$LOGIN_INTERVAL);
		autoReLoginRunning = true;
	}
	
	public boolean isAutoReLoginRunning()
	{
		return autoReLoginRunning;
	}

    public boolean isInit()
    {
        return init;
    }
}
