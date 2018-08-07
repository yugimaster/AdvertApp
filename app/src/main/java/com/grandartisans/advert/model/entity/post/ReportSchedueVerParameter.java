package com.grandartisans.advert.model.entity.post;

public class ReportSchedueVerParameter {
    long templateid;
    long adPositionID;
    int version;

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
