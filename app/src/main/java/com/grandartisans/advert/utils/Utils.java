package com.grandartisans.advert.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.aspsine.multithreaddownload.util.ListUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Aspsine on 2015/7/29.
 */
public class Utils {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public static String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }

    public static void installApp(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void unInstallApp(Context context, String packageName) {
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String getApkFilePackage(Context context, File apkFile) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkFile.getPath(), PackageManager.GET_ACTIVITIES);
        if (info != null){
            return info.applicationInfo.packageName;
        }
        return null;
    }

    public static String getAppVersionName(Context context)
    {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static String getAppVersionName(Context context,String packageName)
    {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static int  getAppVersionCode(Context context)
    {
        int versioncode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versioncode = pi.versionCode;
            Log.e("grandartisans","versionCode = " + pi.versionCode + "versionName = " + pi.versionName + "packagName = " + pi.packageName);
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versioncode;
    }
    public static int  getAppVersionCode(Context context,String packageName)
    {
        int versioncode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            versioncode = pi.versionCode;
            Log.e("grandartisans","versionCode = " + pi.versionCode + "versionName = " + pi.versionName + "packagName = " + pi.packageName);
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versioncode;
    }
    public static String getAppPackageName(Context context)
    {
        String packageName="";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            packageName = pi.packageName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return packageName;
    }
    public static String getAndroidVersion()
    {
        Log.d("VERSION SDK ",android.os.Build.VERSION.SDK);
        Log.d("VERSION RELEASE",android.os.Build.VERSION.RELEASE);
        int currentapiVersion=android.os.Build.VERSION.SDK_INT;
        Log.d("Android Verison ","" + currentapiVersion);
        return "";
    }
    public static boolean isAppInstalled(Context context, String packageName) {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        if (!ListUtils.isEmpty(packages)) {
            for (PackageInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The install command installs a package to the system. Options:
     *
     * @command -l: install the package with FORWARD_LOCK.
     * @command -r: reinstall an existing app, keeping its data.
     * @command -t: allow test .apks to be installed.
     * @command -i: specify the installer package name.
     * @command -s: install package on sdcard.
     * @command -f: install package on internal flash.
     */
    /**
     * The uninstall command removes a package from the system. Options:
     *
     * @command -k: keep the data and cache directories around. after the
     *          package removal.
     */
    public static String installSilently(String path) {
        // 通过命令行来安装APK
        String[] args = { "pm", "install", "-r", path };
        String result = "";
        // 创建一个操作系统进程并执行命令行操作
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
