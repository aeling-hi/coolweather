package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**天气基本信息
 * 城市名:cityName
 * 天气id:weatherId
 * 更新时间:update(:更新时间:updateTime)
 */
public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
