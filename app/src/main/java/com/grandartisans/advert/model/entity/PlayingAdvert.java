package com.grandartisans.advert.model.entity;

public class PlayingAdvert {
    private String path;
    private String md5;
    private String startTime;
    private String endTime;
    private long advertid;
    private long adPositionID;
    private long templateid;
    private String startDate;
    private String endDate;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

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

    public long getAdvertid() {
        return advertid;
    }

    public void setAdvertid(long advertid) {
        this.advertid = advertid;
    }

    public long getAdPositionID() {
        return adPositionID;
    }

    public void setAdPositionID(long adPositionID) {
        this.adPositionID = adPositionID;
    }

    public long getTemplateid() {
        return templateid;
    }

    public void setTemplateid(long templateid) {
        this.templateid = templateid;
    }
}
