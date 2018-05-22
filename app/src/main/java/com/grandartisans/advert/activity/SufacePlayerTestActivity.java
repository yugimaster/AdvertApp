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

public class SufacePlayerTestActivity extends Activity implements SurfaceHolder.Callback{
    private MediaPlayer firstPlayer,nextMediaPlayer;
    private SurfaceView surface;
    private SurfaceHolder surfaceHolder;

    private String adurls[]={"/sdcard/Android/data/com.grandartisans.advert/cache/cfe3f21455ef4319b56758b1390e4a7a.mp4",
            "/sdcard/Android/data/com.grandartisans.advert/cache/cfe3f21455ef4319b56758b1390e4a7a.mp4"
            /*"/storage/udisk1/work/videos/58c0d14d425d5.mp4"*/};
    private int playindex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_player);
        initView();
    }
    private void initView() {
        surface = (SurfaceView) findViewById(R.id.surface);

        surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

    private void initFirstPlayer() {
        firstPlayer = new MediaPlayer();
        firstPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        firstPlayer.setDisplay(surfaceHolder);

        firstPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onPlayCompleted(mp,nextMediaPlayer);
                    }
                });
        firstPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override public void onPrepared(MediaPlayer arg0)
            {
                if(nextMediaPlayer ==null || nextMediaPlayer.isPlaying() == false) {
                    firstPlayer.start();
                } else {
                    //firstPlayer.start();
                    //firstPlayer.pause();
                }
            }
        });

        initNexttPlayer();

        //player对象初始化完成后，开启播放
        startPlayVideo(firstPlayer,adurls[playindex]);



        playindex++;
        int index = playindex%adurls.length;
        startPlayVideo(nextMediaPlayer,adurls[index]);
    }

    private void initNexttPlayer() {
        nextMediaPlayer = new MediaPlayer();
        nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        nextMediaPlayer.setDisplay(null);

        nextMediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onPlayCompleted(mp,firstPlayer);
                    }
                });
        nextMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override public void onPrepared(MediaPlayer arg0)
            {
                //nextMediaPlayer.start();
                //nextMediaPlayer.pause();
            }
        });
    }

    private void onPlayCompleted(MediaPlayer curentmp,MediaPlayer nextmp) {
        curentmp.setDisplay(null);
        //get next player
        if (nextmp != null) {
            nextmp.setDisplay(surfaceHolder);
            nextmp.start();

            playindex++;
            int index = playindex%adurls.length;
            startPlayVideo(curentmp,adurls[index]);
        } else {
            //Toast.makeText(SurfacePlayerActivity.this, "视频播放完毕..", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlayVideo(MediaPlayer mp,String playurl) {
        try {
            //mp.stop();
            mp.reset();
            mp.setDataSource(playurl);
            mp.prepareAsync();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }
}
