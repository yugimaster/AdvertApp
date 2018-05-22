package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.DownloadModel;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.entity.res.AdListHttpResult;
import com.grandartisans.advert.model.entity.res.Advert;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPositionVo;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.AppUpgradeData;
import com.grandartisans.advert.model.entity.res.DateSchedule;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TimeSchedule;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.grandartisans.advert.model.entity.res.TokenHttpResult;
import com.grandartisans.advert.model.entity.res.UpgradeHttpResult;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.FileUtils;
import com.grandartisans.advert.utils.Utils;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.body.ProgressInfo;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.observer.DownloadObserver;
import com.ljy.devring.other.RingLog;
import com.ljy.devring.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

public class UpgradeService extends Service {
    private static final String TAG = UpgradeService.class.getSimpleName();
    DownloadObserver mDownloadObserver=null;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appUpgrade(getApplicationContext());
        getToken();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void appUpgrade (Context context) {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        AppUpgradeParameter parameter = new AppUpgradeParameter();
        parameter.setAndroidVersion(Utils.getAppVersionCode(context));
        parameter.setAppIdent(Utils.getAppPackageName(context));
        //parameter.setAppIdent("123456");
        parameter.setAppName(Utils.getAppPackageName(context));
        parameter.setDeviceClientid(CommonUtil.getEthernetMac());
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setSystemVersion("2.0.1");
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setVersion(Utils.getAppVersionName(context));
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(parameter.getDeviceClientid()).append("$").append(parameter.getTimestamp()).append("$123456");
        signed = encrypt.MD5Encode(sign.toString(),"");
        parameter.setSign(signed);
        DevRing.httpManager().commonRequest(mIModel.appUpgrade(parameter), new CommonObserver<UpgradeHttpResult>() {
            @Override
            public void onResult(UpgradeHttpResult appUpgradeDataAdHttpResult) {
                Log.d(TAG,"upgrade result status = " + appUpgradeDataAdHttpResult.getStatus());
                if(appUpgradeDataAdHttpResult.getStatus()==1000) { // 检查到升级
                    AppUpgradeData data = (AppUpgradeData) appUpgradeDataAdHttpResult.getData();
                    Log.d(TAG,"filePath = " + data.getFilePath());
                    if(data.getFilePath()!=null) {
                        downloadFile(data.getFilePath(),data.getFileMd5(),"upgrade.apk",0);
                    }
                }else {
                    Log.d(TAG,"not need upgrade");
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        },null);
    }

    private void getToken() {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        TokenParameter tokenParameter = new TokenParameter();
        tokenParameter.setDeviceClientid(CommonUtil.getEthernetMac());
        tokenParameter.setTimestamp(System.currentTimeMillis());
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(tokenParameter.getDeviceClientid()).append("$").append(tokenParameter.getTimestamp()).append("$123456");
        signed = encrypt.MD5Encode(sign.toString(),"");
        tokenParameter.setSign(signed);

        DevRing.httpManager().commonRequest(mIModel.getToken(tokenParameter), new CommonObserver<TokenHttpResult>() {
            @Override
            public void onResult(TokenHttpResult tokenDataAdHttpResult) {
                RingLog.d("gettoken ok status = " + tokenDataAdHttpResult.getStatus() );
                if(tokenDataAdHttpResult.getStatus()==0 && tokenDataAdHttpResult.getData().getToken()!=null)
                    getAdList(tokenDataAdHttpResult.getData().getToken());
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("gettoken error i = " + i + "msg = " + s );
            }
        },null);

    }

    private void getAdList(String token) {
        AdvertModel mIModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        parameter.setDeviceClientid(CommonUtil.getEthernetMac());
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(token);
        DevRing.httpManager().commonRequest(mIModel.getAdertList(parameter), new CommonObserver<AdListHttpResult>() {
            @Override
            public void onResult(AdListHttpResult result) {
                //RingLog.d("getAdList ok result = " + result );
                if(result.getStatus() ==0 ) {
                    List<TemplateRegion> regionList  = result.getData().getTemplate().getRegionList();
                    TemplateRegion region = regionList.get(0);
                    Long advertPositionId = result.getData().getRelationMap().get(region.getIdent());
                    AdvertPositionVo advertPositionVo = result.getData().getAdvertPositionMap().get(advertPositionId);
                    List<DateScheduleVo> dateScheduleVos  = advertPositionVo.getDateScheduleVos();
                    int size = dateScheduleVos.size();
                    EventBus.getDefault().postSticky(new AppEvent(AppEvent.ADVERT_LIST_UPDATE_EVENT,dateScheduleVos));
                    for(int i=0;i<size;i++) {
                        DateScheduleVo dateSchedueVo = dateScheduleVos.get(i);
                        DateSchedule dateSchedue = dateSchedueVo.getDateSchedule();
                        RingLog.d("getAdList Schedue start date = " +  dateSchedue.getStartDate() + "end date=" + dateSchedue.getEndDate());
                        List<TimeScheduleVo> TimeSchedueVos = dateSchedueVo.getTimeScheduleVos();
                        for(int j=0;j<TimeSchedueVos.size();j++){
                            TimeScheduleVo timeScheduleVo = TimeSchedueVos.get(j);
                            TimeSchedule timeSchedule = timeScheduleVo.getTimeSchedule();
                            RingLog.d("getAdList Schedue start time = " +  timeSchedule.getStartTime() + "end time=" + timeSchedule.getEndTime());
                            List<AdvertVo> packageAdverts = timeScheduleVo.getPackageAdverts();
                            for(int k=0;k<packageAdverts.size();k++) {
                                AdvertVo advertVo = packageAdverts.get(k);
                                Advert advert = advertVo.getAdvert();
                                RingLog.d("getAdList advert name = " +  advert.getName() + "advert description :" + advert.getDescription());
                                List<AdvertFile> fileList= advertVo.getFileList();
                                for(int l=0;l<fileList.size();l++) {
                                    AdvertFile advertFile = fileList.get(l);
                                    File file = new File(FileUtil.getExternalCacheDir(getApplicationContext()), advertFile.getFileMd5()+".mp4");
                                    if(!file.exists()) {
                                        RingLog.d("getAdList advert file md5  = " + advertFile.getFileMd5() + "path= " + advertFile.getFilePath());
                                        downloadFile(advertFile.getFilePath(), advertFile.getFileMd5(), advertFile.getFileMd5() + ".mp4", 1);
                                    }else {
                                        EventBus.getDefault().postSticky(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT,FileUtil.getExternalCacheDir(getApplicationContext())+"/"+advertFile.getFileMd5()+".mp4"));
                                    }
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("getAdList onError i = " + i + "message = " + s );
            }

        },null);
    }

    /**
     * 下载文件
     */
    public void downloadFile(String downloadURL,final String fileMd5,String fileName,final int type) {
        File file = FileUtil.getFile(FileUtil.getExternalCacheDir(getApplicationContext()), fileName);
        DownloadModel mIModel = new DownloadModel();
        //不为空则不重新构造DownloadObserver，避免创造了多个进度监听回调
        //if (mDownloadObserver == null) {
            //DownloadObserver构造函数传入要要监听的下载地址
            mDownloadObserver = new DownloadObserver(downloadURL) {
                @Override
                public void onResult(boolean isSaveSuccess, String filePath) {
                    if (isSaveSuccess) {
                        String downloadmd5 = EncryptUtil.md5sum(filePath);
                       // Log.d(TAG,"DownloadSuccess:" + filePath + "downloadmd5 = " + downloadmd5 + "fileMd5 = " + fileMd5 );
                        if(downloadmd5.equals(fileMd5)) {
                            if(type == 0) { /*apk 下载*/
                                Utils.installSilently(filePath);
                            }else if(type ==1) {/*视频下载*/
                                EventBus.getDefault().postSticky(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT,filePath));
                                Log.d(TAG,"Download File finished filePath :" + filePath );
                            }
                        }
                    }else {
                        Log.d(TAG,"DownloadFailed:" + filePath );
                    }
                }

                @Override
                public void onError(long progressInfoId, String errMessage) {

                }

                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    //Log.d(TAG,"DownloadProgress:" + progressInfo.getPercent() );
                }
            };
        //}
        DevRing.httpManager().downloadRequest(file, mIModel.downloadFile(downloadURL), mDownloadObserver, null);
    }
}
