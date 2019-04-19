package com.grandartisans.advert.model.imodel;

import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.HeartBeatParameter;
import com.grandartisans.advert.model.entity.post.ReportInfoParameter;
import com.grandartisans.advert.model.entity.post.TokenParameter;

import io.reactivex.Observable;

public interface IAdvertMoel {
    Observable getToken(TokenParameter parameter);
    Observable appUpgrade(AppUpgradeParameter parameter);
    Observable getAdertList(AdvertParameter parameter);
    Observable sendHeartBeat(HeartBeatParameter parameter);
    Observable reportInfo(ReportInfoParameter parameter);
    Observable reportEvent(EventParameter parameter);
    Observable getAdvertInfo(AdvertParameter parameter);
    Observable getAdvertWeather(AdvertParameter parameter);
}
