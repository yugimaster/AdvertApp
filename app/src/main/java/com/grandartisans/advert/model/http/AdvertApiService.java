package com.grandartisans.advert.model.http;

import com.grandartisans.advert.app.constant.UrlConstants;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.HeartBeatParameter;
import com.grandartisans.advert.model.entity.post.ReportInfoParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.entity.res.AdListHttpResult;
import com.grandartisans.advert.model.entity.res.AdvertInfoResult;
import com.grandartisans.advert.model.entity.res.AdvertWeatherResult;
import com.grandartisans.advert.model.entity.res.HeartBeatResult;
import com.grandartisans.advert.model.entity.res.ReportInfoResult;
import com.grandartisans.advert.model.entity.res.TokenHttpResult;
import com.grandartisans.advert.model.entity.res.UpgradeHttpResult;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AdvertApiService {
    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_ADVERT_TOKEN)
    Observable<TokenHttpResult> getToken(@Body TokenParameter token);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_APP_UPGRADE)
    Observable<UpgradeHttpResult> appUpgrade(@Body AppUpgradeParameter parameter);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_ADVERT_LIST)
    Observable<AdListHttpResult> getAdvertList(@Body AdvertParameter parameter);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.SEND_HEARTBEAT)
    Observable<HeartBeatResult> sendHeardBeat(@Body HeartBeatParameter parameter );

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.REPORT_INFO)
    Observable<ReportInfoResult> reportInfo(@Body ReportInfoParameter parameter);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.REPORT_EVENT)
    Observable<ReportInfoResult> reportEvent(@Body EventParameter parameter);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_ADVERT_INFO)
    Observable<AdvertInfoResult> getAdvertInfo(@Body AdvertParameter parameter);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_ADVERT_WEATHER)
    Observable<AdvertWeatherResult> getAdvertWeather(@Body AdvertParameter parameter);
}
