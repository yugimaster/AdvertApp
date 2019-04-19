package com.grandartisans.advert.model.entity.res;

public class AdvertWeatherResult {
    private boolean success;
    private int status;
    private String msg;
    private AdvertWeatherData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public AdvertWeatherData getData() {
        return data;
    }

    public void setData(AdvertWeatherData data) {
        this.data = data;
    }
}
