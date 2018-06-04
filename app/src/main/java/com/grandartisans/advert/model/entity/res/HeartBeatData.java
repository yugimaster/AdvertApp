package com.grandartisans.advert.model.entity.res;

import java.util.ArrayList;
import java.util.List;

public class HeartBeatData {
    private long times;
    private String token;
    private String deviceClientid;
    private List<PositionVer> list =  new ArrayList<>();
    private boolean scheduleUpdate;

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceClientid() {
        return deviceClientid;
    }

    public void setDeviceClientid(String deviceClientid) {
        this.deviceClientid = deviceClientid;
    }

    public List<PositionVer> getList() {
        return list;
    }

    public void setList(List<PositionVer> list) {
        this.list = list;
    }

    public boolean isScheduleUpdate() {
        return scheduleUpdate;
    }

    public void setScheduleUpdate(boolean scheduleUpdate) {
        this.scheduleUpdate = scheduleUpdate;
    }
}
