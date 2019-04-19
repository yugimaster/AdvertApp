package com.grandartisans.advert.model.entity.res;

public class AdvertWeatherData<T> {
    private String cityName;
    private Long type;
    private String air;
    private Long celcius;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getAir() {
        return air;
    }

    public void setAir(String air) {
        this.air = air;
    }

    public Long getCelcius() {
        return celcius;
    }

    public void setCelcius(Long celcius) {
        this.celcius = celcius;
    }
}
