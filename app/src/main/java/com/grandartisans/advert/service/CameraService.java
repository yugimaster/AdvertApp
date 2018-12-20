package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.ljy.devring.other.RingLog;

import net.ossrs.yasea.SrsPublisher;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service {
    private SrsPublisher mPublisher;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private Timer mTimer;

    private String deviceId;
    private String recordPath;

    private static final String TAG = CameraService.class.getSimpleName();
    private static final String RECORD_CHANNEL = "rtmp://119.23.28.204:1935/record";
    private static final String RTMP_CHANNEL = "rtmp://119.23.28.204:1935/live";
    private static final String END_POINT = "http://oss-cn-shenzhen.aliyuncs.com";
    private static final String ACCESS_KEY_ID = "LTAIvIhIJ3JNzkRl";
    private static final String ACCESS_KEY_SECRET = "7aZBMS42QqguHTF5cq5uPD7tle8dK3";
    private static final String BUCKET_NAME = "gadsp";
    private static final String OBJECT_KEY_DIR = "datas/soft/";

    private static final int START_RTMP = 100000;
    private static final int START_RECORD = 100001;
    private static final int UPLOAD_FILE = 100002;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {
                case START_RTMP:
                    startRtmp();
                    break;
                case START_RECORD:
                    startCameraRecord();
                    break;
                case UPLOAD_FILE:
                    uploadRecord();
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
        recordPath = Environment.getExternalStorageDirectory().getPath();
        mHandler.sendEmptyMessage(START_RECORD);
        return super.onStartCommand(intent, flags, startId);
    }

    private void startRtmp() {
        RingLog.d(TAG, "Open RTMP Camera");
        mPublisher.stopPublish();
        RingLog.d(TAG, "Publish stop");
        mPublisher.stopRecord();
        RingLog.d(TAG, "Record stop");

        mPublisher.switchToSoftEncoder();
        mPublisher.startCamera();
        String rtmpUrl = RTMP_CHANNEL + "/" + "G50234001485210002";
        RingLog.d(TAG, "The rtmp url is: " + rtmpUrl);
        mPublisher.startPublish(rtmpUrl);
    }

    private void startCameraRecord() {
        RingLog.d(TAG, "Open Record Camera");
        deviceId = SystemInfoManager.readFromNandkey("usid");
        if (deviceId == null) {
            deviceId = "G50234001485210002";
        }

        mPublisher = MediaPlayerActivity.mPublisher;
        mCamera = mPublisher.getCamera();
        if (mCamera != null) {
			RingLog.d(TAG, "Camera Id is: " + mPublisher.getCamraId());
			RingLog.d(TAG, "Start publish");
            // 切换至软编码
			mPublisher.switchToSoftEncoder();
			String rtmpUrl = RECORD_CHANNEL + "/" + "G50234001485210002";
			RingLog.d(TAG, "The record url is: " + rtmpUrl);
			// 开始推流
			mPublisher.startPublish(rtmpUrl);
			// 打开计时器
            startPublishRecordTimer();
		}
    }

    private void startPublishRecordTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (MediaPlayerActivity.IsPublishRecord) {
                    RingLog.d(TAG, "Now stop publish record, start publish rtmp");
                    mHandler.sendEmptyMessage(START_RTMP);
                }
            }
        }, 60 * 1000);
    }

    private void stopCameraRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            Toast.makeText(getApplicationContext(), "MP4 file saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadRecord() {
        // OSS初始化
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000);   // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000);   // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5);    // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2);   // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), END_POINT, credentialProvider, conf);
        RingLog.d(TAG, "OSS Init");
        uploadFile(oss);
        mHandler.sendEmptyMessage(START_RTMP);
    }

    private void uploadFile(OSS oss) {
        // 上传文件
        RingLog.d(TAG, "Upload Start");
        String date = getCurrentDate();
        String fileName = deviceId + "_" + date + ".mp4";
        String objectKey = OBJECT_KEY_DIR + fileName;
        String recordFilePath = recordPath + "/" + deviceId + ".mp4";
        RingLog.d(TAG, "File name: " + objectKey);
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, objectKey, recordFilePath);
        try {
            PutObjectResult putObjectResult = oss.putObject(put);
            RingLog.d(TAG, "PubObject: Upload Success");
            RingLog.d(TAG, "ETag: " + putObjectResult.getETag());
            RingLog.d(TAG, "RequestId: " + putObjectResult.getRequestId());
        } catch (ClientException e) {
            // 本地异常如网络异常等
            e.printStackTrace();
        } catch (ServiceException e) {
            // 服务异常
            RingLog.d(TAG, "RequestId is: " + e.getRequestId());
            RingLog.d(TAG, "ErrorCode is: " + e.getErrorCode());
            RingLog.d(TAG, "HostId is: " + e.getHostId());
            RingLog.d(TAG, "RawMessage is: " + e.getRawMessage());
        }
    }

    private String getCurrentDate() {
        String date = "";
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        date = Integer.toString(year) + getIntegerFormat(month) + getIntegerFormat(day);
        return date;
    }

    private String getIntegerFormat(int integer) {
        String str;
        if (integer < 10)
            str = "0" + Integer.toString(integer);
        else
            str = Integer.toString(integer);
        return str;
    }
}
