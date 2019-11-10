package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseUtil {
    /**
     * 解析并处理服务器返回的所有省数据
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvince = new JSONArray(response);
                for(int i = 0;i < allProvince.length(); i++){
                    JSONObject jsonObject_province = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject_province.getString("name"));
                    province.setProvinceCode(jsonObject_province.getInt("id"));
                    province.save();
                }
            }catch (JSONException je){
                je.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析并处理服务器返回的所有城市数据
     * @param  response:服务器返回的城市数据
     * @param  provinceCode:城市对应的省的id
     */
    public static boolean handleCityResponse(String response,int provinceCode){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCity = new JSONArray(response);
                for(int i = 0;i < allCity.length();i++ ){
                    JSONObject jsonObject_city = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject_city.getString("name"));
                    city.setCityCode(jsonObject_city.getInt("id"));
                    city.setProvinceId(provinceCode);
                    city.save();
                }
            }catch (JSONException je){
                je.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析并处理服务器返回的所有县数据
     * @param  response:服务器返回的县数据
     * @param  cityCode:县对应的市的id
     */
    public static boolean handleCountyResponse(String response,int cityCode){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounty = new JSONArray(response);
                for(int i = 0;i < allCounty.length();i++ ){
                    JSONObject jsonObject_county = allCounty.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject_county.getString("name"));
                    county.setWeatherId(jsonObject_county.getString("weather_id"));
                    county.setCityId(cityCode);
                    county.save();
                }
            }catch (JSONException je){
                je.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**解析天气信息JSON数据构造Weather实体类*/
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Log.d("QQQ", "weatherContent:" + weatherContent);
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (JSONException je){
            je.printStackTrace();
        }
        return  null;
    }
}
