package com.grandartisans.advert.activity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.grandartisans.advert.R;

import java.io.IOException;
import java.util.HashMap;

public class SurfacePlayerActivity extends Activity implements SurfaceHolder.Callback{
    private MediaPlayer firstPlayer,nextMediaPlayer,cachePlayer,currentPlayer;
    private SurfaceView surface;
    private SurfaceHolder surfaceHolder;

    private String adurls[]={"/storage/udisk0/work/videos/58c0d0b04f872.mp4",
            "/storage/udisk0/work/videos/58c0d1055b43c.mp4",
            "/storage/udisk0/work/videos/58c0d14d425d5.mp4"};
    private int playindex = 0;

    private HashMap<String, MediaPlayer> playersCache = new HashMap<String, MediaPlayer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_player);
        initView();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firstPlayer != null) {
            if (firstPlayer.isPlaying()) {
                firstPlayer.stop();
            }
            firstPlayer.release();
        }
        if (nextMediaPlayer != null) {
            if (nextMediaPlayer.isPlaying()) {
                nextMediaPlayer.stop();
            }
            nextMediaPlayer.release();
        }

        if (currentPlayer != null) {
            if (currentPlayer.isPlaying()) {
                currentPlayer.stop();
            }
            currentPlayer.release();
        }
        currentPlayer = null;
    }
    private void initView() {
        surface = (SurfaceView) findViewById(R.id.surface);

        surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO 自动生成的方法存根

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        //然后初始化播放手段视频的player对象
        initFirstPlayer();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO 自动生成的方法存根

    }

    /*
     * 初始化播放首段视频的player
     */
    private void initFirstPlayer() {
        firstPlayer = new MediaPlayer();
        firstPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        firstPlayer.setDisplay(surfaceHolder);

        firstPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onVideoPlayCompleted(mp);
                    }
                });

        //设置cachePlayer为该player对象
        cachePlayer = firstPlayer;


        //player对象初始化完成后，开启播放
        startPlayFirstVideo();

        initNexttPlayer();
    }

    private void startPlayFirstVideo() {
        try {
            firstPlayer.setDataSource(adurls[playindex]);
            firstPlayer.prepare();
            firstPlayer.start();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /*
     * 新开线程负责初始化负责播放剩余视频分段的player对象,避免UI线程做过多耗时操作
     */
    private void initNexttPlayer() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                for (int i = 1; i < adurls.length; i++) {
                    nextMediaPlayer = new MediaPlayer();
                    nextMediaPlayer
                            .setAudioStreamType(AudioManager.STREAM_MUSIC);

                    nextMediaPlayer
                            .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    onVideoPlayCompleted(mp);
                                }
                            });

                    try {
                        nextMediaPlayer.setDataSource(adurls[i]);
                        nextMediaPlayer.prepare();
                    } catch (IOException e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }

                    //set next mediaplayer
                    cachePlayer.setNextMediaPlayer(nextMediaPlayer);
                    //set new cachePlayer
                    cachePlayer = nextMediaPlayer;
                    //put nextMediaPlayer in cache
                    playersCache.put(String.valueOf(i), nextMediaPlayer);

                }

            }
        }).start();
    }

    /*
     * 负责处理一段视频播放过后，切换player播放下一段视频
     */
    private void onVideoPlayCompleted(MediaPlayer mp) {
        mp.setDisplay(null);
        //get next player
        currentPlayer = playersCache.get(String.valueOf(++playindex));
        if (currentPlayer != null) {
            currentPlayer.setDisplay(surfaceHolder);
        } else {
            Toast.makeText(SurfacePlayerActivity.this, "视频播放完毕..", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
