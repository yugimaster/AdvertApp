package com.grandartisans.advert.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 判断网络工具类
 *
 * @author yugimaster
 */
public class NetUtil {

    /**
     * 没有连接网络
     */
    private static final int NETWORK_NONE = 0;

    /**
     * 移动网络
     */
    private static final int NETWORK_MOBILE = 1;

    /**
     * 无线网络
     */
    private static final int NETWORK_WIFI = 2;

    /**
     * 有线网络
     */
    private static final int NETWORK_ETHERNET = 3;

    public static int getNetworkState(Context context) {
        // 得到连接管理对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                int netWorkType = activeNetworkInfo.getType();
                Log.d("NetUtil", "Network type is " + netWorkType);
                if (netWorkType == ConnectivityManager.TYPE_WIFI) {
                    Log.d("NetUtil", "Network WIFI");
                    return NETWORK_WIFI;
                } else if (netWorkType == ConnectivityManager.TYPE_MOBILE) {
                    Log.d("NetUtil", "Network MOBILE");
                    return NETWORK_MOBILE;
                } else if (netWorkType == ConnectivityManager.TYPE_ETHERNET) {
                    Log.d("NetUtil", "Network ETHERNET");
                    return NETWORK_ETHERNET;
                } else {
                    Log.d("NetUtil", "Network OTHERS");
                    return NETWORK_MOBILE;
                }
            }
        }
        return NETWORK_NONE;
    }
}
