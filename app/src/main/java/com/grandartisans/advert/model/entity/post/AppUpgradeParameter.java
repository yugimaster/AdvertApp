package com.grandartisans.advert.model.entity.post;

public class AppUpgradeParameter {
    String deviceClientid;
    String appName;
    String appIdent;
    String version;
    int androidVersion;
    String systemVersion;
    String requestUuid;
    long timestamp;
    String sign;

    public String getDeviceClientid() {
        return deviceClientid;
    }

    public void setDeviceClientid(String deviceClientid) {
        this.deviceClientid = deviceClientid;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIdent() {
        return appIdent;
    }

    public void setAppIdent(String appIdent) {
        this.appIdent = appIdent;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(int androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
