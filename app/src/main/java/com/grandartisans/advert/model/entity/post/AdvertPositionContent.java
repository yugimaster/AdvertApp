package com.grandartisans.advert.model.entity.post;

public class AdvertPositionContent {
    //广告位id
    Long advertPositionId;
    //广告内容id
    Long advertId;

    public Long getAdvertPositionId() {
        return advertPositionId;
    }

    public void setAdvertPositionId(Long advertPositionId) {
        this.advertPositionId = advertPositionId;
    }

    public Long getAdvertId() {
        return advertId;
    }

    public void setAdvertId(Long advertId) {
        this.advertId = advertId;
    }
}
