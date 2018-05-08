package com.grandartisans.advert.model.entity.post;

public class TokenParameter {
    private String deviceClientid;
    private String rqeuestUuid;
    private long timestamp;
    private String sign;

    public String getDeviceClientid() {
        return deviceClientid;
    }

    public void setDeviceClientid(String deviceClientid) {
        this.deviceClientid = deviceClientid;
    }

    public String getRqeuestUuid() {
        return rqeuestUuid;
    }

    public void setRqeuestUuid(String rqeuestUuid) {
        this.rqeuestUuid = rqeuestUuid;
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

    @Override
    public String toString() {
        return "TokenParameter{" +
                "deviceClientid='" + deviceClientid + '\'' +
                ", rqeuestUuid='" + rqeuestUuid + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
