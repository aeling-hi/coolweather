package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**当前天气情况
 * 温度：temperature
 * 电导率cond（：信息info）
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;

    public Cond cond;

    public class Cond{
        @SerializedName("txt")
        public String info;
    }

}
