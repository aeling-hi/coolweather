package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 总类:包含其他天气情况类
 */
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    //将daily_forecast数组映射成集合
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
