package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.DownloadModel;
import com.grandartisans.advert.model.entity.DownloadInfo;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.post.HeartBeatParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.entity.res.AdListHttpResult;
import com.grandartisans.advert.model.entity.res.Advert;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPosition;
import com.grandartisans.advert.model.entity.res.AdvertPositionVo;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.AppUpgradeData;
import com.grandartisans.advert.model.entity.res.DateSchedule;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.model.entity.res.HeartBeatData;
import com.grandartisans.advert.model.entity.res.HeartBeatResult;
import com.grandartisans.advert.model.entity.res.PositionVer;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TimeSchedule;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.grandartisans.advert.model.entity.res.TokenHttpResult;
import com.grandartisans.advert.model.entity.res.UpgradeHttpResult;
import com.grandartisans.advert.utils.AdvertVersion;
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
import java.util.ArrayList;
import java.util.List;

public class UpgradeService extends Service {
    private static final String TAG = UpgradeService.class.getSimpleName();
    DownloadObserver mDownloadObserver=null;
    private List<DownloadInfo> downloadList = new ArrayList<DownloadInfo>();
    private final int DOWNLOAD_COMPLITE_CMD = 100001;
    private final int DOWNLOAD_ERROR_CMD = 100002;
    private final int HEART_BEAT_CMD = 10003;

    private final int HEART_BEAT_INTERVAL_TIME = 30*1000;// 心跳检测发送时间

    private int mDownloadStatus = DownloadInfo.STATUS_COMPLETE;

