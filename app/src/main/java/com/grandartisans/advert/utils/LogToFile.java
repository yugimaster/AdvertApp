package com.grandartisans.advert.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ljy.devring.DevRing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ��Log��־д���ļ���
 * <p>
 * ʹ�õ���ģʽ����ΪҪ��ʼ���ļ����λ��
 * <p>
 * Created by waka on 2016/3/14.
 */
public class LogToFile {

    private static String TAG = "LogToFile";

    private static String logPath = null;//log��־���·��
    private static String mFileName = null;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);//���ڸ�ʽ;

    private static Date date = new Date();//��Ϊlog��־��ʹ�����������ģ�ʹ�þ�̬��Ա������Ҫ��Ϊ�����������������ڼ�ֻ����һ��.log�ļ���;

    /**
     * ��ʼ��������ʹ��֮ǰ���ã������Application����ʱ����
     *
     * @param context
     */
    public static void init(Context context) {
        logPath = getFilePath(context) + "/Logs";//����ļ�����·��,�ں����"/Logs"�������ļ���
        Log.i(TAG,"logPath = " + logPath);

        int index = DevRing.cacheManager().spCache("logFile").getInt("index",0);

        index ++;
        DevRing.cacheManager().spCache("logFile").put("index",index);

        mFileName = logPath + "/log_" + dateFormat.format(date) +".log"+"_"+index;//log��־����ʹ��ʱ����������֤���ظ�
    }

    /**
     * ����ļ��洢·��
     *
     * @return
     */
    private static String getFilePath(Context context) {

        if (Environment.MEDIA_MOUNTED.equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {//����ⲿ�������
            return context.getExternalFilesDir(null).getPath();//����ⲿ�洢·��,Ĭ��·��Ϊ /storage/emulated/0/Android/data/com.waka.workspace.logtofile/files/Logs/log_2016-03-14_16-15-09.log
        } else {
            return context.getFilesDir().getPath();//ֱ�Ӵ���/data/data���root�ֻ��ǿ�������
        }
    }

    private static final char VERBOSE = 'v';

    private static final char DEBUG = 'd';

    private static final char INFO = 'i';

    private static final char WARN = 'w';

    private static final char ERROR = 'e';

    public static void v(String tag, String msg) {
        writeToFile(VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        writeToFile(DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        writeToFile(INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        writeToFile(WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        writeToFile(ERROR, tag, msg);
    }

    /**
     * ��log��Ϣд���ļ���
     *
     * @param type
     * @param tag
     * @param msg
     */
    private static void writeToFile(char type, String tag, String msg) {

        if (null == logPath) {
            Log.e(TAG, "logPath == null ��δ��ʼ��LogToFile");
            return;
        }

        String log = dateFormat.format(new Date()) + " " + type + " " + tag + " " + msg + "\n";//log��־���ݣ��������ж���

        //�����·��������
        File file = new File(logPath);
        if (!file.exists()) {
            file.mkdirs();//������·��
        }

        FileOutputStream fos = null;//FileOutputStream���Զ����õײ��close()���������ùر�
        BufferedWriter bw = null;
        try {

            fos = new FileOutputStream(mFileName, true);//����ĵڶ�����������׷�ӻ��Ǹ��ǣ�trueΪ׷�ӣ�flaseΪ����
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(log);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();//�رջ�����
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
