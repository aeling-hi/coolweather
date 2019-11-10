package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 天气预报
 * 日期data
 * 电导率cond(:信息info)
 * 温度temperature(:最高max,最低min)
 */
public class Forecast {
    public String date;
    public Cond cond;
    @SerializedName("tmp")
    public Temperature temperature;

    public class Cond{
        @SerializedName("txt_d")
        public String info;
    }

    public class Temperature{
        public String max;
        public String min;
    }


}
