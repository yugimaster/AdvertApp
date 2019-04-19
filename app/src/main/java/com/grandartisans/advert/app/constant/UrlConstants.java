package com.grandartisans.advert.app.constant;

/**
 * author:  ljy
 * date:    2017/9/25
 * description: 网络地址常量类
 */

public interface UrlConstants {

    //String BASE_URL = "https://api.douban.com/";
    String BASE_URL = "http://api.dsp.grandartisans.cn/v1/";
    String GET_PLAYING_MOVIE = "v2/movie/in_theaters";
    String GET_COMMING_MOVIE = "v2/movie/coming_soon";

    String UPLOAD = "http://upload.qiniu.com/";
    String DOWNLOAD = "http://s1.music.126.net/download/android/CloudMusic_2.8.1_official_4.apk";

    String GET_ADVERT_TOKEN = "api/token";
    String GET_APP_UPGRADE = "api/version/app/check";
    String GET_SYSTEM_UPGRADE = "api/version/system/check";
    String GET_ADVERT_LIST = "api/schedule/times";
    String GET_ADVERT_INFO = "api/system/Info";
    String GET_ADVERT_WEATHER = "api/system/weather";
    String  SEND_HEARTBEAT = "api/heartbeat/advertV1";
    String REPORT_INFO = "api/heartbeat/info";
    String REPORT_EVENT = "api/event/export";
}
