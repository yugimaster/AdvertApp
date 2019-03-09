package com.grandartisans.advert.utils;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class AdvertMonitorManager {
    private static final String TAG = "RecorderActivity";
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder mSurfaceHolder_r;
    private ImageView mImageView;
    private Camera camera;
    static final int RECORDER_STATE_STOPED  = 0;
    static final int RECORDER_STATE_STARTED  = 1;
    static final int RECORDER_STATE_PAUSED = 2;
    private int mRecorderStatus = RECORDER_STATE_STOPED;
    private String mRecorderPath ;
    public AdvertMonitorManager(int cameranum,String path){
        mRecorderPath = path;
        RecorderInit(cameranum);
    }
    private boolean RecorderInit(int cameranum) {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        camera = Camera.open(cameranum);
        if (camera != null) {
            //camera.setDisplayOrientation(90);
            camera.unlock();
            mRecorder.setCamera(camera);
        }
        try {
            // 这两项需要放在setOutputFormat之前
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            //CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
            CamcorderProfile mProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
            Log.i(TAG, "recorder profile:" + mProfile.toString());
            //mProfile.videoFrameWidth=640;
            //mProfile.videoFrameHeight = 480;
            mRecorder.setProfile(mProfile);
            // Set output file format
            //mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // 这两项需要放在setOutputFormat之后
            //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            //mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            //mRecorder.setVideoSize(640, 480);
            //mRecorder.setVideoFrameRate(5);
            mRecorder.setVideoEncodingBitRate(800 * 1024);
            //mRecorder.setOrientationHint(90);
            //设置记录会话的最大持续时间（毫秒）
            //mRecorder.setMaxDuration(30 * 1000);
            mRecorder.setPreviewDisplay(mSurfaceHolder_r.getSurface());
        }catch (Exception e) {
            e.printStackTrace();
        }
        mRecorderStatus = RECORDER_STATE_STOPED;
        return true;
    }
    public boolean RecorderStart(){
        if (mRecorderStatus == RECORDER_STATE_STOPED) {
            Log.i(TAG,"recorder path = " + mRecorderPath);
            if (mRecorderPath != null) {
                mRecorder.setOutputFile(mRecorderPath);
                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRecorderStatus = RECORDER_STATE_STARTED;
            }
        }
        return true;
    }
    public boolean RecorderStop(){
        if (mRecorderStatus != RECORDER_STATE_STOPED) {
            try {
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecorderStatus = RECORDER_STATE_STOPED;
        }
        return true;
    }
    public boolean RecorderPause(){
        if (mRecorderStatus == RECORDER_STATE_STARTED) {
            mRecorder.pause();
            mRecorder.resume();
            mRecorderStatus = RECORDER_STATE_PAUSED;
        }
        return true;
    }

    public boolean RecorderResume(){
        if (mRecorderStatus == RECORDER_STATE_PAUSED) {
            mRecorderStatus = RECORDER_STATE_STARTED;
        }
        return true;
    }
}
