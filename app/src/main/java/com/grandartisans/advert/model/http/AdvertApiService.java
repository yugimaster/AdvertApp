package com.grandartisans.advert.model.http;

import com.grandartisans.advert.app.constant.UrlConstants;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.res.AdHttpResult;
import com.grandartisans.advert.model.entity.post.TokenParameter;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AdvertApiService {
    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_ADVERT_TOKEN)
    Observable<AdHttpResult> getToken(@Body TokenParameter token);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_APP_UPGRADE)
    Observable<AdHttpResult> appUpgrade(@Body AppUpgradeParameter parameter);

    @Headers({"Content-Type: application/json;charset=utf-8","Accept: application/json"})
    @POST(UrlConstants.GET_ADVERT_LIST)
    Observable<AdHttpResult> getAdvertList(@Body AdvertParameter parameter);
}
