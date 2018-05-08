package com.grandartisans.advert.model.entity.post;

public class AdvertParameter {
    String deviceClientid;
    String requestUuid;
    long timestamp;
    String token;

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
}
