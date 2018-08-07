package com.grandartisans.advert.model.entity.post;

public class EventParameter<T> {
    private String sn;
    private String mac;
    private String ip;
    private String token;
    private int eventtype;
    private String event;
    private String app;
    private String sessionid;
    private long timestamp;
    private long servertimestamp;
    private int duration;
    private T eventData;
    private T eventExtData;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getEventtype() {
        return eventtype;
    }

    public void setEventtype(int eventtype) {
        this.eventtype = eventtype;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getServertimestamp() {
        return servertimestamp;
    }

    public void setServertimestamp(long servertimestamp) {
        this.servertimestamp = servertimestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public T getEventData() {
        return eventData;
    }

    public void setEventData(T eventData) {
        this.eventData = eventData;
    }

    public T getEventExtData() {
        return eventExtData;
    }

    public void setEventExtData(T eventExtData) {
        this.eventExtData = eventExtData;
    }
}