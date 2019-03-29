
package com.grandartisans.advert.sdk.kernalcode.core;

import java.util.Observer;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.grandartisans.advert.sdk.kernalcode.ClientCoreSDK;

public class KeepAliveDaemon
{
	private final static String TAG = KeepAliveDaemon.class.getSimpleName();
	
	private static KeepAliveDaemon instance = null;
	
	public static int NETWORK_CONNECTION_TIME_OUT = 10 * 1000;
	public static int KEEP_ALIVE_INTERVAL = 3000;//1000;

	private boolean keepAliveRunning = false;
	private long lastGetKeepAliveResponseFromServerTimstamp = 0;
	private Observer networkConnectionLostObserver = null;

    private Handler handler = null;
    private Runnable runnable = null;
	private boolean _excuting = false;
    private boolean init = false;

	private Context context = null;
	
	public static KeepAliveDaemon getInstance(Context context)
	{
		if(instance == null)
			instance = new KeepAliveDaemon(context);
		return instance;
	}
	
	private KeepAliveDaemon(Context context)
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
					new AsyncTask<Object, Integer, Integer>()
					{
						private boolean willStop = false;
						
						@Override
						protected Integer doInBackground(Object... params)
						{
							_excuting = true;
							if(ClientCoreSDK.DEBUG)
								Log.d(TAG, "【IMCORE】心跳线程执行中...");
							int code = LocalUDPDataSender.getInstance(context).sendKeepAlive();
							
							return code;
						}

						@Override
						protected void onPostExecute(Integer code)
						{
							boolean isInitialedForKeepAlive = (lastGetKeepAliveResponseFromServerTimstamp == 0);
							if(code == 0 && lastGetKeepAliveResponseFromServerTimstamp == 0)
								lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();

							if(!isInitialedForKeepAlive)
							{
								long now = System.currentTimeMillis();
								if(now - lastGetKeepAliveResponseFromServerTimstamp >= NETWORK_CONNECTION_TIME_OUT)
								{
									stop();
									if(networkConnectionLostObserver != null)
										networkConnectionLostObserver.update(null, null);
									willStop = true;
								}
							}
							
							_excuting = false;
							if(!willStop)
								handler.postDelayed(runnable, KEEP_ALIVE_INTERVAL);
						}
					}.execute();
				}
			}
		};
	}
	
	public void stop()
	{
		handler.removeCallbacks(runnable);
		keepAliveRunning = false;
		lastGetKeepAliveResponseFromServerTimstamp = 0;
	}
	
	public void start(boolean immediately)
	{
		stop();
		
		handler.postDelayed(runnable, immediately ? 0 : KEEP_ALIVE_INTERVAL);
		keepAliveRunning = true;
	}
	
	public boolean isKeepAliveRunning()
	{
		return keepAliveRunning;
	}

    public boolean isInit()
    {
        return init;
    }
	
	public void updateGetKeepAliveResponseFromServerTimstamp()
	{
		lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();
	}

	public void setNetworkConnectionLostObserver(Observer networkConnectionLostObserver)
	{
		this.networkConnectionLostObserver = networkConnectionLostObserver;
	}
}
