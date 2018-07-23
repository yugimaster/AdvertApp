package com.grandartisans.advert.model.entity;

public class PlayingAdvert {
    private String path;
    private String md5;
    private String startTime;
    private String endTime;
    private Long advertid;
    private Long adPositionID;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getAdvertid() {
        return advertid;
    }

    public void setAdvertid(Long advertid) {
        this.advertid = advertid;
    }

    public Long getAdPositionID() {
        return adPositionID;
    }

    public void setAdPositionID(Long adPositionID) {
        this.adPositionID = adPositionID;
    }
}
