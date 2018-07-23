package com.grandartisans.advert.model.entity.post;

import com.grandartisans.advert.model.entity.res.HeartBeatData;

import java.util.ArrayList;
import java.util.List;

public class ReportInfoParameter<T> {
    String deviceClientid;
    String requestUuid;
    long timestamp;
    String token;
    private List<HeartBeatData<T>> data  = new ArrayList<>();

    public String getDeviceClientid() {
        return deviceClientid;
    }

    public void setDeviceClientid(String deviceClientid) {
        this.deviceClientid = deviceClientid;
    }

    public String getRequestUuid() {
        return requestUuid;
    }

    public void setRequestUuid(String requestUuid) {
        this.requestUuid = requestUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<HeartBeatData<T>> getData() {
        return data;
    }

    public void setData(List<HeartBeatData<T>> data) {
        this.data = data;
    }
}
