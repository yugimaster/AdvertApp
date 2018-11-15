package com.grandartisans.advert.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

/**
 * author:  yugimaster
 * date:    2018/11/15
 * desc:    亮度控制模块
 */

public class ScreenMngModel {

    private final String TAG = "ScreenMngModel";

    private ContentResolver mContentResolver;

    private final static int SYSTEM_MAX_BRIGHTNESS = 255;

    /**
     * 初始化，获取ContentProvider数据
     * @param context
     */
    public ScreenMngModel(Context context) {
        mContentResolver = context.getContentResolver();
    }

    /**
     * 获得当前屏幕亮度模式
     * @return 1 自动，0 手动，-1 失败
     */
    private int getScreenMode() {
        int mode = -1;
        try {
            mode = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return mode;
    }

    /**
     * 获得当前系统屏幕亮度值
     * @return 0-255
     */
    private int getSystemScreenBrightness() {
        int screenBrightness = -1;
        try {
            screenBrightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return screenBrightness;
    }

    /**
     * 设置当前屏幕亮度模式
     * @param mode 1 自动，0 手动
     */
    private void setScreenMode(int mode) {
        try {
            Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            Uri uri = Settings.System.getUriFor("screen_brightness_mode");
            mContentResolver.notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存当前系统屏幕亮度值，并使之生效
     * @param paramInt
     */
    private void setSystemScreenBrightness(int paramInt) {
        Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, paramInt);
        Uri uri = Settings.System.getUriFor("screen_brightness");
        mContentResolver.notifyChange(uri, null);
    }

    /**
     * 获取当前亮度，自定义
     * @return
     */
    public int getScreenBrightness() {
        return 10 * getSystemScreenBrightness() / SYSTEM_MAX_BRIGHTNESS;
    }

    /**
     * 调整亮度，自定义
     * @param num 0-10
     */
    public void setScreenBrightness(int num) {
        int mode = -1;
        mode = getScreenMode();
        int a = (int) Math.ceil((num) * SYSTEM_MAX_BRIGHTNESS * 0.1);
        a = a <= 0 ? 0 : a;
        a = a >= 255 ? 255 : a;
        if (mode != 0) {
            setScreenMode(0);
        }
        setSystemScreenBrightness(a);
    }
}
