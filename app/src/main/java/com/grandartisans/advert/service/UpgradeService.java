package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.DownloadModel;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.res.AdHttpResult;
import com.grandartisans.advert.model.entity.res.AppUpgradeData;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.Utils;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.body.ProgressInfo;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.observer.DownloadObserver;
import com.ljy.devring.util.FileUtil;

import java.io.File;

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
        DevRing.httpManager().commonRequest(mIModel.appUpgrade(parameter), new CommonObserver<AdHttpResult>() {
            @Override
            public void onResult(AdHttpResult appUpgradeDataAdHttpResult) {
                Log.d(TAG,"upgrade result status = " + appUpgradeDataAdHttpResult.getStatus());
                if(appUpgradeDataAdHttpResult.getStatus()==1000) { // 检查到升级
                    AppUpgradeData data = (AppUpgradeData) appUpgradeDataAdHttpResult.getData();
                    Log.d(TAG,"filePath = " + data.getFilePath());
                    if(data.getFilePath()!=null) {
                        downloadFile(data.getFilePath(),data.getFileMd5());
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

    /**
     * 下载文件
     */
    public void downloadFile(String downloadURL,final String fileMd5) {
        File file = FileUtil.getFile(FileUtil.getExternalCacheDir(getApplicationContext()), "advert.apk");
        DownloadModel mIModel = new DownloadModel();
        //不为空则不重新构造DownloadObserver，避免创造了多个进度监听回调
        //if (mDownloadObserver == null) {
            //DownloadObserver构造函数传入要要监听的下载地址
            mDownloadObserver = new DownloadObserver(downloadURL) {
                @Override
                public void onResult(boolean isSaveSuccess, String filePath) {
                    if (isSaveSuccess) {
                        String downloadmd5 = EncryptUtil.md5sum(filePath);
                        Log.d("InterfaceTest","DownloadSuccess:" + filePath + "downloadmd5 = " + downloadmd5 + "fileMd5 = " + fileMd5 );
                        if(downloadmd5.equals(fileMd5)) {
                            Utils.installSilently(filePath);
                        }
                    }else {
                        Log.d("InterfaceTest","DownloadFailed:" + filePath );
                    }
                }

                @Override
                public void onError(long progressInfoId, String errMessage) {

                }

                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    Log.d("InterfaceTest","DownloadProgress:" + progressInfo.getPercent() );
                }
            };
        //}
        DevRing.httpManager().downloadRequest(file, mIModel.downloadFile(downloadURL), mDownloadObserver, null);
    }
}
