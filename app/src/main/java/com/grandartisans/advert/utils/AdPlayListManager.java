package com.grandartisans.advert.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.dbutils.PlayRecord;
import com.grandartisans.advert.interfaces.AdListEventListener;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPosition;
import com.grandartisans.advert.model.entity.res.AdvertPositionVo;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.ljy.devring.DevRing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class AdPlayListManager {
    private final String TAG = "AdPlayListManager";
    private volatile static AdPlayListManager mPlayListInstance = null;
    private List<PlayingAdvert> adurls = new ArrayList<PlayingAdvert>();
    private List<PlayingAdvert> adurls_local = new ArrayList<PlayingAdvert>();
    private AdListEventListener mAdListEventListener = null;
    ReentrantLock lock = new ReentrantLock();
    private AdPlayListManager (Context context) {}
    public static AdPlayListManager getInstance(Context context) {
        if (mPlayListInstance == null) {
            synchronized (AdPlayListManager.class) {
                if (mPlayListInstance == null) {
                    mPlayListInstance = new AdPlayListManager(context);
                }
            }
        }
        return mPlayListInstance;
    }
    public boolean init(){
        boolean res = false;
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
        return false;
    }
    public void  registerListene(AdListEventListener listener){
        if(listener!=null) mAdListEventListener = listener;
    }
    public boolean updatePlayList( List<PlayingAdvert> urls){
        boolean res = true;
        lock.lock();
        adurls.clear();
        for(int i=0;i<urls.size();i++){
            adurls.add(urls.get(i));
        }
        lock.unlock();
        return res;
    }

    public  String  getValidPlayUrl(int playindex) {
        String url=null;
        lock.lock();
        Log.i(TAG,"adurls size  = " + adurls.size() + "playindex = " + playindex);
        Log.i(TAG,"adurls_local size  = " + adurls_local.size() + "playindex = " + playindex);
        boolean urlvalid = false;
        if(adurls.size()>0) {
            url = findPlayUrl(playindex);
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
    private String findPlayUrl(int playindex){
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

    public void saveAdvertVersion(AdvertPosition advertPosition) {
        Gson gson = new Gson();
        String str = gson.toJson(adurls);
        Log.i(TAG, "save advertlist = " + str);
        DevRing.cacheManager().diskCache("advertList").put("playList", str);

        AdvertVersion.setAdVersion(advertPosition.getId().intValue(), advertPosition.getVersion());
        if(mAdListEventListener!=null) {
            mAdListEventListener.onAdListUpdate();
        }
    }
    public PlayingAdvert getPlayingAd(int playindex){
        if(adurls.size()>0) {
            int index = playindex % adurls.size();
            PlayingAdvert item = adurls.get(index);
            return item;
        }
        return null;
    }
}
