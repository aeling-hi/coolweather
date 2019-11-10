package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**空气质量指数
 * 城市空气质量指数cityAQI(：aqi,pm25)
 * */
public class AQI {
    @SerializedName("city")
    public CityAQI cityAQI;

    public class CityAQI{
        public String aqi;
        public String pm25;
    }

}
