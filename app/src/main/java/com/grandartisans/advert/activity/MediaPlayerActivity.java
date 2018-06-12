package com.grandartisans.advert.activity;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.model.entity.DownloadInfo;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPosition;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.AdvertVersion;
import com.grandartisans.advert.utils.FileUtils;
import com.grandartisans.advert.utils.SerialPortUtils;

import com.grandartisans.advert.R;
import com.grandartisans.advert.view.MySurfaceView;
import com.ljy.devring.DevRing;
import com.prj.utils.PrjSettingsManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import gartisans.hardware.pico.PicoClient;

public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback{
	private final String TAG = "MediaPlayerActivity";
	private MediaPlayer mMediaPlayer;
	private MySurfaceView surface;
	private SurfaceHolder surfaceHolder;;

	private TextView messageTV ;
	private int playindex = 0;
	private List<PlayingAdvert> adurls = new ArrayList<PlayingAdvert>();

	private List<PlayingAdvert> adurls_local = new ArrayList<PlayingAdvert>();

	List<DateScheduleVo> dateScheduleVos;
	/*
	{"/storage/udisk0/work/videos/58c0d0b04f872.mp4",
								"/storage/udisk0/work/videos/58c0d1055b43c.mp4",
								"/storage/udisk0/work/videos/58c0d14d425d5.mp4"};
	*/
	private Handler handler;
	private int distance = 0;
	private int strength = 0;
	private int threshold_distance = 0;
	private int lastdistance = 0;
	public static final String I2C2_SLAVE_NODE = "/sys/class/i2c2/slave";
	private PrjSettingsManager prjmanager = null;
	private static Dialog distanceSetDialog = null;
	private int screenStatus = 1;
	private SerialPortUtils serialPortUtils = null;

	private volatile boolean isPowerOff = false;//定时待机状态

	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;

	private boolean scaleMode = false;

	private final  int threshold_temperature = 52;
	private  final int temperature_read_times = 30; //连续获取到温度超过指定值次数大于该值，则认为温度过高了
	private int temperature_read_count=0;

	private PicoClient pClient= null;

	private boolean  player_first_time = true;

	private final int SET_SCREEN_ON_CMD = 100010;
	private final int START_PLAYER_CMD = 100011;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message paramMessage)
		{
			switch (paramMessage.what)
			{
				case SET_SCREEN_ON_CMD:
					setScreen(1);
					if (mMediaPlayer != null)
						mMediaPlayer.start();
					break;
				case START_PLAYER_CMD:
					initPlayer();
					break;
				default:
					break;
			}
			super.handleMessage(paramMessage);
		}
	};

	/**
	 * <功能描述> 保持屏幕常亮
	 *
	 * @return void [返回类型说明]
	 */
	private void keepScreenWake() {
		// 获取WakeLock锁，保持屏幕常亮
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//mPowerManager.wakeUp(SystemClock.uptimeMillis());
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, this
				.getClass().getCanonicalName());
		mWakeLock.acquire();
	}
	/**
	 * <功能描述> 释放WakeLock
	 *
	 * @return void [返回类型说明]
	 */
	private void releaseWakeLock() {
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
    ReentrantLock lock = new ReentrantLock();
    //开线程更新UI
	Runnable runnableAlarm = new Runnable() {
		@Override
		public void run() {
			//initPowerOffAlarm(22,00,00);// 设置定时关机提醒
			initPowerOffAlarm(22,00,00);// 设置定时关机提醒

			initPowerOnAlarm(07,00,00);//设置定时开机提醒
		}
	};

	Runnable runableSetPowerOff = new Runnable() {
		@Override
		public void run() {
            lock.lock();
                isPowerOff = true;

                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                setScreen(0);
            lock.unlock();

			//initPowerOnAlarm(13,10,00);
		}
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_surface_player);
		Log.i(TAG,"onCreate");

		keepScreenWake();



		handler = new Handler();
		handler.postDelayed(runnableAlarm,1000*60*5);
		prjmanager = PrjSettingsManager.getInstance(this);


		initView();

		initEventBus();//注册事件接收

		initTFMini();//初始化激光测距模块

		initVideoList();

		setDisplay();


		/*
		Intent intentService = new Intent(MediaPlayerActivity.this,UpgradeService.class);
		startService(intentService);
		*/

		PicoClient.OnEventListener mPicoOnEventListener = new PicoClient.OnEventListener() {
		    @Override
            public void onEvent(PicoClient client, int etype, Object einfo) {
		        //Log.d(TAG, "PicoClient onEvent" + "etype " + etype + " einfo " + (Float)einfo);

		        if((Float)einfo  > threshold_temperature ) {
					Log.d(TAG, "read temperature onEvent" + "etype " + etype + " einfo " + (Float)einfo +  "is to high");
					temperature_read_count ++ ;
				}else {
					temperature_read_count = 0;
				}

				if(temperature_read_count > temperature_read_times) {
					Log.e(TAG, "temperature is too high , device will power off ");
				}

		    }
        };
		pClient = new PicoClient(mPicoOnEventListener, null);

	}
	private void initView(){
		surface = (MySurfaceView) findViewById(R.id.surface);

		surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	/*
	 * 初始化播放首段视频的player
	 */
	private void initFirstPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setDisplay(surfaceHolder);
		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						onVideoPlayCompleted(mp);
					}
				});
		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
			@Override public void onPrepared(MediaPlayer mp)
			{

				Log.i(TAG,"video width = " + mMediaPlayer.getVideoWidth() + "video height = " + mMediaPlayer.getVideoHeight());
				//mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
				mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
				mMediaPlayer.start();
				if(player_first_time == true) {
					player_first_time = false;
					mMediaPlayer.seekTo(mMediaPlayer.getDuration());
				}

			}
		});

		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener(){
		    @Override public boolean onError(MediaPlayer mp,int what, int extra)
            {
                Log.d(TAG, "OnError - Error code: " + what + " Extra code: " + extra);
                return false;
            }

        });

		mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener(){
			@Override public void onVideoSizeChanged(MediaPlayer mp,int width,int height) {
				Log.d(TAG, "setOnVideoSizeChangedListener  width: " + width + "height: " + height);
			}
		});

	}

	private void startPlay(String url) {
		try {
			//mMediaPlayer.setDataSource(url);
			mMediaPlayer.setDataSource(MediaPlayerActivity.this,Uri.parse(url));
			mMediaPlayer.prepareAsync();
			//mMediaPlayer.prepare();
			//mMediaPlayer.start();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	private void startPlayDefault(){
		/*
		AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.defaultvideo);
		try {
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
					file.getLength());
			//mMediaPlayer.prepare();
			mMediaPlayer.prepareAsync();
			//mMediaPlayer.start();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	private void initPlayer(){
        initFirstPlayer();
        String url = getValidUrl();
		mMediaPlayer.reset();
        if(url!=null && url.length()>0) {
            startPlay(url);
        }else {
            startPlayDefault();

        }
    }
	private void onVideoPlayCompleted(MediaPlayer mp) {
		//get next player
		if(!isPowerOff) {
			mMediaPlayer.reset();
			playindex++;
			String url = getValidUrl();
			if (url != null && url.length() > 0) {
				startPlay(url);
			} else {
				startPlayDefault();
			}
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder,  int format, int width,int height) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"surfaceChanged111 format = " + format + "width = "  + width  + "height = " + height);
		//holder.setFixedSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		//然后初始化播放手段视频的player对象
		//initPlayer();
		mHandler.sendEmptyMessageDelayed(START_PLAYER_CMD,2*1000);
		Log.i(TAG,"surfaceCreated");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"surfaceDestroyed");
	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
		if(mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume");
		if(mMediaPlayer!=null ) {
			mMediaPlayer.start();
		}

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onKeyDown keyCode = " + keyCode);
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			//screenScale(1);
			if(isScaleMode()) {
				scaleDisplay(1);
			}else{
				scaleDisplay(5);
			}
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			//screenScale(0);
			if(isScaleMode()) {
				scaleDisplay(0);
			}else{
				scaleDisplay(6);
			}
		}if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
		    //screenScale(2);
			scaleDisplay(2);
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
		    //screenScale(3);
			scaleDisplay(3);
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER ) {
			if(isScaleMode()){
				setScaleMode(false);
			}else {
				showSetDistanceDialog(MediaPlayerActivity.this);
			}
        }else if(keyCode == KeyEvent.KEYCODE_MENU) {
			startSysSetting(MediaPlayerActivity.this);
		}else if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH){
			return true;
		}else if(keyCode == 138) { //对焦键按下，对屏幕缩放
			setScaleMode(true);
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setScaleMode(boolean enable){
		scaleMode = enable;
	}
	private boolean isScaleMode(){
		return scaleMode;
	}

	private void scaleDisplay(int direction){
		Display display = getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
		Window window = getWindow();


		Log.i(TAG,"display width = " + display.getWidth() + "display height = " + display.getHeight());

		WindowManager.LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值
		//windowLayoutParams.width = (int) (display.getWidth() * 0.7); // 宽度设置为屏幕的0.95
		//windowLayoutParams.height = (int) (display.getHeight() * 0.1); // 高度设置为屏幕的0.6

		int width = windowLayoutParams.width;
		int height = windowLayoutParams.height;
		if(width <=0 ) width = display.getWidth();
		if(height <=0) height = display.getHeight();
		//if(height <=0) height = (display.getWidth()*4)/10;

		if(direction == 1) { /*缩小显示比例*/
			int min_width = display.getWidth()/2;
			int min_height = (display.getWidth()*4/11)/2;

			//int min_width = 1800/2;
			//int min_height = 720/2;

			//int min_height = display.getHeight()/2;
			windowLayoutParams.x = 0;
			windowLayoutParams.y = 0;
			windowLayoutParams.width = (int) (width * 0.99); // 宽度设置为屏幕的0.95
			//windowLayoutParams.height = (int) (height * 0.99); // 高度设置为屏幕的0.6
			windowLayoutParams.height = (int) ((windowLayoutParams.width * 4)/11);
			if(windowLayoutParams.width < min_width) windowLayoutParams.width = min_width;
			if(windowLayoutParams.height < min_height) windowLayoutParams.height = min_height;
		}else if(direction ==0) {/*放大显示比例*/
			int max_width = display.getWidth();
			int max_height = (display.getWidth()*4)/11;

			//int max_height = display.getHeight();
			windowLayoutParams.x = 0;
			windowLayoutParams.y = 0;
			windowLayoutParams.width = (int) (width * 1.01); // 宽度设置为屏幕的0.95
			//windowLayoutParams.height = (int) (height * 1.01); // 高度设置为屏幕的0.6
			windowLayoutParams.height = (int) ((windowLayoutParams.width * 4)/11); // 高度设置为屏幕的0.6
			if(windowLayoutParams.width > max_width) windowLayoutParams.width = max_width;
			if(windowLayoutParams.height > max_height) windowLayoutParams.height = max_height;
		}else if(direction ==2) { /*垂直向上移动*/
			windowLayoutParams.y = windowLayoutParams.y+2;
		}else if(direction == 3) {/*垂直向下移动*/
			windowLayoutParams.y = windowLayoutParams.y-2;
		}else if(direction == 5) {/*水平向左移动*/
			windowLayoutParams.x = windowLayoutParams.x-2;
		}else if(direction == 6) {/*水平向右移动*/
			windowLayoutParams.x = windowLayoutParams.x+2;
		}
		Log.i(TAG,"widow width = " + windowLayoutParams.width + "widow height = " + windowLayoutParams.height + "x :" + windowLayoutParams.x + "y :" + windowLayoutParams.y);
		window.setAttributes(windowLayoutParams);

		//surface.setLayoutParams(new LinearLayout.LayoutParams(windowLayoutParams.width, windowLayoutParams.height));

		//surface.getHolder().setFixedSize((int)windowLayoutParams.width,windowLayoutParams.height);
		surface.setMeasure(windowLayoutParams.width,windowLayoutParams.height);
		surface.requestLayout();


        DevRing.cacheManager().spCache("screenScale").put("width",windowLayoutParams.width);
        DevRing.cacheManager().spCache("screenScale").put("height",windowLayoutParams.height);
        DevRing.cacheManager().spCache("screenScale").put("x",windowLayoutParams.x);
        DevRing.cacheManager().spCache("screenScale").put("y",windowLayoutParams.y);
	}

	private void setDisplay() {
        int width = DevRing.cacheManager().spCache("screenScale").getInt("width",0);
        int height = DevRing.cacheManager().spCache("screenScale").getInt("height",0);
        int x = DevRing.cacheManager().spCache("screenScale").getInt("x",0);
        int y = DevRing.cacheManager().spCache("screenScale").getInt("y",0);
		Window window = getWindow();
		WindowManager.LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值

        if(width!=0 && height !=0) {
            windowLayoutParams.x = x;
            windowLayoutParams.y = y;
            windowLayoutParams.width = width;
            windowLayoutParams.height = height;

        }
        else {
			Display display = getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
			width = display.getWidth();
			height = (display.getWidth()*4)/11;
			//height = display.getHeight();
			windowLayoutParams.x = 0;
			windowLayoutParams.y = 0;
			windowLayoutParams.width = width;
			windowLayoutParams.height = height;
		}
		Log.i(TAG,"set display width = " + windowLayoutParams.width + " height = " + windowLayoutParams.height + "x=" + windowLayoutParams.x + "y=" + windowLayoutParams.y);
		window.setAttributes(windowLayoutParams);



		//surface.setLayoutParams(new LinearLayout.LayoutParams(windowLayoutParams.width, windowLayoutParams.height));

		//surface.getHolder().setFixedSize((int)windowLayoutParams.width,windowLayoutParams.height);
		surface.setMeasure(windowLayoutParams.width,windowLayoutParams.height);
		surface.requestLayout();

        //DevRing.cacheManager().diskCache("advertList").put("playList",adurls.toArray());
    }


	private void initEventBus() {
		EventBus.getDefault().register(this);
        //DevRing.busManager().register(MediaPlayerActivity.class);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
		releaseWakeLock();
		if(mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		EventBus.getDefault().unregister(this);
		if(serialPortUtils!=null) serialPortUtils.closeSerialPort();
	}

	private void initTFMini() {
		initserialPort();
		threshold_distance = DevRing.cacheManager().spCache("TFMini").getInt("threshold_distance",0);
		/*
		if(threshold_distance == 0 ) {
			showSetDistanceDialog(MediaPlayerActivity.this);
		}
		*/
	}

	private void startSysSetting(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName cn = new ComponentName("com.projector.settings", "com.txbox.settings.launcher.systemsettings.SystemSettingsMain");
		intent.setComponent(cn);
		context.startActivity(intent);
	}

	private void showSetDistanceDialog(Context context) {
		if((distanceSetDialog==null||!distanceSetDialog.isShowing())){
			View view = View.inflate(getApplicationContext(), R.layout.dialog_layout, null);
			distanceSetDialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.bt_distance_title_string)).
						setView(view).setPositiveButton
						(context.getResources().getString(R.string.bt_cancel_string),new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										// TODO Auto-generated method stub
										distanceSetDialog.dismiss();
										//distanceSetDialog = null;
									}
								}
						).setNegativeButton(context.getResources().
						getString(R.string.bt_setting_string), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						threshold_distance = distance;
						DevRing.cacheManager().spCache("TFMini").put("threshold_distance",threshold_distance);
					}
				}).create();
			distanceSetDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			distanceSetDialog.show();
			messageTV  = (TextView) distanceSetDialog.findViewById(R.id.distance_dialog);
			String message = String.format(getResources().getString(R.string.distmessage), strength, distance);
			if(messageTV!=null ) messageTV.setText(message);
		}
	}

	private void initserialPort()
	{
		serialPortUtils = new SerialPortUtils();
		serialPortUtils.openSerialPort();
		//串口数据监听事件
		serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
			@Override
			public void onDataReceive(byte[] buffer, int size) {
				//Log.i(TAG, "进入数据监听事件中。。。" + new String(buffer));
				dealWithData(buffer,size);

				//handler.post(ReadThread);
				if(lastdistance !=distance) {
					handler.post(runnable);
					lastdistance = distance;
				}
			}

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(!isPowerOff) {
						//Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance );
                        if (distanceSetDialog != null && distanceSetDialog.isShowing()) {
                            String message = String.format(getResources().getString(R.string.distmessage), strength, distance);
                            if(messageTV!=null ) messageTV.setText(message);
                        } else {
                            if (threshold_distance > 0 && (distance - threshold_distance > 10)) {
                                if (screenStatus == 1) {
									mHandler.removeMessages(SET_SCREEN_ON_CMD);
                                    Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance + "setscreen off");
                                    setScreen(0);
                                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                        mMediaPlayer.pause();
                                    }

                                }
                            } else {
                                if (screenStatus == 0) {
                                    Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance + "setscreen on");
									mHandler.sendEmptyMessageDelayed(SET_SCREEN_ON_CMD,1000);
									/*
                                    setScreen(1);
                                    if (mMediaPlayer != null)
                                        mMediaPlayer.start();
									*/
                                }
                            }
                        }
                    }
                }
            };


		});

		//new ReadThread().start();
	}
	private boolean threadStatus = false; //线程状态
	/**
	 * 单开一线程，来读数据
	 */
	private class ReadThread extends Thread{
		@Override
		public void run() {
			super.run();
			//判断进程是否在运行，更安全的结束进程
			while (!threadStatus){
				if(!isPowerOff) {
					if (distanceSetDialog != null && distanceSetDialog.isShowing()) {
						//handler.post(runnable);
					} else {
						if (threshold_distance > 0 && (distance - threshold_distance > 20)) {
							if (screenStatus == 1) {
								Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance + "setscreen off");
								setScreen(0);
								if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
									mMediaPlayer.pause();
								}

							}
						} else {
							if (screenStatus == 0) {
								Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance + "setscreen on");
								setScreen(1);
								if (mMediaPlayer != null)
									mMediaPlayer.start();

							}
						}
					}
				}
			}
			try {
				sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	private void dealWithData(byte[] buffer, int size)
	{
		if(size!=9) {
			//Log.i(TAG,"receiv data error ,size= "+ size + " is not 9 bytes");
			return ;
		}
		if(buffer[0] != 0x59 || buffer[1] !=0x59) {
			Log.i(TAG,"receiv data error ,head data0 = "+ buffer[0] +"data1 = "+buffer[1]);
			return ;
		}
		int low = buffer[2]&0xff;
		int high = buffer[3]&0xff;
		distance = low + high*256;
		low = buffer[4]&0xff;
		high = buffer[5]&0xff;
		strength = low + high*256;
		/*
		Log.i(TAG,"strength = " + strength + "dist is " + distance);
		for(int i=0;i<size;i++) {
			Log.i(TAG,""+ (buffer[i]&0xff));
		}
		*/
	}

	private void screenScale(int direction){
		/*
		LayoutParams lp = videoView.getLayoutParams();
		int height;
		int width;
		if(lp.height>0 && lp.width >0){
			height = lp.height;
			width  = lp.width;
		}else {
			height = videoView.getHeight();
			width = videoView.getWidth();
		}
		Log.i("TEST","playing video size height = " + height + "width = " + width);
		if(direction  == 1) {
			width = (int) (width * 0.9);
			//height = (int) (height * 0.9);
		}else if(direction == 0){
			width = (int) (width * 1.1);
			//height = (int) (height * 1.1);
		}else if(direction == 2) {
		    height = (int) (height * 0.9);
        }else if(direction == 3) {
		    height = (int) (height * 1.1);
        }
		//videoView.setVideoScale(width, height);
		DevRing.cacheManager().spCache("screenScale").put("width",width);
		DevRing.cacheManager().spCache("screenScale").put("height",height);
		*/
	}
	private void initVideoList() {
        String jsondata = DevRing.cacheManager().diskCache("advertList").getString("playList");
        Gson gson = new Gson();
        if (jsondata!=null) {
            adurls = gson.fromJson(jsondata, new TypeToken<List<PlayingAdvert>>() {}.getType());
        }else{
        	File path = new File("/system/media/advertList");
        	if(path.exists()) {
				File[] files = path.listFiles();// 读取文件夹下文件
				for (int i = 0; i < files.length; i++) {
					PlayingAdvert item = new PlayingAdvert();
					Log.i(TAG,"interal file = " + files[i].getAbsolutePath());
					item.setPath(files[i].getAbsolutePath());
					//item.setPath("http://update.thewaxseal.cn/videos/defaultvideo.mp4");
					adurls_local.add(item);
				}
			}
		}
	}

	private String  getValidUrl() {
		String url=null;
		if(adurls.size()>0) {
			int index = playindex % adurls.size();
			url = adurls.get(index).getPath();
		}else if(adurls_local.size()>0) {
			int index = playindex % adurls_local.size();
			url = adurls_local.get(index).getPath();
		}
		return url;
	}

	private void updateVideoList(String path) {
		//playindex=0;
		if(dateScheduleVos!=null && dateScheduleVos.size()>0) {
            List<AdvertVo> packageAdverts = dateScheduleVos.get(0).getTimeScheduleVos().get(0).getPackageAdverts();
            int size = packageAdverts.size();
            for (int i = 0; i < size; i++) {
                AdvertVo advertVo = packageAdverts.get(i);
                List<AdvertFile> fileList = advertVo.getFileList();
                for (int j = 0; j < fileList.size(); j++) {
                    AdvertFile advertFile = fileList.get(j);
                    if (path.contains(advertFile.getFileMd5())) {
                        PlayingAdvert item = new PlayingAdvert();
                        item.setPath(path);
                        item.setMd5(advertFile.getFileMd5());
                        adurls.add(item);
                    }
                }
            }

            Gson gson = new Gson();
            String str = gson.toJson(adurls);
            Log.i(TAG, "save advertlist = " + str);
            DevRing.cacheManager().diskCache("advertList").put("playList", str);
        }
	}

	private void saveAdvertVersion(AdvertPosition advertPosition){
		AdvertVersion.setAdVersion(advertPosition.getId().intValue(),advertPosition.getVersion());
	}

	private void setScreen(int enable){
        screenStatus = enable;
		prjmanager.setScreen(enable);
	}

	private void  test()
	{
		for(int i=0;i<=255;i++) {
			byte value = (byte)i;
			String teststring = String.format("0x1a 7 0 0xe0 0x%x 0x00 0x01 0x33 0x01 0x00", value);
			Log.i(TAG, teststring);
			Log.i(TAG,"i = " + value);
		}
	}

	private void initPowerOffAlarm(int hour,int minute,int second) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1=new Date(cal.getTimeInMillis());
		String t1=format.format(d1);
		Log.i(TAG,"Alarm POWER OFF time = " + t1);
		Intent intent=new Intent("POWER_OFF_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}


	private void initPowerOnAlarm(int hour,int minute,int second) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1=new Date(cal.getTimeInMillis());
		String t1=format.format(d1);
		Log.i(TAG,"Alarm POWER ON time = " + t1);
		Intent intent=new Intent("POWER_ON_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}

	private void reboot (){
		Intent intent = new Intent(Intent.ACTION_REBOOT);
		intent.putExtra("nowait", 1);
		intent.putExtra("interval", 1);
		intent.putExtra("window", 0);
		sendBroadcast(intent);
	}

    //接收事件总线发来的事件
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN) //如果使用默认的EventBus则使用此@Subscribe
    public void onAlarmEvent(AppEvent event) {
		int msg = event.getMessage();
		Log.i(TAG,"received event = " + msg);
		switch (msg) {
			case AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT:
				Log.i(TAG,"received event data = " + event.getData());
				updateVideoList((String)event.getData());
				break;
			case AppEvent.ADVERT_LIST_UPDATE_EVENT:
				dateScheduleVos = (List<DateScheduleVo>)event.getData();
				adurls.clear();
				playindex = 0;
				Log.i(TAG,"received event data size =  " + dateScheduleVos.size());
				break;
			case AppEvent.ADVERT_LIST_DOWNLOAD_FINISHED_EVENT:
				saveAdvertVersion((AdvertPosition) event.getData());
				break;
			case AppEvent.POWER_SET_ALARM_EVENT:
				if ("POWER_OFF_ALARM".equals(event.getData())) {
					handler.post(runableSetPowerOff);

				}else if("POWER_ON_ALARM".equals(event.getData())){
					reboot();
				}
				break;
			default:
				break;

		}

    }
}
