package com.grandartisans.advert.model;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * author:  yugimaster
 * date:    2018/11/15
 * desc:    音量控制模块
 */

public class AudioMngModel {

    private final String TAG = "AudioMngModel";

    private AudioManager mAudioManager;
    private int NOW_AUDIO_TYPE = TYPE_SYSTEM;
    private int NOW_FLAG = FLAG_NOTHING;

    /**
     * 封装：STREAM 类型
     */
    public final static int TYPE_SYSTEM = AudioManager.STREAM_SYSTEM;
    public final static int TYPE_MUSIC = AudioManager.STREAM_MUSIC;
    public final static int TYPE_ALARM = AudioManager.STREAM_ALARM;
    public final static int TYPE_RING = AudioManager.STREAM_RING;
    @IntDef({TYPE_SYSTEM, TYPE_MUSIC, TYPE_ALARM, TYPE_RING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TYPE {}

    /**
     * 封装：FLAG 类型
     */
    public final static int FLAG_SHOW_UI = AudioManager.FLAG_SHOW_UI;
    public final static int FLAG_PLAY_SOUND = AudioManager.FLAG_PLAY_SOUND;
    public final static int FLAG_NOTHING = 0;
    @IntDef({FLAG_SHOW_UI, FLAG_PLAY_SOUND, FLAG_NOTHING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FLAG {}

    /**
     * 初始化，获取音量管理者
     * @param context   上下文
     */
    public AudioMngModel(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public int getSystemMaxVolume() {
        return mAudioManager.getStreamMaxVolume(NOW_AUDIO_TYPE);
    }

    public int getSystemCurrentVolume() {
        return mAudioManager.getStreamVolume(NOW_AUDIO_TYPE);
    }

    /**
     * 以0-100为范围，获取当前的音量值
     * @return  获取当前的音量值
     */
    public int getCurrentVolumeHundred() {
        return 100 * getSystemCurrentVolume() / getSystemMaxVolume();
    }

    /**
     * 改变当前的模式，对全局API生效
     * @param type
     * @return
     */
    public AudioMngModel setAudioType(@TYPE int type) {
        NOW_AUDIO_TYPE = type;
        return this;
    }

    public AudioMngModel setFlag(@FLAG int flag) {
        NOW_FLAG = flag;
        return this;
    }

    /**
     * 调整音量，自定义
     * @param num   0-100
     * @return  改完后的音量值
     */
    public int setVoiceHundred(int num) {
        int a = (int) Math.ceil((num) * getSystemMaxVolume() * 0.01);
        a = a <= 0 ? 0 : a;
        a = a >= 100 ? 100 : a;
        mAudioManager.setStreamVolume(NOW_AUDIO_TYPE, a, 0);
        return getCurrentVolumeHundred();
    }
}
