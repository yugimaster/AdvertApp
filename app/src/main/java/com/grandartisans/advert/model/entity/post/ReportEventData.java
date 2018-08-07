package com.grandartisans.advert.model.entity.post;

public class ReportEventData {
    long startTime;
    long endTime;
    long templateid;
    long adPositionID;
    long advertid;
    int  count;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTemplateid() {
        return templateid;
    }

    public void setTemplateid(long templateid) {
        this.templateid = templateid;
    }

    public long getAdPositionID() {
        return adPositionID;
    }

    public void setAdPositionID(long adPositionID) {
        this.adPositionID = adPositionID;
    }

    public long getAdvertid() {
        return advertid;
    }

    public void setAdvertid(long advertid) {
        this.advertid = advertid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
