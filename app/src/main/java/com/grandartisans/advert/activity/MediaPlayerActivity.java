package com.grandartisans.advert.activity;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.SerialPortUtils;
import com.grandartisans.advert.view.VideoView;

import com.grandartisans.advert.R;
import com.ljy.devring.DevRing;
import com.droidlogic.app.SystemControlManager;

public class MediaPlayerActivity extends Activity {
	private final String TAG = "MediaPlayerActivity";
	VideoView videoView;
	private TextView messageTV ;
	private int playindex = 0;
	private List<String> adurls = new ArrayList<String>();
	/*
	{"/storage/udisk0/work/videos/58c0d0b04f872.mp4",
								"/storage/udisk0/work/videos/58c0d1055b43c.mp4",
								"/storage/udisk0/work/videos/58c0d14d425d5.mp4"};
	*/

	private Handler handler;
	private int distance = 0;
	private int strength = 0;
	public static final String I2C2_SLAVE_NODE = "/sys/class/i2c2/slave";
	private SystemControlManager mSystemControl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		handler = new Handler();
		mSystemControl = new SystemControlManager(this);
		Intent intentService = new Intent(MediaPlayerActivity.this,UpgradeService.class);
		startService(intentService);

		initserialPort();

		initVideoList();
		messageTV = findViewById(R.id.distmessage);
		videoView = (VideoView) findViewById(R.id.videoView);
		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playindex++;
                int index = playindex%adurls.size();
                videoView.setVideoURI(Uri.parse(adurls.get(index)));
            }
        });
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				int width = DevRing.cacheManager().spCache("screenScale").getInt("width",1920);
				int height = DevRing.cacheManager().spCache("screenScale").getInt("height",1280);
				videoView.setVideoScale(width, height);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		videoView.setVideoURI(Uri.parse(adurls.get(playindex)));
	}

	@Override
	protected void onPause() {
		super.onPause();
		videoView.stopPlayback();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			screenScale(1);
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			screenScale(0);
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initserialPort()
	{
		SerialPortUtils serialPortUtils = new SerialPortUtils();
		serialPortUtils.openSerialPort();
		//串口数据监听事件
		serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
			@Override
			public void onDataReceive(byte[] buffer, int size) {
				//Log.i(TAG, "进入数据监听事件中。。。" + new String(buffer));
				dealWithData(buffer,size);
				handler.post(runnable);
			}
			//开线程更新UI
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					String message = String.format(getResources().getString(R.string.distmessage), strength,distance);
					messageTV.setText(message);
				}
			};

		});
	}

	private void dealWithData(byte[] buffer, int size)
	{
		if(size!=9) {
			Log.i(TAG,"receiv data error ,size= "+ size + " is not 9 bytes");
			return ;
		}
		if(buffer[0] != 0x59 || buffer[1] !=0x59) {
			Log.i(TAG,"receiv data error ,head data0 = "+ buffer[0] +"data1 = "+buffer[1]);
			return ;
		}
		distance = buffer[2]&0xff + (buffer[3]&0xff)*256;
		strength = buffer[4]&0xff + (buffer[5]&0xff)*256;
		Log.i(TAG,"strength = " + strength + "dist is " + distance);
		for(int i=0;i<size;i++) {
			Log.i(TAG,""+ (buffer[i]&0xff));
		}
	}

	private void screenScale(int direction){
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
			height = (int) (height * 0.9);
		}else {
			width = (int) (width * 1.1);
			height = (int) (height * 1.1);
		}
		videoView.setVideoScale(width, height);
		DevRing.cacheManager().spCache("screenScale").put("width",width);
		DevRing.cacheManager().spCache("screenScale").put("height",height);
	}
	private void initVideoList() {
		String uri = "android.resource://" + getPackageName() + "/" +R.raw.defaultvideo;
		adurls.add(uri);
	}

	private void setScreen(int enable){
		if(enable ==1) {
			mSystemControl.writeSysFs(I2C2_SLAVE_NODE, "0x1a 2 0 0x90 0x00");
		}else if(enable == 0) {
			mSystemControl.writeSysFs(I2C2_SLAVE_NODE, "0x1a 2 0 0x90 0x07");
		}
	}
}