    private String  mToken ;
    private AdvertPosition mAdverPosition;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message paramMessage)
        {
            String fileMd5;
            switch (paramMessage.what)
            {
                case DOWNLOAD_COMPLITE_CMD:
                    fileMd5 = (String)paramMessage.obj;
                    setDownloadStatus(fileMd5,DownloadInfo.STATUS_COMPLETE);
                    downloadAdList();
                    break;
                case DOWNLOAD_ERROR_CMD:
                    fileMd5 = (String)paramMessage.obj;
                    setDownloadStatus(fileMd5,DownloadInfo.STATUS_DOWNLOAD_ERROR);
                    downloadAdList();
                    break;
                case HEART_BEAT_CMD:
                    heardBeat(mToken);
                    break;
                default:
                    break;
            }
            super.handleMessage(paramMessage);
        }
    };

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
                if(tokenDataAdHttpResult.getStatus()==0 && tokenDataAdHttpResult.getData().getToken()!=null) {
                    mToken = tokenDataAdHttpResult.getData().getToken();
                    heardBeat(tokenDataAdHttpResult.getData().getToken());
                }
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("gettoken error i = " + i + "msg = " + s );
            }
        },null);

    }

    private void heardBeat(final String token ) {
        AdvertModel mIModel = new AdvertModel();
        HeartBeatParameter parameter = new HeartBeatParameter();
        parameter.setDeviceClientid(CommonUtil.getEthernetMac());
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(token);
        DevRing.httpManager().commonRequest(mIModel.sendHeartBeat(parameter), new CommonObserver<HeartBeatResult>() {
            @Override
            public void onResult(HeartBeatResult result) {
                RingLog.d("send HeartBeat ok status = " + result.getStatus() );
                if(result!=null){
                    if(result.getStatus()==0) {
                        HeartBeatData data = result.getData();
                        if (data != null) {
                            List<PositionVer> list = data.getList();
                            if (list != null && list.size() > 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    PositionVer item = list.get(i);
                                    if (AdvertVersion.getAdPositionId() > 0) {
                                        if (item.getAdvertPositionId() == AdvertVersion.getAdPositionId()) {
                                            if (item.getVersion() != AdvertVersion.getAdVersion(AdvertVersion.getAdPositionId())) {
                                                if (!isDownloadingAdFiles()) getAdList(token);
                                            }
                                        }
                                    } else {
                                        if(!isDownloadingAdFiles()) getAdList(token);
                                    }
                                }
                            }
                        }
                    }else if(result.getStatus()==9800) { // token 已过期 ，重新获取
                        getToken();
                    }
                }
                mHandler.sendEmptyMessageDelayed(HEART_BEAT_CMD, HEART_BEAT_INTERVAL_TIME);
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("send HeartBeat error  i = " + i + "msg = " + s );
                mHandler.sendEmptyMessageDelayed(HEART_BEAT_CMD, HEART_BEAT_INTERVAL_TIME);
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
                downloadList.clear();
                if(result.getStatus() ==0 ) {
                    List<TemplateRegion> regionList  = result.getData().getTemplate().getRegionList();
                    TemplateRegion region = regionList.get(0);
                    Long advertPositionId = result.getData().getRelationMap().get(region.getIdent());
                    AdvertPositionVo advertPositionVo = result.getData().getAdvertPositionMap().get(advertPositionId);
                    if(advertPositionVo!=null) {
                        List<DateScheduleVo> dateScheduleVos = advertPositionVo.getDateScheduleVos();
                        mAdverPosition = advertPositionVo.getadvertPosition();
                        int size = dateScheduleVos.size();
                        EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_UPDATE_EVENT, dateScheduleVos));
                        for (int i = 0; i < size; i++) {
                            DateScheduleVo dateSchedueVo = dateScheduleVos.get(i);
                            DateSchedule dateSchedue = dateSchedueVo.getDateSchedule();
                            RingLog.d("getAdList Schedue start date = " + dateSchedue.getStartDate() + "end date=" + dateSchedue.getEndDate());
                            List<TimeScheduleVo> TimeSchedueVos = dateSchedueVo.getTimeScheduleVos();
                            for (int j = 0; j < TimeSchedueVos.size(); j++) {
                                TimeScheduleVo timeScheduleVo = TimeSchedueVos.get(j);
                                TimeSchedule timeSchedule = timeScheduleVo.getTimeSchedule();
                                RingLog.d("getAdList Schedue start time = " + timeSchedule.getStartTime() + "end time=" + timeSchedule.getEndTime());
                                List<AdvertVo> packageAdverts = timeScheduleVo.getPackageAdverts();
                                for (int k = 0; k < packageAdverts.size(); k++) {
                                    AdvertVo advertVo = packageAdverts.get(k);
                                    Advert advert = advertVo.getAdvert();
                                    RingLog.d("getAdList advert name = " + advert.getName() + "advert description :" + advert.getDescription());
                                    List<AdvertFile> fileList = advertVo.getFileList();
                                    for (int l = 0; l < fileList.size(); l++) {
                                        AdvertFile advertFile = fileList.get(l);
                                        DownloadInfo downloadInfo = new DownloadInfo();
                                        downloadInfo.setId(advertFile.getId());
                                        downloadInfo.setFileMd5(advertFile.getFileMd5());
                                        downloadInfo.setUrl(advertFile.getFilePath());
                                        downloadInfo.setName(advertFile.getName());
                                        downloadInfo.setStatus(DownloadInfo.STATUS_NOT_DOWNLOAD);
                                        downloadList.add(downloadInfo);
                                        /*
                                        String filepath = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + advertFile.getFileMd5() + ".mp4";
                                        File file = new File(FileUtil.getExternalCacheDir(getApplicationContext()), advertFile.getFileMd5() + ".mp4");
                                        if (file.exists() && file.length() >0 ) {
                                            if(EncryptUtil.md5sum(filepath).equals(advertFile.getFileMd5()))
                                            {
                                                EventBus.getDefault().postSticky(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT, FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + advertFile.getFileMd5() + ".mp4"));
                                            }else {
                                                file.deleteOnExit();
                                                RingLog.d("getAdList advert file md5  = " + advertFile.getFileMd5() + "path= " + advertFile.getFilePath());
                                                downloadFile(advertFile.getFilePath(), advertFile.getFileMd5(), advertFile.getFileMd5() + ".mp4", 1);
                                            }
                                        } else {
                                            RingLog.d("getAdList advert file md5  = " + advertFile.getFileMd5() + "path= " + advertFile.getFilePath());
                                            downloadFile(advertFile.getFilePath(), advertFile.getFileMd5(), advertFile.getFileMd5() + ".mp4", 1);
                                        }
                                        */
                                    }
                                }
                            }
                        }
                    }
                }
                downloadAdList();
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("getAdList onError i = " + i + "message = " + s );
            }

        },null);
    }

    private void downloadAdList() {
        boolean finished  = true;
        int size = downloadList.size();
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(item.getStatus()==DownloadInfo.STATUS_NOT_DOWNLOAD) {
                item.setStatus(DownloadInfo.STATUS_DOWNLOADING);
                finished =false;
                downloadFile(item.getUrl(), item.getFileMd5(), item.getFileMd5() + ".mp4", 1);
                break;
            }
        }
        for(int j=0;j<size;j++) {
            DownloadInfo item = downloadList.get(j);
            if(item.getStatus()==DownloadInfo.STATUS_DOWNLOAD_ERROR) {
                item.setStatus(DownloadInfo.STATUS_DOWNLOADING);
                finished = false;
                downloadFile(item.getUrl(), item.getFileMd5(), item.getFileMd5() + ".mp4", 1);
                break;
            }
        }
        if(finished) {
            EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_DOWNLOAD_FINISHED_EVENT,mAdverPosition));
        }
    }

    private boolean isDownloadingAdFiles(){
        boolean isDownloading = false;
        int size = downloadList.size();
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(item.getStatus()!=DownloadInfo.STATUS_COMPLETE) {
                isDownloading = true;
                break;
            }
        }
        return isDownloading;
    }

    private void setDownloadStatus(String fileMd5,int status) {
        int size = downloadList.size();
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(item.getFileMd5().equals(fileMd5)) {
                item.setStatus(status);
                break;
            }
        }
    }

    /**
     * 下载视频广告文件
     */
    public void downloadFile(String downloadURL, final String fileMd5, final String fileName, final int type) {
        String filepath = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + fileMd5 + ".mp4";
        File fileCheck = new File(FileUtil.getExternalCacheDir(getApplicationContext()), fileMd5 + ".mp4");
        if (fileCheck.exists() && fileCheck.length() >0 ) {
            if(EncryptUtil.md5sum(filepath).equals(fileMd5))
            {
                Message msg = new Message();
                msg.what = DOWNLOAD_COMPLITE_CMD;
                msg.obj = fileMd5;
                mHandler.sendMessage(msg);
                EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT, FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + fileMd5 + ".mp4"));
                return;
            }else {
                fileCheck.deleteOnExit();
            }
        }

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
                                Message msg = new Message();
                                msg.what = DOWNLOAD_COMPLITE_CMD;
                                msg.obj = fileMd5;
                                mHandler.sendMessage(msg);
                                EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT,filePath));
                                Log.d(TAG,"Download File finished filePath :" + filePath );
                            }
                        }
                    }else {
                        if(type == 1) {
                            Message msg = new Message();
                            msg.what = DOWNLOAD_ERROR_CMD;
                            msg.obj = fileMd5;
                            mHandler.sendMessage(msg);
                            Log.d(TAG, "Download ad file Failed:" + filePath);
                        }
                    }
                }

                @Override
                public void onError(long progressInfoId, String errMessage) {
                    if(type ==1 ) {
                        Message msg = new Message();
                        msg.what = DOWNLOAD_ERROR_CMD;
                        msg.obj = fileMd5;
                        mHandler.sendMessage(msg);
                        Log.d(TAG, "Download ad file Failed:" + fileName);
                    }
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
