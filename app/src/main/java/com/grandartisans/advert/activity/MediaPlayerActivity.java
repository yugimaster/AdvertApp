package com.grandartisans.advert.activity;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
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
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.dbutils.PlayRecord;
import com.grandartisans.advert.dbutils.dbutils;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.DownloadInfo;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.PlayerStatusParameter;
import com.grandartisans.advert.model.entity.post.ReportEventData;
import com.grandartisans.advert.model.entity.post.ReportSchedueVerParameter;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPosition;
import com.grandartisans.advert.model.entity.res.AdvertPositionVo;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.model.entity.res.PowerOnOffData;
import com.grandartisans.advert.model.entity.res.ReportInfoResult;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TerminalAdvertPackageVo;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.grandartisans.advert.service.CameraService;
import com.grandartisans.advert.service.NetworkService;
import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.AdvertVersion;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.FileUtils;
import com.grandartisans.advert.utils.LogToFile;
import com.grandartisans.advert.utils.NetUtil;
import com.grandartisans.advert.utils.SerialPortUtils;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.grandartisans.advert.utils.Utils;
import com.grandartisans.advert.view.MySurfaceView;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;
import com.prj.utils.PrjSettingsManager;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import gartisans.hardware.pico.PicoClient;

public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback,SensorEventListener,SrsEncodeHandler.SrsEncodeListener {
	private final String TAG = "MediaPlayerActivity";
	private MediaPlayer mMediaPlayer;
	private MySurfaceView surface;
	private SurfaceHolder surfaceHolder;
	private boolean surfaceDestroyedFlag = true;

	private int surfaceDestroyedCount = 0;
	private int mediaplayerDestroyedCount = 0;
	private int menuKeyPressedCount = 0;

	private TextView messageTV ;
	private int playindex = 0;
	private List<PlayingAdvert> adurls = new ArrayList<PlayingAdvert>();
	private List<PlayingAdvert> downloading_ads = new ArrayList<PlayingAdvert>();

	private List<PlayingAdvert> adurls_local = new ArrayList<PlayingAdvert>();

	//List<DateScheduleVo> dateScheduleVos;

	private TerminalAdvertPackageVo mTerminalAdvertPackageVo;
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

	private final  int threshold_temperature = 58;
	private  final int temperature_read_times = 200; //连续获取到温度超过指定值次数大于该值，则认为温度过高了
	private int temperature_read_count=0;

	private PicoClient pClient= null;

	private boolean  player_first_time = true;

	private final int SET_SCREEN_ON_CMD = 100010;
	private final int START_PLAYER_CMD = 100011;
	private final int START_REPORT_EVENT_CMD= 100012;
	private final int START_REPORT_SCHEDULEVER_CMD = 100013;
	private final int ON_PAUSE_EVENT_CMD = 100014;
	private final int SET_POWER_ALARM_CMD = 100015;
	private final int START_OPEN_SERIALPORT = 100016;
	private final int START_PUSH_RTMP = 100017;
	private final int STOP_PUSH_RTMP = 100018;

	private final int START_REPORT_PLAYSTATUS_CMD= 100019;

	private final int START_SERVICE_CMD = 100020;

	private final int SET_LIFT_STOP_CMD = 100021;
	private final int START_FIRST_RECORD_CMD = 100022;

	private String mMode ="";

	static final float THRESHOLD = 0.2f; //0.08f;
	static final int LIFT_STATE_INIT = 0;
	static final int LIFT_STATE_STOP = 1;
	static final int LIFT_STATE_UP = 2;
	static final int LIFT_STATE_DOWN = 3;
	static final int LIFT_STATE_UP_WAITING_STOP = 4;
	static final int LIFT_STATE_DOWN_WAITING_STOP = 5;
	static final int LIFT_STATE_PRE_STOP =6;


	static final int DOOR_STATE_INIT  = 0;
	static final int DOOR_STATE_OPENED  = 1;
	static final int DOOR_STATE_CLOSED = 2;

	private SensorManager mSensorManager;
	private Sensor mAccSensor;
	public static SrsCameraView mCameraView;
	public static SrsPublisher mPublisher;
	private Timer mCheckTimer;
	private CameraService mCameraService;
	private NetworkService mNetworkService;
	private ServiceConnection mCamServiceConn;
	private ServiceConnection mNetServiceConn;
	private boolean AccSensorEnabled = false;
	private int mLiftState=0;
	private int mChanging=0;
	private int mUpChanging = 0;
	private int mDownChanging = 0;
	private float mInitZ = 0;
	private float mLastZ;

	private int mDoorState = DOOR_STATE_INIT;

	private int mReportEventTimeInterval=5*60*1000;
	private int mIntentId = 0;

	private boolean  activate_started = false;
	private boolean IsCameraServiceOn = false;
	private boolean IsNetworkServiceOn = false;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message paramMessage)
		{
			switch (paramMessage.what)
			{
				case SET_SCREEN_ON_CMD:
					setScreenOn();
					//mHandler.sendEmptyMessageDelayed(SET_LIFT_STOP_CMD,1000*60*1);
					break;
				case START_PLAYER_CMD:
					initPlayer();
					break;
				case START_OPEN_SERIALPORT:
					openSerialPort();
					break;
				case START_REPORT_EVENT_CMD:
					ReportPlayRecordAll();
					break;
				case START_REPORT_SCHEDULEVER_CMD:
					ReportScheduleVer();
					break;
				case ON_PAUSE_EVENT_CMD:
					onPauseEvent();
					break;
				case SET_POWER_ALARM_CMD:
					SetPowerAlarm();
					break;
				case START_PUSH_RTMP:
                    startPushRtmp();
					break;
				case STOP_PUSH_RTMP:
                    stopPushRtmp();
				case START_REPORT_PLAYSTATUS_CMD:
					ReportPlayStatus();
					break;
				case START_SERVICE_CMD:
					initService();
					break;
				case SET_LIFT_STOP_CMD:
					setScreenOff();
					setLiftState(LIFT_STATE_STOP);
				case START_FIRST_RECORD_CMD:
					startFirstRecord();
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
	private void SetPowerAlarm(){
			//initPowerOffAlarm(22,00,00);// 设置定时关机提醒
		long startTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("startTime",0);
		long endTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("endTime",0);
		int startHour = 07;
		int startMinute = 00;
		int startSecond = 00;
		int endHour = 21;
		int endMinute = 30;
		int endSecond = 00;
		if(startTime!=0) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String t1 = format.format(startTime);
			startHour = Integer.valueOf(t1.substring(0, 2));
			startMinute = Integer.valueOf(t1.substring(3, 5));
			startSecond = Integer.valueOf(t1.substring(6, 8));
		}
		if(endTime!=0){
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String t1 = format.format(endTime);
			endHour = Integer.valueOf(t1.substring(0, 2));
			endMinute = Integer.valueOf(t1.substring(3, 5));
			endSecond = Integer.valueOf(t1.substring(6, 8));
		}

		initPowerOffAlarm(endHour,endMinute,endSecond);// 设置定时关机提醒

		initPowerOnAlarm(startHour,startMinute,startSecond);//设置定时开机提醒

	}

	Runnable runableSetPowerOff = new Runnable() {
		@Override
		public void run() {
            lock.lock();
            if(isPowerOff==false) {
				isPowerOff = true;
				setScreenOff();
				mHandler.sendEmptyMessageDelayed(START_REPORT_EVENT_CMD,mReportEventTimeInterval);
			}
            lock.unlock();

			//initPowerOnAlarm(13,10,00);
		}
	};

	Runnable runableSetPowerOn = new Runnable() {
		@Override
		public void run() {
			lock.lock();
			if(isPowerOff==true) {
				isPowerOff = false;
				setScreen(1);
				onVideoPlayCompleted();
			}
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
		setCurrentTime();
		keepScreenWake();



		handler = new Handler();
		mHandler.sendEmptyMessageDelayed(SET_POWER_ALARM_CMD,1000*60*10);
		prjmanager = PrjSettingsManager.getInstance(this);

		mMode = CommonUtil.getModel();

		if(mMode.equals("GAPADS4A1") || mMode.equals("GAPEDS4A3")){
			checkCamera();
		}

		initAccSensor();

		initEventBus();//注册事件接收

		initTFMini();//初始化激光测距模块

		initVideoList();


		if(mMode.equals("AOSP on p313")) {
			setDisplay();
		}


		/*
		Intent intentService = new Intent(MediaPlayerActivity.this,UpgradeService.class);
		startService(intentService);
		*/


		PicoClient.OnEventListener mPicoOnEventListener = new PicoClient.OnEventListener() {
		    @Override
            public void onEvent(PicoClient client, int etype, Object einfo) {
		        //Log.d(TAG, "PicoClient onEvent" + "etype " + etype + " einfo " + (Float)einfo);

		        if((Float)einfo  > threshold_temperature ) {
					Log.d(TAG, "read temperature onEvent" + "etype " + etype + " einfo " + (Float)einfo +  "is to high" + "read count = " + temperature_read_count);
					LogToFile.i(TAG,"read temperature onEvent" + "etype " + etype + " einfo " + (Float)einfo +  "is to high "+"read count = " + temperature_read_count );
					temperature_read_count ++ ;
				}else {
					temperature_read_count = 0;
				}

				if(temperature_read_count > temperature_read_times) {
					LogToFile.i(TAG, "temperature is too high , device will power off ");
				}

		    }
        };
		pClient = new PicoClient(mPicoOnEventListener, null);

		//mHandler.sendEmptyMessage(START_REPORT_PLAYSTATUS_CMD);
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
						onVideoPlayCompleted();
					}
				});
		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
			@Override public void onPrepared(MediaPlayer mp) {

                Log.i(TAG, "video width = " + mMediaPlayer.getVideoWidth() + "video height = " + mMediaPlayer.getVideoHeight() + "screenStatus = " + getScreenStatus());
                //mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                mMediaPlayer.start();
                if (IsCameraServiceOn && !mCameraService.getRecordStatus()) {
                    RingLog.d(TAG, "Player is resumed, now resume record");
                    mPublisher.resumeRecord();
                }
                if (getScreenStatus() == 0){
                    mMediaPlayer.pause();
			    }
			    if(mMode.equals("AOSP on p313")) {
					if (player_first_time == true) {
						player_first_time = false;
						mMediaPlayer.seekTo(mMediaPlayer.getDuration());
					}
				}
			}
		});

		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener(){
		    @Override public boolean onError(MediaPlayer mp,int what, int extra)
            {
                Log.d(TAG, "OnError - Error code: " + what + " Extra code: " + extra);
				onVideoPlayCompleted();
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
			Log.d(TAG, "start play: url = " + url );
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
	private void onVideoPlayCompleted() {
		//get next player
		Log.i(TAG,"onVideoPlayCompleted format  isPowerOff =  " + isPowerOff);
		savePlayRecord();
		if(!isPowerOff) {
			mMediaPlayer.reset();
			//mMediaPlayer.stop();
			playindex++;
			String url = getValidUrl();
			Log.i(TAG,"onVideoPlayCompleted validurl  =  " + url);
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
		surfaceDestroyedFlag = false;
		CommonUtil.setScreenVideoMode("1");
		String status = DevRing.cacheManager().spCache("PowerStatus").getString("status","on");
		if(status.equals("off")){
			handler.post(runableSetPowerOff);
		}
        onResumeEvent();
		if(activate_started == false) {
			mHandler.sendEmptyMessageDelayed(START_PLAYER_CMD, 3 * 1000);
			if(mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
					mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")) {
				mHandler.sendEmptyMessageDelayed(START_OPEN_SERIALPORT, 5 * 1000);
			}else{
				openSerialPort();
			}
			activate_started =true;
		}
		else{
			mHandler.sendEmptyMessage(START_PLAYER_CMD);
			openSerialPort();
		}
		Log.i(TAG,"surfaceCreated");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"surfaceDestroyed");
		surfaceDestroyedFlag = true;
        onPauseEvent();
	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop");
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");

		//mHandler.sendEmptyMessageDelayed(ON_PAUSE_EVENT_CMD,1000);
		CameraService.cameraNeedStop = true;
		stopPushRtmp();
	}
	private void onPauseEvent(){
		Log.i(TAG,"onPauseEvent");
		mHandler.removeMessages(START_PLAYER_CMD);

		if(mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
				mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")) {
			mHandler.removeMessages(START_OPEN_SERIALPORT);
		}

		if (serialPortUtils != null) {
			serialPortUtils.closeSerialPort();
		}
		if(mMediaPlayer!=null) {
			Log.i(TAG,"Stop mMediaPlayer");
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		surface = null;
		surfaceHolder = null;
		if (IsNetworkServiceOn) {
			unbindService(mNetServiceConn);
			IsNetworkServiceOn = false;
		}
		CameraService.cameraNeedStop = true;
		stopPushRtmp();
		/*
		if(AccSensorEnabled) {
			mSensorManager.unregisterListener(this);
		}
		*/
		setScreen(1);
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume");
		initView();
		if (IsCameraServiceOn) {
			mCameraService.restartCameraRecord();
		}
	}
	private void openSerialPort(){
		if (serialPortUtils != null) {
			if (serialPortUtils.openSerialPort("/dev/" + CommonUtil.getTFMiniDevice()) == null){

			}
		}
	}
	private void onResumeEvent(){

        if(!mMode.equals("AOSP on p313")) {
            threshold_distance = Integer.valueOf(prjmanager.getDistance());
        }
		if(AccSensorEnabled) {
			mInitZ = Float.valueOf(prjmanager.getGsensorDefault());
		}

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onKeyDown keyCode = " + keyCode);
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if(mMode.equals("AOSP on p313")) {
				if (isScaleMode()) {
					scaleDisplay(1);
				} else {
					scaleDisplay(5);
				}
			}
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if(mMode.equals("AOSP on p313")) {
				if (isScaleMode()) {
					scaleDisplay(0);
				} else {
					scaleDisplay(6);
				}
			}
		}if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if(mMode.equals("AOSP on p313")) {
				scaleDisplay(2);
			}
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
			if(mMode.equals("AOSP on p313")) {
				scaleDisplay(3);
			}
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER ) {
			if(mMode.equals("AOSP on p313")) {
				if (isScaleMode()) {
					setScaleMode(false);
				} else {
					showSetDistanceDialog(MediaPlayerActivity.this);
				}
			}
        }else if(keyCode == KeyEvent.KEYCODE_MENU) {
			menuKeyPressedCount +=1;
			startSysSetting(MediaPlayerActivity.this);
		}else if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH){
			return true;
		}else if(keyCode == 138) { //对焦键按下，对屏幕缩放
			if(mMode.equals("AOSP on p313")) {
				if (isScaleMode() == true) {
					setScaleMode(false);
					surface.setBackground(null);
					mMediaPlayer.start();
					if (IsCameraServiceOn && !mCameraService.getRecordStatus()) {
						RingLog.d(TAG, "Player is resumed, now resume record");
						mPublisher.resumeRecord();
					}
				} else {
					setScaleMode(true);
					mMediaPlayer.pause();
					surface.setBackgroundColor(Color.BLUE);
				}
			}
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
		if (IsNetworkServiceOn) {
			unbindService(mNetServiceConn);
			IsNetworkServiceOn = false;
		}
		CameraService.cameraNeedStop = true;

		stopPushRtmp();
		if (IsCameraServiceOn) {
			unbindService(mCamServiceConn);
			IsCameraServiceOn = false;
		}
	}

	private void initTFMini() {
		initserialPort();
		if(mMode.equals("AOSP on p313")) {
			threshold_distance = DevRing.cacheManager().spCache("TFMini").getInt("threshold_distance",0);
		}else {
			threshold_distance = Integer.valueOf(prjmanager.getDistance());
		}

		/*
		if(threshold_distance == 0 ) {
			showSetDistanceDialog(MediaPlayerActivity.this);
		}
		*/
	}

	private void initAccSensor(){

        LogToFile.init(this);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(mMode.equals("GAPEDS4A2")||mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
				mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")){
			AccSensorEnabled = true;
			mInitZ =  Float.valueOf(prjmanager.getGsensorDefault());
		}
		else AccSensorEnabled = false;
		Log.i(TAG, "initAccSensor AccSensorEnabled = " + AccSensorEnabled  + " mInitZ = " + mInitZ);

		if(AccSensorEnabled) {
			mSensorManager.registerListener(this, mAccSensor, 200000);
			mInitZ = Float.valueOf(prjmanager.getGsensorDefault());
		}
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
		//串口数据监听事件
		serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
			@Override
			public void onDataReceive(byte[] buffer, int size) {
				//Log.i(TAG, "进入数据监听事件中。。。" + new String(buffer));
				if(CommonUtil.getTFMiniEnabled()==0) return;
				dealWithData(buffer,size);
				if(serialPortUtils.serialPortStatus) {
					//handler.post(ReadThread);
					if (lastdistance != distance) {
						handler.post(runnable);
						lastdistance = distance;
					}

					if (mLiftState != LIFT_STATE_STOP && mLiftState != LIFT_STATE_PRE_STOP && mLiftState != LIFT_STATE_INIT) {
						handler.post(runnable);
					}
				}
			}

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(!isPowerOff && serialPortUtils.serialPortStatus) {
						//Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance );
                        if (distanceSetDialog != null && distanceSetDialog.isShowing()) {
                            String message = String.format(getResources().getString(R.string.distmessage), strength, distance);
                            if(messageTV!=null ) messageTV.setText(message);
                        } else {
                            if (threshold_distance > 0 && (distance - threshold_distance > 10)) {
                        		//if(mDoorState!=DOOR_STATE_OPENED) mDoorState = DOOR_STATE_OPENED;
                                if (screenStatus == 1 || screenStatus ==2) {
									mHandler.removeMessages(SET_SCREEN_ON_CMD);
									setScreenOff();
                                }
                                if(mLiftState!=LIFT_STATE_INIT) {
									setLiftState(LIFT_STATE_STOP);
								}
                            } else {
                            	//if( screenStatus == 0 && mLiftState==LIFT_STATE_INIT) {
								if ((mLiftState == LIFT_STATE_INIT ||mLiftState== LIFT_STATE_UP ||mLiftState==LIFT_STATE_DOWN) && screenStatus == 0) {
                            		screenStatus = 2;
                            		Log.i(TAG, "threshold_distance= " + threshold_distance + "distance = " + distance + "setscreen on");
                            		//LogToFile.i(TAG,"threshold_distance= " + threshold_distance + "distance = " + distance + "setscreen on");
									mHandler.sendEmptyMessageDelayed(SET_SCREEN_ON_CMD, 1000);
                            	}
                            }
                        }
                    }
                }
            };


		});

	}


	private void dealWithData(byte[] buffer, int size)
	{
		if(size!=9) {
			Log.i(TAG,"receiv data error ,size= "+ size + " is not 9 bytes");
			distance = 0;
			return ;
		}
		if(buffer[0] != 0x59 || buffer[1] !=0x59) {
			Log.i(TAG,"receiv data error ,head data0 = "+ buffer[0] +"data1 = "+buffer[1]);
			distance = 0;
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
        }
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

	private void savePlayRecord() {
		if(adurls.size()>0) {
			int index = playindex % adurls.size();
			PlayingAdvert item = adurls.get(index);
			PlayRecord record = new PlayRecord();
			record.setTpid(item.getTemplateid());
			record.setApid(item.getAdPositionID());
			record.setAdid(item.getAdvertid());
			long currentTime = System.currentTimeMillis();
			record.setStarttime(currentTime);
			record.setEndtime(currentTime);
			record.setCount(1);
			dbutils.updatePlayCount(record);

			List<PlayRecord> records = dbutils.selectById(PlayRecord.class,record.getTpid(),record.getApid(),record.getAdid());
			if(records!=null && records.size()>0) {
				PlayRecord recordItem = records.get(0);
				if(recordItem.getCount()>=30){
					ReportPlayRecord(recordItem);
				}
			}
		}
	}

	private void ReportPlayRecordAll() {
		List<PlayRecord> records = dbutils.getPlayRecordAll(PlayRecord.class);
		if(records!=null && records.size()>0) {
			for(int i=0;i<records.size();i++) {
				PlayRecord recordItem = records.get(i);
				if (recordItem.getCount() >0) {
					ReportPlayRecord(recordItem);
				}
			}
			mHandler.removeMessages(START_REPORT_EVENT_CMD);
			mHandler.sendEmptyMessageDelayed(START_REPORT_EVENT_CMD,mReportEventTimeInterval);
		}

	}

	private void ReportPlayStatus () {
		EventParameter parameter = new EventParameter();
		parameter.setSn(SystemInfoManager.readFromNandkey("usid").toUpperCase());
		parameter.setSessionid(CommonUtil.getRandomString(50));
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(UpgradeService.mToken);
		parameter.setApp(Utils.getAppPackageName(MediaPlayerActivity.this));
		parameter.setEvent("playStatus");
		parameter.setEventtype(0000);

		parameter.setMac(CommonUtil.getEthernetMac());

		PlayerStatusParameter eventData = new PlayerStatusParameter();
		if(mMediaPlayer!=null) {
			eventData.setPlayerHandler(true);
			eventData.setPlaying(mMediaPlayer.isPlaying());
		}
		else {
			eventData.setPlayerHandler(false);
			eventData.setPlaying(false);
		}
		eventData.setSurfaceDestroyedFlag(surfaceDestroyedFlag);
		eventData.setgSensorDefaultValue(mInitZ);
		eventData.setGtfminiDefaultValue(threshold_distance);
		eventData.setMediaplayerDestroyedCount(mediaplayerDestroyedCount);
		eventData.setSurfaceDestroyedCount(surfaceDestroyedCount);
		eventData.setMenukeyPressedCount(menuKeyPressedCount);
		parameter.setEventData(eventData);
		//parameter.setIp();
		parameter.setTimestamp(System.currentTimeMillis());
		AdvertModel mIModel = new AdvertModel();

		DevRing.httpManager().commonRequest(mIModel.reportEvent(parameter), new CommonObserver<ReportInfoResult>() {
			@Override
			public void onResult(ReportInfoResult result) {
				RingLog.d("reportPlayer  status ok = " + result.getStatus() );
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("reportPlayer error i = " + i + "msg = " + s );
			}
		},null);

		mHandler.removeMessages(START_REPORT_PLAYSTATUS_CMD);
		mHandler.sendEmptyMessageDelayed(START_REPORT_PLAYSTATUS_CMD,mReportEventTimeInterval);
	}

	private void ReportScheduleVer()
	{
		EventParameter parameter = new EventParameter();
		parameter.setSn(SystemInfoManager.readFromNandkey("usid").toUpperCase());
		parameter.setSessionid(CommonUtil.getRandomString(50));
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(UpgradeService.mToken);
		parameter.setApp(Utils.getAppPackageName(MediaPlayerActivity.this));
		parameter.setEvent("scheduleVer");
		parameter.setEventtype(4000);

		parameter.setMac(CommonUtil.getEthernetMac());

		if(adurls.size()>0) {
			int index = playindex % adurls.size();
			PlayingAdvert item = adurls.get(index);
			ReportSchedueVerParameter info = new ReportSchedueVerParameter();
			info.setTemplateid(item.getTemplateid());
			info.setAdPositionID(item.getAdPositionID());
			info.setVersion(AdvertVersion.getAdVersion(item.getAdPositionID()));

			parameter.setEventData(info);
			parameter.setTimestamp(System.currentTimeMillis());
			AdvertModel mIModel = new AdvertModel();

			DevRing.httpManager().commonRequest(mIModel.reportEvent(parameter), new CommonObserver<ReportInfoResult>() {
				@Override
				public void onResult(ReportInfoResult result) {
					RingLog.d("reportScheduleVersion  ok status = " + result.getStatus());
				}

				@Override
				public void onError(int i, String s) {
					RingLog.d("reportScheduleVersion error i = " + i + "msg = " + s);
					handler.removeMessages(START_REPORT_SCHEDULEVER_CMD);
					handler.sendEmptyMessageDelayed(START_REPORT_SCHEDULEVER_CMD,30*1000);
				}
			}, null);
		}
	}

	private void ReportPlayRecord(final PlayRecord record)
	{
		EventParameter parameter = new EventParameter();
		parameter.setSn(SystemInfoManager.readFromNandkey("usid").toUpperCase());
		parameter.setSessionid(CommonUtil.getRandomString(50));
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(UpgradeService.mToken);
		parameter.setApp(Utils.getAppPackageName(MediaPlayerActivity.this));
		parameter.setEvent("playRecord");
		parameter.setEventtype(4000);

		parameter.setMac(CommonUtil.getEthernetMac());

        ReportEventData eventData = new ReportEventData();
        eventData.setCount(record.getCount());
        eventData.setAdvertid(record.getAdid());
        eventData.setAdPositionID(record.getApid());
        eventData.setTemplateid(record.getTpid());
        eventData.setStartTime(record.getStarttime());
        eventData.setEndTime(record.getEndtime());
        parameter.setEventData(eventData);
		//parameter.setIp();
		parameter.setTimestamp(System.currentTimeMillis());
		AdvertModel mIModel = new AdvertModel();

		DevRing.httpManager().commonRequest(mIModel.reportEvent(parameter), new CommonObserver<ReportInfoResult>() {
			@Override
			public void onResult(ReportInfoResult result) {
				RingLog.d("reportEvent ok status = " + result.getStatus() );
				dbutils.deletePlayRecord(PlayRecord.class,record.getId());
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("reportEvent error i = " + i + "msg = " + s );
			}
		},null);
	}

	private String  getValidUrl() {
		String url=null;
		lock.lock();
		Log.i(TAG,"adurls size  = " + adurls.size() + "playindex = " + playindex);
		Log.i(TAG,"adurls_local size  = " + adurls_local.size() + "playindex = " + playindex);
		boolean urlvalid = false;
		if(adurls.size()>0) {
			url = findPlayUrl();
			if(url!=null && !url.isEmpty()) {
				urlvalid = true;
				int index = playindex % adurls.size();
				//url = adurls.get(index).getPath();
				AdvertApp.setPlayingAdvert(adurls.get(index));

				PlayRecord record = new PlayRecord();
			}else{
				urlvalid = false;
			}

		}
		if(urlvalid == false && adurls_local.size()>0) {
			int index = playindex % adurls_local.size();
			url = adurls_local.get(index).getPath();
			PlayingAdvert playingItem = new PlayingAdvert();
			Long id = Long.valueOf(0);
			playingItem.setAdPositionID(id);
			playingItem.setAdvertid(id);
			AdvertApp.setPlayingAdvert(playingItem);
		}
		lock.unlock();
		return url;
	}
	private String findPlayUrl(){
		String url="";
		int size = adurls.size();
		for(int i=0;i<size;i++) {
			int index = playindex % adurls.size();

			PlayingAdvert playAdvertItem  = adurls.get(index);
			Log.i(TAG,"play advertitem "+ playAdvertItem.getPath() + "playindex = " +  playindex + "index = " + index + "path = " + playAdvertItem.getPath());
			Log.i(TAG,"play advertitem   = " +  playAdvertItem.getStartDate() + " " + playAdvertItem.getStartTime()+playAdvertItem.getEndDate() + " " + playAdvertItem.getEndTime());
			if(playAdvertItem.getPath()!=null && !playAdvertItem.getPath().isEmpty()) {
				if(playAdvertItem.getStartDate()!=null && !playAdvertItem.getStartDate().isEmpty()) {
					if (CommonUtil.compareDateState(playAdvertItem.getStartDate() + " " + playAdvertItem.getStartTime(), playAdvertItem.getEndDate() + " " + playAdvertItem.getEndTime())) {
						url = playAdvertItem.getPath();
						break;
					} else if (CommonUtil.compareDateState("2015-01-01 00:00:00", "2016-12-30 23:59:59")) {
						url = playAdvertItem.getPath();
						break;
					} else {
						playindex++;
					}
				}else{
					url = playAdvertItem.getPath();
					break;
				}
			}else{
				playindex++;
			}
		}
		return url;
	}

	private void updatePlayListFilePath(String path){
		int size = downloading_ads.size();
		for(int i=0;i<size;i++){
			PlayingAdvert item = downloading_ads.get(i);
			if(path.contains(item.getMd5())){
				item.setPath(path);
			}
		}
	}

	private void updateVideoList() {
		//playindex=0;
		downloading_ads.clear();
		if(mTerminalAdvertPackageVo!=null ) {
			List<DateScheduleVo> dateScheduleVos=null;
			List<TemplateRegion> regionList  = mTerminalAdvertPackageVo.getTemplate().getRegionList();
			TemplateRegion region = regionList.get(0);
			Long advertPositionId = mTerminalAdvertPackageVo.getRelationMap().get(region.getIdent());
			AdvertPositionVo advertPositionVo = mTerminalAdvertPackageVo.getAdvertPositionMap().get(advertPositionId);
			if(advertPositionVo!=null) {
				dateScheduleVos = advertPositionVo.getDateScheduleVos();
				//mAdverPosition = advertPositionVo.getadvertPosition();

			}
			int dateScheduleSize = dateScheduleVos.size();
			for(int l=0;l<dateScheduleSize;l++) {
				DateScheduleVo dateScheduleVo = dateScheduleVos.get(l);
				List<TimeScheduleVo> timeScheduleVos = dateScheduleVo.getTimeScheduleVos();
				int timeScheduleSize = timeScheduleVos.size();
				for (int k = 0; k < timeScheduleSize; k++) {
					TimeScheduleVo timeScheduleVo = timeScheduleVos.get(k);
					List<AdvertVo> packageAdverts = timeScheduleVo.getPackageAdverts();
					Long adPositionId = dateScheduleVo.getDateSchedule().getAdvertPositionId();
					int size = packageAdverts.size();
					for (int i = 0; i < size; i++) {
						AdvertVo advertVo = packageAdverts.get(i);
						List<AdvertFile> fileList = advertVo.getFileList();
						for (int j = 0; j < fileList.size(); j++) {
							AdvertFile advertFile = fileList.get(j);
								PlayingAdvert item = new PlayingAdvert();
								item.setPath("");
								item.setMd5(advertFile.getFileMd5());
								item.setAdvertid(advertFile.getAdvertid());
								item.setAdPositionID(adPositionId);
								item.setTemplateid(region.getTemplateid());
								item.setStartDate(dateScheduleVo.getDateSchedule().getStartDate());
								item.setEndDate(dateScheduleVo.getDateSchedule().getEndDate());
								item.setStartTime(timeScheduleVo.getTimeSchedule().getStartTime()+":00");
								item.setEndTime(timeScheduleVo.getTimeSchedule().getEndTime()+":00");
								downloading_ads.add(item);
						}
					}
				}
			}
        }
	}

	private void saveAdvertVersion(AdvertPosition advertPosition){
		adurls.clear();
		playindex = 0;
		for(int i=0;i<downloading_ads.size();i++){
			adurls.add(downloading_ads.get(i));
		}


		Gson gson = new Gson();
		String str = gson.toJson(adurls);
		Log.i(TAG, "save advertlist = " + str);
		DevRing.cacheManager().diskCache("advertList").put("playList", str);

		AdvertVersion.setAdVersion(advertPosition.getId().intValue(),advertPosition.getVersion());
		ReportSchedueVerParameter info = new ReportSchedueVerParameter();
		info.setAdPositionID(advertPosition.getId());
		info.setVersion(advertPosition.getVersion());
		info.setTemplateid(mTerminalAdvertPackageVo.getTemplate().getTemplate().getId());
		ReportScheduleVer();
	}

	private int getScreenStatus(){
	    return screenStatus;
    }

	private void setScreen(int enable){
        screenStatus = enable;
		Log.i(TAG,"setScreen enable :" + enable);
		prjmanager.setScreen(enable);
	}

	private void setScreenOff(){
		if (getScreenStatus() != 0) {
			setScreen(0);
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				if (IsCameraServiceOn && mCameraService.getRecordStatus()) {
					RingLog.d(TAG, "Player is paused, so pause record");
					mPublisher.pauseRecord();
				}
			}
		}
	}
	private void setScreenOn() {
		if(getScreenStatus()!=1 ) {
			setScreen(1);
			if (mMediaPlayer != null) {
				mMediaPlayer.start();
				if (IsCameraServiceOn && !mCameraService.getRecordStatus()) {
					RingLog.d(TAG, "Player is resumed, now resume record");
					mPublisher.resumeRecord();
				}
			}
			if(surfaceDestroyedFlag) {
				surfaceDestroyedCount +=1;
				initView();
				initPlayer();
			}
			if(mMediaPlayer==null || !mMediaPlayer.isPlaying()){
				Log.i(TAG,"Player is error ,reset player");
				mediaplayerDestroyedCount +=1;
				initView();
				initPlayer();
			}
		}
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

	private void ResetPowerOnAlarm(long time){
		/*
		Intent intent=new Intent("POWER_ON_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am.cancel(pi);
		*/

		SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
		String t1=format.format(time);

		int hour = Integer.valueOf(t1.substring(0,2));
		int minute = Integer.valueOf(t1.substring(3,5));
		int second = Integer.valueOf(t1.substring(6,8));

		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		Log.i(TAG,"Alarm POWER ON time = " + t1 + "hour = " + hour + "minute = " + minute + "second = " + second  + "date = " + date.toString());
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);



		Intent intent1=new Intent("POWER_ON_ALARM");
		PendingIntent pi1= PendingIntent.getBroadcast(this, 0, intent1,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am1=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am1.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1);
	}
	private void ResetPowerOffAlarm(long time){

		/*
		Intent intent=new Intent("POWER_OFF_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am.cancel(pi);
		*/


		SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
		String t1=format.format(time);

		int hour = Integer.valueOf(t1.substring(0,2));
		int minute = Integer.valueOf(t1.substring(3,5));
		int second = Integer.valueOf(t1.substring(6,8));
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		Log.i(TAG,"Alarm POWER OFF time = " + t1 + "hour = " + hour + "minute = " + minute + "second = " + second  + "date = " + date.toString());
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		Intent intent1=new Intent("POWER_OFF_ALARM");
		PendingIntent pi1= PendingIntent.getBroadcast(this, 0, intent1,0);


		//设置一个PendingIntent对象，发送广播
		AlarmManager am1=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am1.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1);

	}
	/*设置当前时间*/
	private void setCurrentTime(){
		if(CommonUtil.compareDateState("2015-01-01 00:00:00","2016-01-01 23:59:00")){
			long when = DevRing.cacheManager().spCache("SysTime").getLong("timeInMillis",0);
			if(when!=0){
					when +=  30*1000;
					if(when / 1000 < Integer.MAX_VALUE){
						((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setTime(when);
					}
			}
		}
	}
	/*保存当前时间*/
	private void saveCurrentTime(){
		DevRing.cacheManager().spCache("SysTime").put("timeInMillis",System.currentTimeMillis());
	}

    //接收事件总线发来的事件
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN) //如果使用默认的EventBus则使用此@Subscribe
    public void onAlarmEvent(AppEvent event) {
		int msg = event.getMessage();
		Log.i(TAG,"received event = " + msg);
		switch (msg) {
			case AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT:
				Log.i(TAG,"received event data = " + event.getData());
				//updateVideoList((String)event.getData());
				updatePlayListFilePath((String)event.getData());
				break;
			case AppEvent.ADVERT_LIST_UPDATE_EVENT:
				//Log.i(TAG,"received event data = " + event.getData());
				mTerminalAdvertPackageVo = (TerminalAdvertPackageVo) event.getData();
				//adurls.clear();
				//playindex = 0;
				updateVideoList();
				//Log.i(TAG,"received event data size =  " + dateScheduleVos.size());
				break;
			case AppEvent.ADVERT_LIST_DOWNLOAD_FINISHED_EVENT:
				saveAdvertVersion((AdvertPosition) event.getData());
				break;
			case AppEvent.POWER_SET_ALARM_EVENT:
				if ("POWER_OFF_ALARM".equals(event.getData())) {
					handler.post(runableSetPowerOff);

				}else if("POWER_ON_ALARM".equals(event.getData())){
					saveCurrentTime();
					CommonUtil.reboot(MediaPlayerActivity.this);
				}
				break;
			case AppEvent.SET_POWER_OFF:
				handler.post(runableSetPowerOff);
				DevRing.cacheManager().spCache("PowerStatus").put("status","off");
				break;
			case AppEvent.SET_POWER_ON:
				DevRing.cacheManager().spCache("PowerStatus").put("status","on");
				handler.post(runableSetPowerOn);
				break;
			case AppEvent.POWER_UPDATE_ALARM_EVENT:
				PowerOnOffData powerOnOffData = (PowerOnOffData)event.getData();
				mHandler.removeMessages(SET_POWER_ALARM_CMD);
				ResetPowerOffAlarm(powerOnOffData.getEndTime());
				ResetPowerOnAlarm(powerOnOffData.getStartTime());
			default:
				break;

		}

    }

	/* Filter positive direction*/
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Sensor eSensor= sensorEvent.sensor;

		if (eSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			//float acc = sensorEvent.values[0];
			//float acc = sensorEvent.values[1];
			float acc = sensorEvent.values[2];
			//Log.i(TAG, "time:" + sensorEvent.timestamp + "acc_z:" + acc  + "  " +"mLiftState=" + mLiftState);
			// " X Y Z: " + acc_x + " " + acc_y + " " + acc_z);
			//Log.i(TAG,"state: " + mLiftState  + "acc_z = " + acc);
            //LogToFile.i(TAG,"state: " + mLiftState  + "acc_z = " + acc);
			if(CommonUtil.getGsensorEnabled()==0){
				setLiftState(LIFT_STATE_INIT);
				return;
			}
			switch (mLiftState) {
				case LIFT_STATE_INIT:
					//setLiftState(LIFT_STATE_STOP);
					//mInitZ = acc;
					//break;

				case LIFT_STATE_STOP: {
					float deltaz = acc - mInitZ;
					if (Math.abs(deltaz) > THRESHOLD && deltaz >0) {
						mDownChanging = 0;
						if (++mUpChanging == 8) {
							mUpChanging = 0;
							setLiftState(LIFT_STATE_UP);
						}
					}else if(Math.abs(deltaz) > THRESHOLD && deltaz <0){
						mUpChanging = 0;
						if (++mDownChanging == 8) {
							mDownChanging = 0;
							setLiftState(LIFT_STATE_DOWN);
						}
					} else {
						if (mChanging != 0) mChanging = 0;
						mDownChanging = 0;
						mUpChanging = 0;
					}
					break;
				}

				case LIFT_STATE_DOWN: {
					//check decelerate
					float deltaz = acc - mInitZ;
					if (deltaz > THRESHOLD) {
						if (++mChanging == 8) {
							mChanging = 0;
							setLiftState(LIFT_STATE_PRE_STOP);
						}
					} else {
						if (mChanging != 0) mChanging = 0;
					}
					break;
				}

				case LIFT_STATE_PRE_STOP: {
					float deltaz = acc - mInitZ;
					if (Math.abs(deltaz) <= 0.08) {
						if (++mChanging == 4) {
							mChanging = 0;
							setLiftState(LIFT_STATE_STOP);
						}
					} else {
						if (mChanging != 0) mChanging = 0;
					}
					break;
				}
				case LIFT_STATE_UP_WAITING_STOP: {
					float deltaz = acc - mInitZ;
					if (acc > mLastZ ) {
						if (++mChanging == 4) {
							mChanging = 0;
							setLiftState(LIFT_STATE_PRE_STOP);
						}
					} else {
						if (mChanging != 0) mChanging = 0;
					}
					break;
				}

				case LIFT_STATE_DOWN_WAITING_STOP: {
					float deltaz = acc - mInitZ;
					if (acc < mLastZ) {
						if (++mChanging == 4) {
							mChanging = 0;
							setLiftState(LIFT_STATE_PRE_STOP);
						}
					} else {
						if (mChanging != 0) mChanging = 0;
					}
					break;
				}
				case LIFT_STATE_UP: {
					//check decelerate
					float deltaz = acc - mInitZ;
					if (deltaz < 0) {
						if (Math.abs(deltaz) > THRESHOLD) {
							if (++mChanging == 8) {
								mChanging = 0;
								setLiftState(LIFT_STATE_PRE_STOP);
							}
						} else {
							if (mChanging != 0) mChanging = 0;
						}
					}
					break;
				}
			}

			mLastZ = acc;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	@Override
	public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
		handleException(e);
	}

	private void handleException(Exception e) {
		try {
			RingLog.d(TAG, e.getMessage());
		} catch (Exception e1) {
			//
		}
	}

	@Override
	public void onNetworkWeak() {
		RingLog.d(TAG, "The network is weak");
	}

	@Override
	public void onNetworkResume() {}

	private void setLiftState(int liftState) {
		mLiftState = liftState;
		Log.i(TAG, "state: " + liftState);
        //LogToFile.i(TAG,"state: " + liftState);
		//mLiftStateTV.setText(liftStateString(mLiftState));
		switch (liftState) {
			case LIFT_STATE_INIT:
				break;
			case LIFT_STATE_STOP:
			    if(CommonUtil.isForeground(MediaPlayerActivity.this,"com.grandartisans.advert.activity.MediaPlayerActivity")) {
                    setScreenOff();
                }
				break;
			case LIFT_STATE_UP:
				//setScreenOn();
				break;
			case LIFT_STATE_DOWN:
				//setScreenOn();
				break;
			//case LIFT_STATE_DOWN_WAITING_STOP:
			//case LIFT_STATE_UP_WAITING_STOP:
			case LIFT_STATE_PRE_STOP:
                if(CommonUtil.isForeground(MediaPlayerActivity.this,"com.grandartisans.advert.activity.MediaPlayerActivity")) {
                    setScreenOff();
                }
				break;
			default:
				break;
		}
	}
	private void checkCamera() {
		// 初始化CameraView 并设置为不可见
		mCameraView = (SrsCameraView) findViewById(R.id.glsurfaceview_camera);
		mCameraView.setVisibility(View.GONE);
		startCheckCameraTimer();
	}

	private void startCheckCameraTimer() {
		mCheckTimer = new Timer();
		mCheckTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				int num = Camera.getNumberOfCameras();
				RingLog.d(TAG, "Camera numbers is " + num);
				if (num != 0) {
                    RingLog.d(TAG, "Camera is on");
                    mCheckTimer.cancel();
                    mHandler.sendEmptyMessageDelayed(START_SERVICE_CMD, 1000);
                } else {
                    RingLog.d(TAG, "None camera");
                }
			}
		}, 6000, 5 * 1000);
	}

	private void initCameraView() {
		// 初始化相机并打开
		mCameraView.setVisibility(View.VISIBLE);
		mPublisher = new SrsPublisher(mCameraView);
		// 编码状态回调
		mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
		// 预览分辨率
		mPublisher.setPreviewResolution(1280, 720);
		// 推流分辨率
		mPublisher.setOutputResolution(1280, 720);
		// 传输率
		mPublisher.setVideoHDMode();
		// 将摄像头预览最小化
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1, 1);
		layoutParams.setMargins(0, 0, 0, 0);
		mCameraView.setLayoutParams(layoutParams);
		// 调整摄像头角度
		mCameraView.setPreviewOrientation(0);
		mPublisher.startCamera();
	}

	private void startPushRtmp() {
		if (!mCameraService.isHasFirstRecord()) {
			// 第一次录像
			mCameraService.startCameraRecord();
		} else if (!mCameraService.getRecordFinished()) {
			// 重新录像
			mCameraService.restartCameraRecord();
		} else if(CommonUtil.getModel().equals("GAPEDS4A3")) {
			// 已录像完 进行推流
			mCameraService.startRtmp();
		}
	}

	private void stopPushRtmp() {
		if (mPublisher != null) {
			mPublisher.stopPublish();
			mPublisher.stopRecord();
		}
	}

	private void initService() {
		initCameraService();
		initNetworkService();
	}

	private void initCameraService() {
		initCamServiceConnection();
		Intent intent = new Intent(MediaPlayerActivity.this, CameraService.class);
		startService(intent);
		bindService(intent, mCamServiceConn, getApplicationContext().BIND_AUTO_CREATE);
		IsCameraServiceOn = true;
	}

	private void initCamServiceConnection() {
		mCamServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				CameraService.CamBinder binder = (CameraService.CamBinder) service;
				mCameraService = binder.getService();
				mHandler.sendEmptyMessage(START_FIRST_RECORD_CMD);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mCameraService = null;
			}
		};
	}

    private void initNetworkService() {
		initCameraView();
		initNetServiceConnection();
		Intent intent = new Intent(MediaPlayerActivity.this, NetworkService.class);
		startService(intent);
		bindService(intent, mNetServiceConn, getApplicationContext().BIND_AUTO_CREATE);
		IsNetworkServiceOn = true;
	}

    private void initNetServiceConnection() {
		mNetServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mNetworkService = ((NetworkService.MyBinder) service).getService();
				mNetworkService.setOnGetConnectState(new NetworkService.GetConnectState() {
					@Override
					public void GetState(int isConnected) {
						if (mIntentId != 0 && isConnected != 0) {
							// 当前还是处于有网状态，不做任何动作
							return;
						}
						if (mIntentId != isConnected) {
							// 如果当前连接状态与广播服务返回的状态不同才进行通知显示
							mIntentId = isConnected;
							RingLog.d(TAG, "Current network is " + mIntentId);

							if (mIntentId == 0) {
								// 未连接
								RingLog.d(TAG, "The network has disconnected");
								CameraService.cameraNeedStop = true;
								mHandler.sendEmptyMessage(STOP_PUSH_RTMP);
							} else if (mIntentId != 0) {
								// 已连接
								RingLog.d(TAG, "The network has connected");
								mHandler.sendEmptyMessage(START_PUSH_RTMP);
							}
						}
					}
				});
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {}
		};
	}

	private void startFirstRecord() {
		int networkStatus = NetUtil.getNetworkState(this);
		if (networkStatus != 0) {
			mCameraService.startCameraRecord();
		}
	}
}
