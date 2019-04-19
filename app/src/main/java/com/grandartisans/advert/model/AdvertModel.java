package com.grandartisans.advert.model;

import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.HeartBeatParameter;
import com.grandartisans.advert.model.entity.post.ReportInfoParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.http.AdvertApiService;
import com.grandartisans.advert.model.imodel.IAdvertMoel;
import com.ljy.devring.DevRing;

import io.reactivex.Observable;

public class AdvertModel implements IAdvertMoel {
    @Override
    public Observable getAdertList(AdvertParameter parameter) {
        return  DevRing.httpManager().getService(AdvertApiService.class).getAdvertList(parameter);
    }

    @Override
    public Observable appUpgrade(AppUpgradeParameter parameter) {
        return  DevRing.httpManager().getService(AdvertApiService.class).appUpgrade(parameter);
    }

    @Override
    public Observable getToken(TokenParameter token) {
        return DevRing.httpManager().getService(AdvertApiService.class).getToken(token);
    }

    @Override
    public Observable sendHeartBeat(HeartBeatParameter parameter) {
        return DevRing.httpManager().getService(AdvertApiService.class).sendHeardBeat(parameter);
    }
    @Override
    public Observable reportInfo(ReportInfoParameter parameter) {
        return DevRing.httpManager().getService(AdvertApiService.class).reportInfo(parameter);
    }

    @Override
    public Observable reportEvent(EventParameter parameter) {
        return DevRing.httpManager().getService(AdvertApiService.class).reportEvent(parameter);
    }

    @Override
    public Observable getAdvertInfo(AdvertParameter parameter) {
        return DevRing.httpManager().getService(AdvertApiService.class).getAdvertInfo(parameter);
    }

    @Override
    public Observable getAdvertWeather(AdvertParameter parameter) {
        return DevRing.httpManager().getService(AdvertApiService.class).getAdvertWeather(parameter);
    }
}
