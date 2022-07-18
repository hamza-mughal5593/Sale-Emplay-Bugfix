package com.deens.cheese;

public class CityClass {

    int CityId;
    String CityName;

    public CityClass(int cityId, String cityName) {
        CityId = cityId;
        CityName = cityName;
    }

    public int getCityId() {
        return CityId;
    }

    public void setCityId(int cityId) {
        CityId = cityId;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }
}
