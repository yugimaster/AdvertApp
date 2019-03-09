package com.grandartisans.advert.activity;

import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.grandartisans.advert.R;

import java.io.File;
import java.util.Calendar;

public class RecorderActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = "RecorderActivity";
    private SurfaceView mSurfaceview;
    private SurfaceView mSurfaceview_r;
    private Button mBtnStartStop;
    private Button mBtnPlay;
    private boolean mStartedFlg = false;//是否正在录像
    private boolean mIsPlay = false;//是否正在播放录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder mSurfaceHolder_r;
    private ImageView mImageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private TextView textView;
    private int text = 0;

    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            textView.setText(text+"");
            handler.postDelayed(this,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder);

        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceview_r = (SurfaceView) findViewById(R.id.surfaceviewr);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        mBtnPlay = (Button) findViewById(R.id.btnPlayVideo);
        textView = (TextView)findViewById(R.id.text);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (mIsPlay) {
                    if (mediaPlayer != null) {
                        mIsPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                */
                if (!mStartedFlg) {
                    handler.postDelayed(runnable,1000);
                    mImageView.setVisibility(View.GONE);
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                    }

                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (camera != null) {
                        camera.setDisplayOrientation(90);
                        camera.unlock();
                        mRecorder.setCamera(camera);
                    }

                    try {
                        // 这两项需要放在setOutputFormat之前
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                        //CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
                        CamcorderProfile mProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
                        Log.i(TAG,"recorder profile:" + mProfile.toString());
                        //mProfile.videoFrameWidth=640;
                        //mProfile.videoFrameHeight = 480;
                        mRecorder.setProfile(mProfile);
                                               // Set output file format
                                               //mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                        //                        // 这两项需要放在setOutputFormat之后
                        //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        //mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

                        //mRecorder.setVideoSize(640, 480);
                        //mRecorder.setVideoFrameRate(5);
                        mRecorder.setVideoEncodingBitRate(800* 1024);
                        //mRecorder.setOrientationHint(90);
                        //设置记录会话的最大持续时间（毫秒）
                        //mRecorder.setMaxDuration(30 * 1000);
                        mRecorder.setPreviewDisplay(mSurfaceHolder_r.getSurface());

                        path = getSDPath();
                        Log.i(TAG,"recorder path = " + path);
                        if (path != null) {
                            File dir = new File(path + "/recordtest");
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            path = dir + "/" + getDate() + ".mp4";
                            Log.i(TAG,"recorder path = " + path);
                            mRecorder.setOutputFile(path);
                            mRecorder.prepare();
                            mRecorder.start();
                            mStartedFlg = true;
                            mBtnStartStop.setText("Stop");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //stop
                    if (mStartedFlg) {
                        try {
                            handler.removeCallbacks(runnable);
                            mRecorder.stop();
                            mRecorder.reset();
                            mRecorder.release();
                            mRecorder = null;
                            mBtnStartStop.setText("Start");
                            if (camera != null) {
                                camera.release();
                                camera = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mStartedFlg = false;
                }
            }
        });

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mpath = getSDPath();
                Log.i(TAG,"player path = " + mpath);
                if (mpath != null) {
                    File dir = new File(mpath + "/recordtest");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    mpath = dir + "/"  + "20193645631.mp4";
                }

                mIsPlay = true;
                mImageView.setVisibility(View.GONE);
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                mediaPlayer.reset();

                Uri uri = Uri.parse(mpath);
                mediaPlayer = MediaPlayer.create(RecorderActivity.this, uri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDisplay(mSurfaceHolder);
                try{
                    mediaPlayer.prepare();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });

        mSurfaceHolder = mSurfaceview.getHolder();
        mSurfaceHolder_r = mSurfaceview_r.getHolder();
        mSurfaceHolder.addCallback(this);
        // setType必须设置，要不出错.
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mStartedFlg) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        String date = "" + year + (month + 1) + day + hour + minute + second;
        Log.d(TAG, "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
