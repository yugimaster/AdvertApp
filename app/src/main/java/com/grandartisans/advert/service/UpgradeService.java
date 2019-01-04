package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.dbutils.PlayRecord;
import com.grandartisans.advert.dbutils.dbutils;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.DownloadModel;
import com.grandartisans.advert.model.entity.DownloadInfo;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.TerminalGroup;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AdvertPositionContent;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.post.DownLoadContent;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.HeartBeatParameter;
import com.grandartisans.advert.model.entity.post.ReportEventData;
import com.grandartisans.advert.model.entity.post.ReportInfoParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.entity.post.UserAgent;
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
import com.grandartisans.advert.model.entity.res.PowerOnOffData;
import com.grandartisans.advert.model.entity.res.ReportInfoResult;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TimeSchedule;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.grandartisans.advert.model.entity.res.TokenHttpResult;
import com.grandartisans.advert.model.entity.res.UpgradeHttpResult;
import com.grandartisans.advert.utils.AdvertVersion;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.FileOperator;
import com.grandartisans.advert.utils.FileUtils;
import com.grandartisans.advert.utils.OnUsbState;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.grandartisans.advert.utils.Utils;
import com.grandartisans.advert.utils.ZipUtils;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.body.ProgressInfo;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.observer.DownloadObserver;
import com.ljy.devring.other.RingLog;
import com.ljy.devring.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpgradeService extends Service {
    private static final String TAG = UpgradeService.class.getSimpleName();
    DownloadObserver mDownloadObserver=null;
    private List<DownloadInfo> downloadList = new ArrayList<DownloadInfo>();
    private final int DOWNLOAD_COMPLITE_CMD = 100001;
    private final int DOWNLOAD_ERROR_CMD = 100002;
    private final int HEART_BEAT_CMD = 10003;
    private final int UPGRADE_APP_CMD = 10004;
    private final int GETTOKEN_CMD = 10005;
    private final int UPGRADE_APP_ON_USB_CMD = 10006;
    private final int SHOW_TIME_INFO_CMD = 10007;

    private final int HEART_BEAT_INTERVAL_TIME = 60*1000;// 心跳检测发送时间

    private final int UPGRADE_INTERVAL_TIME = 10*60*1000;

    private int mDownloadStatus = DownloadInfo.STATUS_COMPLETE;

    public static String  mToken ;
    private AdvertPosition mAdverPosition;

    private boolean isPowerAlarmSet = false;

    private OnUsbState onUsb = null;

    private Handler handler;


    private final String USB_UPGRADE_DIR = "/GA";
    private final String USB_UPGRADE_GROUP_ZIPFIlE = "group_terminal.GA";
    private final String USB_UPGRADE_GROUP_ORGFILE = "group_terminal.config";
    private final String USB_UPGRADE_DEST_DIR = "/cache";

    private final String USB_UPGRADE_APP_ZIPFILE = "com.grandartisans.advert.GA";
    private final String USB_UPGRADE_TIMEAPP_ZIPFILE = "com.tofu.locationinfo.GA";
    private final String USB_UPGRADE_APP_ORGFILE = "com.grandartisans.advert.config";
    private final String USB_UPGRADE_TIMEAPP_ORGFILE = "com.tofu.locationinfo.config";
    private final String USB_UPGRADE_SCHEDULE_ORGFILE = "schedule.config";
    private final String USB_UPGRADE_FILE_SUFFIX = ".GA";
    private final String USB_UPGRADE_ORGFILE_SUFFIX = ".config";
    private final String USB_UPGRADE_SCHEDULE_SUB = "_group_times";

    private String mUsbPath = "";

    private List<PlayingAdvert> adurls = new ArrayList<PlayingAdvert>();

    Runnable runableUsbUpgrade = new Runnable() {
        @Override
        public void run() {
            if(FileOperator.fileIsExists(mUsbPath+USB_UPGRADE_DIR+ "/" + USB_UPGRADE_GROUP_ZIPFIlE)){
                FileOperator.copyFileToDir(mUsbPath+USB_UPGRADE_DIR + "/" + USB_UPGRADE_GROUP_ZIPFIlE,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                File file = new File(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR+"/" + USB_UPGRADE_GROUP_ZIPFIlE);
                try {
                    ZipUtils.upZipFile(file,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder sb1 = new StringBuilder();
                try {
                    sb1 = FileOperator.convertStreamToString(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+ USB_UPGRADE_GROUP_ORGFILE);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(FileOperator.hexStr2Str(sb1.toString()));


                String eventDataString = sb.toString();
                Gson gson = new Gson();
                if (eventDataString != null) {
                    List<TerminalGroup> list = new ArrayList<>();
                    list = gson.fromJson(eventDataString, new TypeToken<List<TerminalGroup>>() {
                    }.getType());
                    RingLog.d(TAG, "USB Plugined list size = " + list.size());
                    String deviceid = SystemInfoManager.readFromNandkey("usid").toUpperCase();
                    for (int i = 0; i < list.size(); i++) {
                        TerminalGroup group = list.get(i);
                        RingLog.d(TAG, "USB Plugined map = " + group.getGroupId());
                        String terminials[] = group.getArrTerminal();
                        for (int j = 0; j < terminials.length; j++) {
                            if(deviceid.equals(terminials[j].toUpperCase())){
                                RingLog.d(TAG, "USB Plugined map = " + terminials[j]);

                                DevRing.cacheManager().spCache("usbActivate").put("activate",1);

                                //检查右上角时间信息apk升级
                                if(FileOperator.fileIsExists(mUsbPath+USB_UPGRADE_DIR+ "/" + USB_UPGRADE_TIMEAPP_ZIPFILE)){
                                    FileOperator.copyFileToDir(mUsbPath+USB_UPGRADE_DIR + "/" + USB_UPGRADE_TIMEAPP_ZIPFILE,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                    file = new File(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR+"/" + USB_UPGRADE_TIMEAPP_ZIPFILE);
                                    try {
                                        ZipUtils.upZipFile(file,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    StringBuilder sb2 = new StringBuilder();

                                    try {
                                        sb2 = FileOperator.convertStreamToString(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+ USB_UPGRADE_TIMEAPP_ORGFILE);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }

                                    StringBuilder sb3 = new StringBuilder();
                                    sb3.append(FileOperator.hexStr2Str(sb2.toString()));

                                    String appUpgradeDataString = sb3.toString();
                                    if (appUpgradeDataString != null) {
                                        UpgradeHttpResult resultUpgrade = new UpgradeHttpResult();
                                        resultUpgrade = gson.fromJson(appUpgradeDataString, UpgradeHttpResult.class);
                                        if(resultUpgrade!=null){
                                            if(resultUpgrade.getData().getAndroidVersion() > Utils.getAppVersionCode(getApplicationContext(),"com.tofu.locationinfo")){
                                                sendMessage("开始更新时间信息应用",false);
                                                int index = resultUpgrade.getData().getFilePath().lastIndexOf("/");
                                                String fileName = resultUpgrade.getData().getFilePath().substring(index+1);

                                                String destFile = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + "LocationInfo.apk";
                                                String srcFile = USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+fileName;
                                                FileOperator.moveFile(srcFile,destFile);
                                                file = new File(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                                FileOperator.deleteFile(file);
                                                Utils.installSilently(destFile);
                                            }
                                        }
                                    }
                                }




                                String scheduleFileName = group.getGroupId()+USB_UPGRADE_SCHEDULE_SUB+USB_UPGRADE_FILE_SUFFIX;
                                //FileOperator.copyFileToDir(mUsbPath+USB_UPGRADE_DIR + "/" + scheduleFileName,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                file = new File(mUsbPath + USB_UPGRADE_DIR+"/" + scheduleFileName);
                                try {
                                    ZipUtils.upZipFile(file,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                StringBuilder sb4 = new StringBuilder();
                                scheduleFileName =USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+ group.getGroupId()+USB_UPGRADE_SCHEDULE_SUB+USB_UPGRADE_ORGFILE_SUFFIX;
                                try {
                                    sb4 = FileOperator.convertStreamToString(scheduleFileName);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                StringBuilder sb5 = new StringBuilder();
                                sb5.append(FileOperator.hexStr2Str(sb4.toString()));

                                String scheduleDataString = sb5.toString();
                                if (scheduleDataString != null) {
                                    AdListHttpResult result = new AdListHttpResult();
                                    result = gson.fromJson(scheduleDataString, AdListHttpResult.class);
                                    List<TemplateRegion> regionList  = result.getData().getTemplate().getRegionList();
                                    TemplateRegion region = regionList.get(0);
                                    Long advertPositionId = result.getData().getRelationMap().get(region.getIdent());
                                    AdvertPositionVo advertPositionVo = result.getData().getAdvertPositionMap().get(advertPositionId);
                                    long positionId = advertPositionVo.getadvertPosition().getId();
                                    long positionVersion = advertPositionVo.getadvertPosition().getVersion();

                                    boolean needUpgradeAd = false;
                                    if (AdvertVersion.getAdVersion(positionId) > 0) {
                                        if (positionVersion != AdvertVersion.getAdVersion(positionId)) {
                                            needUpgradeAd = true;
                                        }
                                    } else {
                                        needUpgradeAd = true;
                                    }


                                    //EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_UPDATE_EVENT, result.getData()));
                                    if(needUpgradeAd == true && advertPositionVo!=null) {
                                        sendMessage("开始升级广告视频",true);
                                        List<DateScheduleVo> dateScheduleVos = advertPositionVo.getDateScheduleVos();
                                        //mAdverPosition = advertPositionVo.getadvertPosition();
                                        int size = dateScheduleVos.size();
                                        //EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_UPDATE_EVENT, dateScheduleVos));
                                        for (int ii = 0; ii < size; ii++) {
                                            DateScheduleVo dateSchedueVo = dateScheduleVos.get(ii);
                                            DateSchedule dateSchedue = dateSchedueVo.getDateSchedule();
                                            RingLog.d("getAdList Schedue start date = " + dateSchedue.getStartDate() + "end date=" + dateSchedue.getEndDate());
                                            List<TimeScheduleVo> TimeSchedueVos = dateSchedueVo.getTimeScheduleVos();
                                            for (int jj = 0; jj < TimeSchedueVos.size(); jj++) {
                                                TimeScheduleVo timeScheduleVo = TimeSchedueVos.get(jj);
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
                                                        String destFile = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + advertFile.getFileMd5() + ".mp4";
                                                        int index = advertFile.getFilePath().lastIndexOf("/");
                                                        String srcFile = USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+advertFile.getFilePath().substring(index+1);
                                                        FileOperator.moveFile(srcFile,destFile);
                                                    }
                                                }
                                            }
                                        }
                                        updateAdList(result);
                                        sendMessage("广告视频升级完成",true);
                                    }else {
                                        sendMessage("广告视频已经是最新版本，无需更新",true);
                                    }
                                }

                                //检查apk升级
                                if(FileOperator.fileIsExists(mUsbPath+USB_UPGRADE_DIR+ "/" + USB_UPGRADE_APP_ZIPFILE)){
                                    FileOperator.copyFileToDir(mUsbPath+USB_UPGRADE_DIR + "/" + USB_UPGRADE_APP_ZIPFILE,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                    file = new File(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR+"/" + USB_UPGRADE_APP_ZIPFILE);
                                    try {
                                        ZipUtils.upZipFile(file,USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    StringBuilder sb2 = new StringBuilder();

                                    try {
                                        sb2 = FileOperator.convertStreamToString(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+ USB_UPGRADE_APP_ORGFILE);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }


                                    StringBuilder sb6 = new StringBuilder();
                                    sb6.append(FileOperator.hexStr2Str(sb2.toString()));

                                    String appUpgradeDataString = sb6.toString();
                                    if (appUpgradeDataString != null) {
                                        UpgradeHttpResult resultUpgrade = new UpgradeHttpResult();
                                        resultUpgrade = gson.fromJson(appUpgradeDataString, UpgradeHttpResult.class);
                                        if(resultUpgrade!=null){
                                            if(resultUpgrade.getData().getAndroidVersion() > Utils.getAppVersionCode(getApplicationContext())){
                                                sendMessage("开始更新应用",false);
                                                int index = resultUpgrade.getData().getFilePath().lastIndexOf("/");
                                                String fileName = resultUpgrade.getData().getFilePath().substring(index+1);

                                                String destFile = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + "advert.apk";
                                                String srcFile = USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR + "/"+fileName;
                                                FileOperator.moveFile(srcFile,destFile);
                                                file = new File(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                                FileOperator.deleteFile(file);

                                                mHandler.sendEmptyMessageDelayed(UPGRADE_APP_ON_USB_CMD,1000);
                                            }
                                        }
                                    }
                                }

                                file = new File(USB_UPGRADE_DEST_DIR + USB_UPGRADE_DIR);
                                FileOperator.deleteFile(file);

                                /*拷贝播放记录到U盘*/
                                SavePlayRecordToUsb();

                                sendMessage("USB更新完成",false);
                                return ;
                            }
                            //RingLog.d(TAG, "USB Plugined map = " + terminials[j]);
                        }
                    }
                    RingLog.d(TAG, "USB Plugined 设备不属于任何运营组= ");
                }
            }else{
                RingLog.d(TAG, "USB Plugined  没有找到升级文件 ");
            }

        }
    };

    private void SavePlayRecordToUsb() {
        List<PlayRecord> records = dbutils.getPlayRecordAll(PlayRecord.class);
        String eventlistFile = mUsbPath+USB_UPGRADE_DIR+ "/eventlist";
        List<EventParameter> list = new ArrayList<EventParameter>();
        Gson gson = new Gson();
        if(FileOperator.fileIsExists(eventlistFile)){
            StringBuilder sb = new StringBuilder();
            try {
                sb = FileOperator.convertStreamToString(eventlistFile);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String eventlistDataString = sb.toString();
            if (eventlistDataString != null && eventlistDataString.length()>0) {
                list = gson.fromJson(eventlistDataString, new TypeToken<List<EventParameter<ReportEventData>>>() {
                }.getType());

            }
        }
        if(records!=null && records.size()>0) {
            for(int i=0;i<records.size();i++) {
                PlayRecord recordItem = records.get(i);
                if (recordItem.getCount() >0) {
                    EventParameter parameter = new EventParameter();
                    parameter.setSn(SystemInfoManager.readFromNandkey("usid").toUpperCase());
                    parameter.setSessionid(CommonUtil.getRandomString(50));
                    parameter.setTimestamp(System.currentTimeMillis());
                    parameter.setToken(UpgradeService.mToken);
                    parameter.setApp(Utils.getAppPackageName(getApplicationContext()));
                    parameter.setEvent("playRecord");
                    parameter.setEventtype(4000);

                    parameter.setMac(CommonUtil.getEthernetMac());

                    ReportEventData eventData = new ReportEventData();
                    eventData.setCount(recordItem.getCount());
                    eventData.setAdvertid(recordItem.getAdid());
                    eventData.setAdPositionID(recordItem.getApid());
                    eventData.setTemplateid(recordItem.getTpid());
                    eventData.setStartTime(recordItem.getStarttime());
                    eventData.setEndTime(recordItem.getEndtime());
                    parameter.setEventData(eventData);
                    parameter.setTimestamp(System.currentTimeMillis());
                    list.add(parameter);
                    dbutils.deletePlayRecord(PlayRecord.class,recordItem.getId());

                }
            }
            String str = gson.toJson(list);
            FileOperator.saveStringToFile(eventlistFile,str);
        }

    }
    private void updateAppOnUsb(){
        if(!isDownloadingAdFiles()) {
            String destFile = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + "advert.apk";
            Utils.installSilently(destFile);
        }else{
            mHandler.sendEmptyMessageDelayed(UPGRADE_APP_ON_USB_CMD,1000);
        }
    }
    private void initUSB(Context context){
        handler = new Handler();
        onUsb = new OnUsbState();
        onUsb.init(context);
        onUsb.setUsbChangeListener(new OnUsbState.UsbChangeListener() {

            @Override
            public void onRemove(String path) {
                // TODO Auto-generated method stub
                RingLog.d(TAG,"USB removed path = " + path);
            }

            @Override
            public void onPlugin(String path) {
                // TODO Auto-generated method stub
                RingLog.d(TAG,"USB Plugined path = " + path);
                mUsbPath = path;

                handler.postDelayed(runableUsbUpgrade,1000);

            }
        });
    }

    private void sendMessage(String msg,boolean always_show){
        Intent i = new Intent("android.intent.action.pushcommMessage");
        if(always_show) {
            i.putExtra("action", "local_install");
        }else{
            i.putExtra("action", "action_tip");
        }
        i.putExtra("content", msg);
        sendBroadcast(i);
    }

    private void showTimeInfo(){
        Intent i = new Intent("com.intent.action.openInfo");
        sendBroadcast(i);
    }

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
                case UPGRADE_APP_CMD:
                    appUpgrade(getApplicationContext());
                    break;
                case  GETTOKEN_CMD:
                    getToken(false);
                    break;
                case UPGRADE_APP_ON_USB_CMD:
                    updateAppOnUsb();
                    break;
                case SHOW_TIME_INFO_CMD:
                    showTimeInfo();
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
        initUSB(getApplicationContext());
        appUpgrade(getApplicationContext());
        getToken(false);
        mHandler.sendEmptyMessageDelayed(SHOW_TIME_INFO_CMD,3*1000);
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
        parameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
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
                        downloadWithXutils(data.getFilePath(),data.getFileMd5(),"upgrade.apk",0);
                    }
                }else {
                    Log.d(TAG,"not need upgrade");
                    OtherappUpgrade(getApplicationContext(),"com.tofu.locationinfo");
                    mHandler.removeMessages(UPGRADE_APP_CMD);
                    mHandler.sendEmptyMessageDelayed(UPGRADE_APP_CMD, UPGRADE_INTERVAL_TIME);
                }
            }

            @Override
            public void onError(int i, String s) {
                mHandler.removeMessages(UPGRADE_APP_CMD);
                mHandler.sendEmptyMessageDelayed(UPGRADE_APP_CMD, UPGRADE_INTERVAL_TIME);
            }
        },null);
    }

    private void OtherappUpgrade (Context context,String packageName) {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        AppUpgradeParameter parameter = new AppUpgradeParameter();
        parameter.setAndroidVersion(Utils.getAppVersionCode(context,packageName));
        parameter.setAppIdent(packageName);
        parameter.setAppName(packageName);
        parameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setSystemVersion("2.0.1");
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setVersion(Utils.getAppVersionName(context,packageName));
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
                        downloadWithXutils(data.getFilePath(),data.getFileMd5(),"LocationInfo.apk",0);
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

    private void getToken(boolean expired) {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        TokenParameter tokenParameter = new TokenParameter();
        tokenParameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
        tokenParameter.setTimestamp(System.currentTimeMillis() - SystemClock.elapsedRealtime());
        if(expired==true){
            tokenParameter.setRqeuestUuid("true");
        }else {
            tokenParameter.setRqeuestUuid("false");
        }
        UserAgent useragent = new UserAgent();
        String version = Utils.getAppVersionName(getApplicationContext(),"com.tofu.locationinfo");
        if(version.equals("30")){
            version = "1";
        }else if(version.equals("29") || version.equals("28")){
            version = "0";
        }
        useragent.setAppVersionName(Utils.getAppVersionName((getApplicationContext()))+"."+version);
        useragent.setPlatformVersion(CommonUtil.getVersionInfo());
        tokenParameter.setUserAgent(useragent);
        
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(tokenParameter.getDeviceClientid()).append("$").append(tokenParameter.getTimestamp()).append("$123456");
        signed = encrypt.MD5Encode(sign.toString(),"");
        String strDate = CommonUtil.getSystemBootTime(getApplicationContext());
        tokenParameter.setSign(signed + "_" + strDate);

        DevRing.httpManager().commonRequest(mIModel.getToken(tokenParameter), new CommonObserver<TokenHttpResult>() {
            @Override
            public void onResult(TokenHttpResult tokenDataAdHttpResult) {
                RingLog.d("gettoken ok status = " + tokenDataAdHttpResult.getStatus() );
                if(tokenDataAdHttpResult.getStatus()==0 && tokenDataAdHttpResult.getData().getToken()!=null) {
                    mToken = tokenDataAdHttpResult.getData().getToken();
                    heardBeat(tokenDataAdHttpResult.getData().getToken());
                }else{
                    mHandler.removeMessages(GETTOKEN_CMD);
                    mHandler.sendEmptyMessageDelayed(GETTOKEN_CMD, HEART_BEAT_INTERVAL_TIME);
                }
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("gettoken error i = " + i + "msg = " + s );
                mHandler.removeMessages(GETTOKEN_CMD);
                mHandler.sendEmptyMessageDelayed(GETTOKEN_CMD, 30000);
            }
        },null);

    }


    public void getAppTrafficList(){
        //获取所有的安装在手机上的应用软件的信息，并且获取这些软件里面的权限信息
        PackageManager pm=getPackageManager();//获取系统应用包管理
        //获取每个包内的androidmanifest.xml信息，它的权限等等
        List<PackageInfo> pinfos=pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
        //遍历每个应用包信息
        for(PackageInfo info:pinfos){
            //请求每个程序包对应的androidManifest.xml里面的权限
            String[] premissions=info.requestedPermissions;
            if(premissions!=null && premissions.length>0){
                //找出需要网络服务的应用程序
                for(String premission : premissions){
                    if("android.permission.INTERNET".equals(premission)){
                        //获取每个应用程序在操作系统内的进程id
                        int uId=info.applicationInfo.uid;
                        //如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
                        long rx=TrafficStats.getUidRxBytes(uId);
                        //如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
                        long tx=TrafficStats.getUidTxBytes(uId);

                        RingLog.d(info.applicationInfo.loadLabel(pm)+"消耗的流量-- uid = " + uId +"tx :" + tx +"rx:" + rx);

                    }
                }
            }
        }
    }

    private void heardBeat(final String token ) {
        AdvertModel mIModel = new AdvertModel();
        HeartBeatParameter parameter = new HeartBeatParameter();
        parameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(token);
        DevRing.httpManager().commonRequest(mIModel.sendHeartBeat(parameter), new CommonObserver<HeartBeatResult>() {
            @Override
            public void onResult(HeartBeatResult result) {
                RingLog.d("send HeartBeat ok status = " + result.getStatus() );
                TrafficStats.getTotalTxBytes();
                RingLog.d("Total snd Bytes : " + TrafficStats.getTotalTxBytes() + "recv Bytes: " + TrafficStats.getTotalRxBytes());
                //getAppTrafficList();
                if(result!=null){
                    if(result.getStatus()==0) {
                        List<HeartBeatData> data = result.getData();

                        if (data != null && data.size()>0) {
                            for (int j = 0; j < data.size(); j++) {
                                HeartBeatData dataItem = data.get(j);
                                if (dataItem.getEventID().equals("1001")) { /*广告版本更新检查*/
                                    //List<PositionVer> list = (List<PositionVer>)dataItem.getEventData();
                                    List<PositionVer> list = new ArrayList<>();
                                    if(dataItem.getEventData().getClass().equals(list.getClass())){
                                        String eventDataString = dataItem.getEventData().toString();
                                        Gson gson = new Gson();
                                        if (eventDataString!=null) {
                                            list = gson.fromJson(eventDataString, new TypeToken<List<PositionVer>>() {}.getType());
                                        }
                                    }
                                    //List<PositionVer> list = ( List<PositionVer>)dataItem.getEventData();

                                    //RingLog.d("send HeartBeat  eventData =  " + eventDataString);

                                    if (list != null && list.size() > 0) {
                                        for (int i = 0; i < list.size(); i++) {
                                            PositionVer item = (PositionVer)list.get(i);
                                            RingLog.d("send HeartBeat  positionID " + item.getAdvertPositionId() + "version = " + item.getVersion() );
                                            if (AdvertVersion.getAdVersion(item.getAdvertPositionId()) > 0) {
                                                //if (item.getAdvertPositionId() == AdvertVersion.getAdPositionId()) {
                                                if (item.getVersion() != AdvertVersion.getAdVersion(item.getAdvertPositionId())) {
                                                    if (!isDownloadingAdFiles()) getAdList(token);
                                                }
                                                //}
                                            } else {
                                                if (!isDownloadingAdFiles()) getAdList(token);
                                            }

                                        }
                                    }
                                }else if(dataItem.getEventID().equals("1002")) {/*关机事件*/
                                    EventBus.getDefault().post(new AppEvent(AppEvent.SET_POWER_OFF,""));
                                }else if(dataItem.getEventID().equals("1003")) {/*开机事件*/
                                    EventBus.getDefault().post(new AppEvent(AppEvent.SET_POWER_ON,""));
                                }else if(dataItem.getEventID().equals("1004")) {/*重启事件*/
                                    CommonUtil.reboot(getApplicationContext());
                                }else if(dataItem.getEventID().equals("1005")) {/*查看下载情况*/
                                    List<DownLoadContent> downLoadContents = new ArrayList<DownLoadContent>();
                                    List<HeartBeatData> dataList = new ArrayList<HeartBeatData>();
                                    HeartBeatData infoData = new HeartBeatData();
                                    infoData.setEventID("1005");

                                    DownloadInfo info = getDownloadingAdInfo();
                                    if(info!=null) {
                                        DownLoadContent downLoadContent = new DownLoadContent();
                                        downLoadContent.setAdvertId(info.getId());
                                        downLoadContent.setStatus(1);
                                        downLoadContent.setUrl(info.getUrl());
                                        downLoadContents.add(downLoadContent);

                                    }
                                    infoData.setEventData(downLoadContents);
                                    dataList.add(infoData);

                                    ReportInfoParameter infoParameter = new ReportInfoParameter();
                                    infoParameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
                                    infoParameter.setTimestamp(System.currentTimeMillis());
                                    infoParameter.setToken(token);
                                    infoParameter.setData(dataList);
                                    reportInfo(infoParameter);

                                }else if(dataItem.getEventID().equals("1006")) {/*查看当前播放内容*/
                                    PlayingAdvert playingAdvert = AdvertApp.getPlayingAdvert();
                                    List<AdvertPositionContent> advertPositionContentList = new ArrayList<AdvertPositionContent>();
                                    List<HeartBeatData> dataList = new ArrayList<HeartBeatData>();
                                    HeartBeatData infoData = new HeartBeatData();
                                    infoData.setEventID("1006");

                                    AdvertPositionContent advertPositionContent = new AdvertPositionContent();
                                    advertPositionContent.setAdvertPositionId(playingAdvert.getAdPositionID());
                                    advertPositionContent.setAdvertId(playingAdvert.getAdvertid());
                                    advertPositionContentList.add(advertPositionContent);

                                    infoData.setEventData(advertPositionContentList);

                                    dataList.add(infoData);

                                    ReportInfoParameter infoParameter = new ReportInfoParameter();
                                    infoParameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
                                    infoParameter.setTimestamp(System.currentTimeMillis());
                                    infoParameter.setToken(token);
                                    infoParameter.setData(dataList);
                                    reportInfo(infoParameter);
                                }
                                else if(dataItem.getEventID().equals("1007")) {
                                    List<PowerOnOffData> list = new ArrayList<>();
                                    if(dataItem.getEventData().getClass().equals(list.getClass())){
                                        String eventDataString = dataItem.getEventData().toString();
                                        Gson gson = new Gson();
                                        if (eventDataString!=null) {
                                            list = gson.fromJson(eventDataString, new TypeToken<List<PowerOnOffData>>() {}.getType());
                                        }
                                        if (list != null && list.size() > 0) {
                                            PowerOnOffData powerOnOffData = list.get(0);
                                            RingLog.d(TAG,"poweronoff startTime= " + powerOnOffData.getStartTime() + "endTime = " + powerOnOffData.getEndTime());
                                            long startTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("startTime",0);
                                            long endTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("endTime",0);
                                            if(endTime!=powerOnOffData.getEndTime()){
                                                DevRing.cacheManager().spCache("PowerAlarm").put("endTime",powerOnOffData.getEndTime());
                                                isPowerAlarmSet = false;
                                            }
                                            if(startTime!=powerOnOffData.getStartTime()){
                                                DevRing.cacheManager().spCache("PowerAlarm").put("startTime",powerOnOffData.getStartTime());

                                                isPowerAlarmSet = false;
                                            }
                                            if(isPowerAlarmSet==false){

                                                EventBus.getDefault().post(new AppEvent(AppEvent.POWER_UPDATE_ALARM_EVENT, powerOnOffData));
                                                isPowerAlarmSet = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        mHandler.sendEmptyMessageDelayed(HEART_BEAT_CMD, HEART_BEAT_INTERVAL_TIME);
                    }else if(result.getStatus()==9800) { // token 已过期 ，重新获取
                        mHandler.removeMessages(HEART_BEAT_CMD);
                        getToken(true);
                    }else{
                        mHandler.sendEmptyMessageDelayed(HEART_BEAT_CMD, HEART_BEAT_INTERVAL_TIME);
                    }
                }

            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("send HeartBeat error  i = " + i + "msg = " + s );
                mHandler.sendEmptyMessageDelayed(HEART_BEAT_CMD, 30000);
            }
        },null);
    }

    private void reportInfo(ReportInfoParameter parameter) {
        AdvertModel mIModel = new AdvertModel();

        DevRing.httpManager().commonRequest(mIModel.reportInfo(parameter), new CommonObserver<ReportInfoResult>() {
            @Override
            public void onResult(ReportInfoResult result) {
                RingLog.d("reportInfo ok status = " + result.getStatus() );

            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("reportInfo error i = " + i + "msg = " + s );
            }
        },null);
    }
    private void updateAdList(AdListHttpResult result){
            adurls.clear();
            downloadList.clear();
            List<TemplateRegion> regionList  = result.getData().getTemplate().getRegionList();
            TemplateRegion region = regionList.get(0);
            Long advertPositionId = result.getData().getRelationMap().get(region.getIdent());
            AdvertPositionVo advertPositionVo = result.getData().getAdvertPositionMap().get(advertPositionId);
            EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_UPDATE_EVENT, result.getData()));
            if(advertPositionVo!=null) {
                List<DateScheduleVo> dateScheduleVos = advertPositionVo.getDateScheduleVos();
                mAdverPosition = advertPositionVo.getadvertPosition();
                int size = dateScheduleVos.size();
                //EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_UPDATE_EVENT, dateScheduleVos));
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


                                PlayingAdvert item = new PlayingAdvert();
                                item.setPath("");
                                item.setMd5(advertFile.getFileMd5());
                                item.setAdvertid(advertFile.getAdvertid());
                                item.setAdPositionID(dateSchedueVo.getDateSchedule().getAdvertPositionId());
                                item.setTemplateid(region.getTemplateid());
                                item.setStartDate(dateSchedueVo.getDateSchedule().getStartDate());
                                item.setEndDate(dateSchedueVo.getDateSchedule().getEndDate());
                                item.setStartTime(timeScheduleVo.getTimeSchedule().getStartTime()+":00");
                                item.setEndTime(timeScheduleVo.getTimeSchedule().getEndTime()+":00");
                                adurls.add(item);

                            }
                        }
                    }
                }
            }
            downloadAdList();
    }

    private void updatePlayListFilePath(String path){
        int size = adurls.size();
        for(int i=0;i<size;i++){
            PlayingAdvert item = adurls.get(i);
            if(path.contains(item.getMd5())){
                item.setPath(path);
            }
        }
    }

    private void saveAdvertVersion(AdvertPosition advertPosition) {
        Gson gson = new Gson();
        String str = gson.toJson(adurls);
        Log.i(TAG, "save advertlist = " + str);
        DevRing.cacheManager().diskCache("advertList").put("playList", str);

        AdvertVersion.setAdVersion(advertPosition.getId().intValue(), advertPosition.getVersion());
    }

    private void getAdList(String token) {
        AdvertModel mIModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        parameter.setDeviceClientid(SystemInfoManager.readFromNandkey("usid").toUpperCase());
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(token);
        DevRing.httpManager().commonRequest(mIModel.getAdertList(parameter), new CommonObserver<AdListHttpResult>() {
            @Override
            public void onResult(AdListHttpResult result) {
                //RingLog.d("getAdList ok result = " + result );
                downloadList.clear();
                if(result.getStatus() ==0 ) {
                    updateAdList(result);
                }
                //downloadAdList();
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
        Log.d(TAG,"check downloadAdList");
        for(int k=0;k<size;k++) {/*判断是否已经有下载任务*/
            DownloadInfo item = downloadList.get(k);
            if(item.getStatus()==DownloadInfo.STATUS_DOWNLOADING) {
                Log.d(TAG,"check downloadAdList there is file on downloading");
                return;
            }
        }
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(item.getStatus()==DownloadInfo.STATUS_NOT_DOWNLOAD) {
                item.setStatus(DownloadInfo.STATUS_DOWNLOADING);
                finished =false;
                Log.d(TAG,"check downloadAdList  download not DOWNLOAD file: " + item.getUrl());
                //downloadFile(item.getUrl(), item.getFileMd5(), item.getFileMd5() + ".mp4", 1);
                downloadWithXutils(item.getUrl(),item.getFileMd5(),item.getFileMd5()+".mp4",1);
                return;
            }
        }
        for(int j=0;j<size;j++) {
            DownloadInfo item = downloadList.get(j);
            if(item.getStatus()==DownloadInfo.STATUS_DOWNLOAD_ERROR) {
                item.setStatus(DownloadInfo.STATUS_DOWNLOADING);
                finished = false;
                Log.d(TAG,"check downloadAdList  download downlaod error file: " + item.getUrl());
                //downloadFile(item.getUrl(), item.getFileMd5(), item.getFileMd5() + ".mp4", 1);
                downloadWithXutils(item.getUrl(),item.getFileMd5(),item.getFileMd5()+".mp4",1);
                break;
            }
        }
        if(finished) {
            EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_LIST_DOWNLOAD_FINISHED_EVENT,mAdverPosition));
            saveAdvertVersion(mAdverPosition);
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

    private boolean isInDownloadList(String filePath){
        boolean isDownloading = false;
        int size = downloadList.size();
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(filePath.contains(item.getFileMd5())) {
                isDownloading = true;
                break;
            }
        }
        return isDownloading;
    }

    private  DownloadInfo getDownloadingAdInfo(){
        int size = downloadList.size();
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(item.getStatus()!=DownloadInfo.STATUS_COMPLETE) {
                return item;
            }
        }
        return null;
    }

    private void setDownloadStatus(String fileMd5,int status) {
        int size = downloadList.size();
        for(int i=0;i<size;i++) {
            DownloadInfo item = downloadList.get(i);
            if(item.getFileMd5().equals(fileMd5)) {
                item.setStatus(status);
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
                    }
                    /*else {
                        if(type == 1) {
                            Message msg = new Message();
                            msg.what = DOWNLOAD_ERROR_CMD;
                            msg.obj = fileMd5;
                            mHandler.sendMessage(msg);
                            Log.d(TAG, "Download ad file Failed:" + filePath);
                        }
                    }*/
                }

                @Override
                public void onError(long progressInfoId, String errMessage) {
                    if(type ==1 ) {
                        Message msg = new Message();
                        msg.what = DOWNLOAD_ERROR_CMD;
                        msg.obj = fileMd5;
                        mHandler.removeMessages(DOWNLOAD_ERROR_CMD);
                        mHandler.sendMessageDelayed(msg,1000);
                        Log.d(TAG, "onError Download ad file Failed:" + fileName);
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

    private void checkAvailableStorage(){
        Log.i(TAG,"Total size :"+ CommonUtil.getTotalSize() + "Available Size : " + CommonUtil.getAvailableSize());
        long totalSize = CommonUtil.getTotalSize();
        long availableSize = CommonUtil.getAvailableSize();
        if(availableSize < (totalSize - totalSize*0.8)){
            File delFile = new File(FileUtil.getExternalCacheDir(getApplicationContext()));
            if (delFile.isDirectory()) {
                File[] files = delFile.listFiles();
                for (File file : files) {
                    if(file.getAbsolutePath().contains(".mp4")) {
                        if(!isInDownloadList(file.getAbsolutePath())) {
                            file.delete();
                            Log.i("tag", "delete file :" + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private void downloadWithXutils(String url,final  String fileMd5,final String fileName,final int type){

        final String filePath = FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + fileName;
        checkAvailableStorage();
        File fileCheck = new File(FileUtil.getExternalCacheDir(getApplicationContext()), fileName);
        if (fileCheck.exists() && fileCheck.length() >0 ) {
            if(EncryptUtil.md5sum(filePath).equals(fileMd5))
            {
                Message msg = new Message();
                msg.what = DOWNLOAD_COMPLITE_CMD;
                msg.obj = fileMd5;
                mHandler.sendMessage(msg);
                EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT, filePath));
                updatePlayListFilePath(FileUtil.getExternalCacheDir(getApplicationContext()) + "/" + fileMd5 + ".mp4");
                return;
            }else {
                fileCheck.delete();
            }
        }


        //设置请求参数
        RequestParams params = new RequestParams(url);
        params.setAutoResume(true);//设置是否在下载是自动断点续传
        params.setAutoRename(false);//设置是否根据头信息自动命名文件
        params.setSaveFilePath(filePath);
        params.setExecutor(new PriorityExecutor(2, true));//自定义线程池,有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
        params.setCancelFast(true);//是否可以被立即停止.
        //下面的回调都是在主线程中运行的,这里设置的带进度的回调
        Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onCancelled(CancelledException arg0) {
                Log.i("tag", "取消"+Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                Log.i("tag", "onError: 失败"+Thread.currentThread().getName());
                if(type ==1 ) {
                    Message msg = new Message();
                    msg.what = DOWNLOAD_ERROR_CMD;
                    msg.obj = fileMd5;
                    mHandler.removeMessages(DOWNLOAD_ERROR_CMD);
                    mHandler.sendMessageDelayed(msg, 1000);
                }else if(type == 0 ) {
                    mHandler.removeMessages(UPGRADE_APP_CMD);
                    mHandler.sendEmptyMessage(UPGRADE_APP_CMD);
                }
                Log.d(TAG, "onError Download ad file Failed:" + fileName);
            }

            @Override
            public void onFinished() {
                Log.i("tag", "完成,每次取消下载也会执行该方法"+Thread.currentThread().getName());
            }

            @Override
            public void onSuccess(File arg0) {
                Log.i("tag", "下载成功的时候执行"+Thread.currentThread().getName());

                String downloadmd5 = EncryptUtil.md5sum(filePath);
                Log.i(TAG,"DownloadSuccess:" + filePath + "downloadmd5 = " + downloadmd5 + "fileMd5 = " + fileMd5 );
                if(downloadmd5.equals(fileMd5)) {
                    if(type ==1 ) {
                        Message msg = new Message();
                        msg.what = DOWNLOAD_COMPLITE_CMD;
                        msg.obj = fileMd5;
                        mHandler.sendMessage(msg);
                        EventBus.getDefault().post(new AppEvent(AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT, filePath));
                        updatePlayListFilePath(filePath);
                        Log.d(TAG, "Download File finished filePath :" + filePath);
                    }else if(type == 0) { /*apk 下载*/
                        Utils.installSilently(filePath);
                    }
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (isDownloading) {

                    //Log.i("tag", "下载中,会不断的进行回调:"+Thread.currentThread().getName());
                }
            }

            @Override
            public void onStarted() {
                Log.i("tag", "开始下载的时候执行"+Thread.currentThread().getName());
            }

            @Override
            public void onWaiting() {
                Log.i("tag", "等待,在onStarted方法之前执行"+Thread.currentThread().getName());
            }

        });
    }

    /**
     * 得到当前的手机蜂窝网络信号强度
     * 获取LTE网络和3G/2G网络的信号强度的方式有一点不同，
     * LTE网络强度是通过解析字符串获取的，
     * 3G/2G网络信号强度是通过API接口函数完成的。
     * asu 与 dbm 之间的换算关系是 dbm=-113 + 2*asu
     */
    public void getCurrentNetDBM(Context context) {

        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mylistener = new PhoneStateListener(){
            //            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String signalInfo = signalStrength.toString();
                String[] params = signalInfo.split(" ");


                if(tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                    //4G网络 最佳范围   >-90dBm 越大越好
                    int Itedbm = Integer.parseInt(params[9]);
                    //setDBM(Itedbm+"");

                }else if(tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
                    /*
                    String yys = IntenetUtil.getYYS(getApplication());//获取当前运营商
                    if (yys=="中国移动") {
                        //setDBM(0+"");//中国移动3G不可获取，故在此返回0
                    }else if (yys=="中国联通") {
                        int cdmaDbm = signalStrength.getCdmaDbm();
                        //setDBM(cdmaDbm+"");
                    }else if (yys=="中国电信") {
                        int evdoDbm = signalStrength.getEvdoDbm();
                        //setDBM(evdoDbm+"");
                    }
                    */
                }else{
                    //2G网络最佳范围>-90dBm 越大越好
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2*asu;
                    //setDBM(dbm+"");
                }

            }
        };
        //开始监听
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
}
