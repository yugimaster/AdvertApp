package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.DownloadModel;
import com.grandartisans.advert.model.MovieModel;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.res.AdHttpResult;
import com.grandartisans.advert.model.entity.res.AppUpgradeData;
import com.grandartisans.advert.model.entity.res.HttpResult;
import com.grandartisans.advert.model.entity.res.MovieRes;
import com.grandartisans.advert.model.entity.res.TokenData;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.Utils;
import com.ljy.devring.DevRing;
import com.ljy.devring.base.activity.IBaseActivity;
import com.ljy.devring.base.fragment.IBaseFragment;
import com.ljy.devring.http.support.body.ProgressInfo;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.observer.DownloadObserver;
import com.ljy.devring.other.RingLog;
import com.ljy.devring.util.FileUtil;

import java.io.File;
import java.util.List;

public class InterfaeTestActivity extends Activity implements IBaseFragment,IBaseActivity {
    @Override
    public boolean isUseFragment() {
        return true;
    }

    @Override
    public void onSaveState(Bundle bundle) {

    }

    @Override
    public void onRestoreState(Bundle bundle) {

    }

    @Override
    public boolean isUseEventBus() {
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfae_test);

        //getPlayingMovie(0,20,0);

        downloadFile();
        //getToken();

        //appUpgrade(InterfaeTestActivity.this);
    }

    /**
     * 获取正在上映的电影
     *
     * @param start 请求电影的起始位置
     * @param count 获取的电影数量
     * @param type  类型：初始化数据INIT、刷新数据REFRESH、加载更多数据LOADMORE
     */
    private void getPlayingMovie(int start, int count, final int type) {
        MovieModel mIModel = new MovieModel();
        DevRing.httpManager().commonRequest(mIModel.getPlayingMovie(start, count), new CommonObserver<HttpResult<List<MovieRes>>>() {
            @Override
            public void onResult(HttpResult<List<MovieRes>> result) {
                RingLog.d("获取" + result.getTitle() + "成功");
            }

            @Override
            public void onError(int errType, String errMessage) {

            }
        }, null);
    }

    private void appUpgrade (Context context) {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        AppUpgradeParameter parameter = new AppUpgradeParameter();
        parameter.setAndroidVersion(Utils.getAppVersionCode(context));
        //parameter.setAndroidVersion(1);
        parameter.setAppIdent(Utils.getAppPackageName(context));
        //parameter.setAppIdent("123456");
        parameter.setAppName(Utils.getAppPackageName(context));
        //parameter.setAppName("君匠广告");
        parameter.setDeviceClientid(CommonUtil.getEthernetMac());
        //parameter.setDeviceClientid("1111");
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
        tokenParameter.setDeviceClientid("1111");
        tokenParameter.setTimestamp(System.currentTimeMillis());
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(tokenParameter.getDeviceClientid()).append("$").append(tokenParameter.getTimestamp()).append("$123456");
        signed = encrypt.MD5Encode(sign.toString(),"");
        tokenParameter.setSign(signed);

        DevRing.httpManager().commonRequest(mIModel.getToken(tokenParameter), new CommonObserver<AdHttpResult<TokenData>>() {
            @Override
            public void onResult(AdHttpResult<TokenData> tokenDataAdHttpResult) {

            }

            @Override
            public void onError(int i, String s) {

            }
        },null);

    }

    DownloadObserver mDownloadObserver;
    private String downloadUrl = "http://update.thewaxseal.cn/apks/advert-signed.apk";
    /**
     * 下载文件
     */
    public void downloadFile() {
        File file = FileUtil.getFile(FileUtil.getExternalCacheDir(this), "CloudMusic_2.apk");
        DownloadModel mIModel = new DownloadModel();
        //不为空则不重新构造DownloadObserver，避免创造了多个进度监听回调
        if (mDownloadObserver == null) {
            //DownloadObserver构造函数传入要要监听的下载地址
            mDownloadObserver = new DownloadObserver(downloadUrl) {
                @Override
                public void onResult(boolean isSaveSuccess, String filePath) {
                    if (isSaveSuccess) {
                        Log.i("InterfaceTest","DownloadSuccess:" + filePath );
                        Utils.installSilently(filePath);
                    }else {
                        Log.i("InterfaceTest","DownloadFailed:" + filePath );
                    }
                }

                @Override
                public void onError(long progressInfoId, String errMessage) {

                }

                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    Log.i("InterfaceTest","DownloadProgress:" + progressInfo.getPercent() );
                }
            };
        }
        DevRing.httpManager().downloadRequest(file, mIModel.downloadFile(downloadUrl), mDownloadObserver, null);
    }
}
