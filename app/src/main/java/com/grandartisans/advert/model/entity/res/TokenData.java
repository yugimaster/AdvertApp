package com.grandartisans.advert.model.entity.res;

public class TokenData {
    private String deviceClientid;
    private String token;
    private int times;

    public String getDeviceClientid() {
        return deviceClientid;
    }

    public void setDeviceClientid(String deviceClientid) {
        this.deviceClientid = deviceClientid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
