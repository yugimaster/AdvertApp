package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.res.AdvertInfoData;
import com.grandartisans.advert.model.entity.res.AdvertInfoResult;
import com.grandartisans.advert.model.entity.res.AdvertWeatherData;
import com.grandartisans.advert.model.entity.res.AdvertWeatherResult;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class AdvertInfoService extends Service {

    private static final String TAG = AdvertInfoService.class.getSimpleName();
    private static final int GET_ADVERT_INFO = 100001;
    private static final int GET_ADVERT_WEATHER = 100002;
    private static final int UPDATE_ADVERT_WEATHER = 100003;
    private static final int REFRESH_ADVERT_INFO = 100004;

    private String mDeviceId = "";
    private int mAdvertInfoCount = 0;
    private int mAdvertWeatherCount = 0;
    private int mTimerCount = 0;

    private Binder mInfoBinder = new InfoBinder();
    private Timer mTimer;
    private TimerTask mTimerTask;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mInfoBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        closeTimer();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getAdvertInfo();
        startUpdateAdWeatherTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    public class InfoBinder extends Binder {
        public AdvertInfoService getService() {
            return AdvertInfoService.this;
        }
    }

    public void refreshAdvertInfo() {
        AdvertModel advertModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        parameter.setDeviceClientid(mDeviceId);
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(UpgradeService.mToken);
        DevRing.httpManager().commonRequest(advertModel.getAdvertInfo(parameter), new CommonObserver<AdvertInfoResult>() {
            @Override
            public void onResult(AdvertInfoResult result) {
                if (result.getStatus() == 0) {
                    if (result.getData() != null) {
                        setAdvertInfoCache(result.getData());
                        EventBus.getDefault().post(new AppEvent(AppEvent.SET_ADVERT_INFO, ""));
                        if (mTimer == null) {
                            startUpdateAdWeatherTimer();
                        }
                    }
                    mAdvertInfoCount = 0;
                } else {
                    mAdvertInfoCount += 1;
                    if (mAdvertInfoCount > 3) {
                        mAdvertInfoCount = 0;
                    } else {
                        mHandler.removeMessages(REFRESH_ADVERT_INFO);
                        mHandler.sendEmptyMessageDelayed(REFRESH_ADVERT_INFO, 3000);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                mAdvertInfoCount += 1;
                if (mAdvertInfoCount > 3) {
                    mAdvertInfoCount = 0;
                } else {
                    mHandler.removeMessages(REFRESH_ADVERT_INFO);
                    mHandler.sendEmptyMessageDelayed(REFRESH_ADVERT_INFO, 3000);
                }
            }
        }, null);
    }

    public void closeTimer() {
        destroyTimer();
        mTimerCount = 0;
    }

    private void getAdvertInfo() {
        AdvertModel IModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        if (mDeviceId.isEmpty()) {
            mDeviceId = SystemInfoManager.getDeviceId();
        }
        parameter.setDeviceClientid(mDeviceId);
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(UpgradeService.mToken);
        DevRing.httpManager().commonRequest(IModel.getAdvertInfo(parameter), new CommonObserver<AdvertInfoResult>() {
            @Override
            public void onResult(AdvertInfoResult result) {
                if (result.getStatus() == 0 && result.getData() != null) {
                    setAdvertInfoCache(result.getData());
                    mAdvertInfoCount = 0;
                    mHandler.sendEmptyMessage(GET_ADVERT_WEATHER);
                } else {
                    mAdvertInfoCount += 1;
                    if (mAdvertInfoCount > 3) {
                        mAdvertInfoCount = 0;
                    } else {
                        mHandler.removeMessages(GET_ADVERT_INFO);
                        mHandler.sendEmptyMessageDelayed(GET_ADVERT_INFO, 3000);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "get advert info error i = " + i + " msg = " + s);
                mAdvertInfoCount += 1;
                if (mAdvertInfoCount > 3) {
                    mAdvertInfoCount = 0;
                } else {
                    mHandler.removeMessages(GET_ADVERT_INFO);
                    mHandler.sendEmptyMessageDelayed(GET_ADVERT_INFO, 3000);
                }
            }
        }, null);
    }

    private void getAdvertWeather() {
        AdvertModel IModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        if (mDeviceId.isEmpty()) {
            mDeviceId = SystemInfoManager.getDeviceId();
        }
        parameter.setDeviceClientid(mDeviceId);
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(UpgradeService.mToken);
        DevRing.httpManager().commonRequest(IModel.getAdvertWeather(parameter), new CommonObserver<AdvertWeatherResult>() {
            @Override
            public void onResult(AdvertWeatherResult result) {
                if (result.getStatus() == 0 && result.getData() != null) {
                    setAvertWeatherCache(result.getData());
                    mAdvertWeatherCount = 0;
                    EventBus.getDefault().post(new AppEvent(AppEvent.SET_ADVERT_INFO, ""));
                } else {
                    mAdvertWeatherCount += 1;
                    if (mAdvertWeatherCount > 3) {
                        mAdvertWeatherCount = 0;
                        EventBus.getDefault().post(new AppEvent(AppEvent.SET_ADVERT_INFO, ""));
                    } else {
                        mHandler.removeMessages(GET_ADVERT_INFO);
                        mHandler.sendEmptyMessageDelayed(GET_ADVERT_INFO, 3000);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "get advert weather error i = " + i + " msg = " + s);
                mAdvertWeatherCount += 1;
                if (mAdvertWeatherCount > 3) {
                    mAdvertWeatherCount = 0;
                    EventBus.getDefault().post(new AppEvent(AppEvent.SET_ADVERT_INFO, ""));
                } else {
                    mHandler.removeMessages(GET_ADVERT_WEATHER);
                    mHandler.sendEmptyMessageDelayed(GET_ADVERT_WEATHER, 3000);
                }
            }
        }, null);
    }

    private void updateAdvertWeather() {
        AdvertModel advertModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        parameter.setDeviceClientid(mDeviceId);
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(UpgradeService.mToken);
        DevRing.httpManager().commonRequest(advertModel.getAdvertWeather(parameter), new CommonObserver<AdvertWeatherResult>() {
            @Override
            public void onResult(AdvertWeatherResult result) {
                mTimerCount = -1;
                if (result.getStatus() == 0 && result.getData() != null) {
                    setAvertWeatherCache(result.getData());
                    mAdvertWeatherCount = 0;
                    EventBus.getDefault().post(new AppEvent(AppEvent.UPDATE_ADVERT_WEATHER, ""));
                } else {
                    mAdvertWeatherCount += 1;
                    if (mAdvertWeatherCount < 4) {
                        mHandler.removeMessages(UPDATE_ADVERT_WEATHER);
                        mHandler.sendEmptyMessageDelayed(UPDATE_ADVERT_WEATHER, 3000);
                    } else {
                        mAdvertWeatherCount = 0;
                    }
                }
                mTimerCount = 0;
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "update advert weather error i = " + i + " msg = " + s);
                mTimerCount = -1;
                mAdvertWeatherCount += 1;
                if (mAdvertWeatherCount < 4) {
                    mHandler.removeMessages(UPDATE_ADVERT_WEATHER);
                    mHandler.sendEmptyMessageDelayed(UPDATE_ADVERT_WEATHER, 3000);
                } else {
                    mAdvertWeatherCount = 0;
                }
                mTimerCount = 0;
            }
        }, null);
    }

    private void setAdvertInfoCache(AdvertInfoData advertInfoData) {
        if (advertInfoData.getGroupId() == null) {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("groupId", 1L);
        } else {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("groupId", advertInfoData.getGroupId());
        }
        DevRing.cacheManager().spCache("AdvertInfoResult").put("name", advertInfoData.getName());
        if (advertInfoData.getType() == null) {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("type", 1L);
        } else {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("type", advertInfoData.getType());
        }
        DevRing.cacheManager().spCache("AdvertInfoResult").put("vTime", advertInfoData.getvTime());
        DevRing.cacheManager().spCache("AdvertInfoResult").put("weather", advertInfoData.getWeather());
        DevRing.cacheManager().spCache("AdvertInfoResult").put("backColor", advertInfoData.getBackColor());
        DevRing.cacheManager().spCache("AdvertInfoResult").put("fontColor", advertInfoData.getFontColor());
        if (advertInfoData.getFontSize() == null) {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("fontSize", 30L);
        } else {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("fontSize", advertInfoData.getFontSize());
        }
        if (advertInfoData.getVelocity() == null) {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("velocity", 1L);
        } else {
            DevRing.cacheManager().spCache("AdvertInfoResult").put("velocity", advertInfoData.getVelocity());
        }
        DevRing.cacheManager().spCache("AdvertInfoResult").put("writing", advertInfoData.getWriting());
    }

    private void setAvertWeatherCache(AdvertWeatherData advertWeatherData) {
        DevRing.cacheManager().spCache("AdvertWeatherResult").put("cityName", advertWeatherData.getCityName());
        DevRing.cacheManager().spCache("AdvertWeatherResult").put("type", advertWeatherData.getType());
        DevRing.cacheManager().spCache("AdvertWeatherResult").put("air", advertWeatherData.getAir());
        DevRing.cacheManager().spCache("AdvertWeatherResult").put("celcius", advertWeatherData.getCelcius());
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_ADVERT_INFO:
                    getAdvertInfo();
                    break;
                case GET_ADVERT_WEATHER:
                    getAdvertWeather();
                    break;
                case UPDATE_ADVERT_WEATHER:
                    updateAdvertWeather();
                    break;
                case REFRESH_ADVERT_INFO:
                    refreshAdvertInfo();
                    break;
                default:
                    break;
            }
        }
    };

    private void startUpdateAdWeatherTimer() {
        destroyTimer();
        mTimerCount = 0;
        initTimer();
        mTimer.schedule(mTimerTask, 0, 60 * 1000);
    }

    /**
     * init timer
     */
    private void initTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 两小时一次刷新天气数据
                if (mTimerCount > 119) {
                    mHandler.removeMessages(UPDATE_ADVERT_WEATHER);
                    mHandler.sendEmptyMessageDelayed(UPDATE_ADVERT_WEATHER, 3000);
                } else if (mTimerCount >= 0) {
                    mTimerCount += 1;
                }
            }
        };
    }

    /**
     * destroy timer
     */
    private void destroyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
}
