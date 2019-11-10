package com.coolweather.android;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.coolweather.android.R;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.ParseUtil;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView scrollView_weatherLayout;

    private TextView textView_titleCity;

    private TextView textView_titleUpdateTime;

    private TextView textView_degree;

    private TextView textView_cond;

    private LinearLayout linearLayout_forecastLayout;

    private TextView textView_aqi;

    private  TextView textView_pm25;

    private TextView textView_comport;

    private  TextView textView_carWash;

    private  TextView textView_sport;

    private ImageView imageView_background;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各控件
        scrollView_weatherLayout = findViewById(R.id.weather_layout);
        textView_titleCity = findViewById(R.id.title_city);
        textView_titleUpdateTime = findViewById(R.id.title_update_time);
        textView_degree = findViewById(R.id.degree_text);
        textView_cond = findViewById(R.id.cond_info_text);
        linearLayout_forecastLayout = findViewById(R.id.forecast_layout);
        textView_aqi = findViewById(R.id.aqi_text);
        textView_pm25 = findViewById(R.id.pm25_text);
        textView_comport = findViewById(R.id.comfort_text);
        textView_carWash = findViewById(R.id.car_wash_text);
        textView_sport = findViewById(R.id.sport_text);
        imageView_background = findViewById(R.id.background_image);


        //加载天气情况数据
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherString = sharedPreferences.getString("weatherStringFromServer",null);
        if(weatherString != null){
            Weather weather = ParseUtil.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            // 无缓存时去服务器查询天气
            // ▲通过Intent从主活动获得天气信息▲
            String weatherId = getIntent().getStringExtra("weather_id");
            //为美观而隐藏视图
            scrollView_weatherLayout.setVisibility(View.INVISIBLE);
            requestWeatherInfo(weatherId);
        }

        //加载背景图片
        String imageString = sharedPreferences.getString("imageStringFromServer",null);
        if (imageString != null) {
            Glide.with(WeatherActivity.this).load(imageString).into(imageView_background);
        }else{
            loadBackgroundImage();
        }

        //融合状态栏和背景图
        if(Build.VERSION.SDK_INT >= 21){
            View  decorView = getWindow().getDecorView();
            //改变UI显示，设置将本活动会显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 本地缓存有数据，直接从本地读取
     */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String temperature = weather.now.temperature + "℃";
        String cond = weather.now.cond.info;
        textView_titleCity.setText(cityName);
        textView_titleUpdateTime.setText(updateTime);
        textView_degree.setText(temperature);
        textView_cond.setText(cond);

        for(Forecast forecast:weather.forecastList){
            //加载forecast_item布局，并指定父局为forecast_layout
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,linearLayout_forecastLayout,false);
            //忘了加view
            TextView textView_date = view.findViewById(R.id.forecast_data_text);
            TextView textView_forecast_cond = view.findViewById(R.id.forecast_cond_text);
            TextView textView_max = view.findViewById(R.id.forecast_max_text);
            TextView textView_min = view.findViewById(R.id.forecast_min_text);
            //date而不是data
            textView_date.setText(forecast.date);
            textView_forecast_cond.setText(forecast.cond.info);
            textView_max.setText(forecast.temperature.max);
            textView_min.setText(forecast.temperature.min);
            //▲
            linearLayout_forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            textView_aqi.setText(weather.aqi.cityAQI.aqi);
            textView_pm25.setText(weather.aqi.cityAQI.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWish.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        textView_comport.setText(comfort);
        textView_carWash.setText(carWash);
        textView_sport.setText(sport);
        //显示天气布局
        scrollView_weatherLayout.setVisibility(View.VISIBLE);
    }
    /**
     * 从服务器获得数据,再从本地读取读取
     */
    private void requestWeatherInfo(String weatherId){
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId +"&key=9618c9b7080b4638a16fca8687bf9a60";

        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(WeatherActivity.this,"天气信息加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = ParseUtil.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && weather.status.equals("ok")){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weatherStringFromServer",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else{
                            Toast.makeText(WeatherActivity.this,"加载天气失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 从服务器加载背景图片
     */
    private void loadBackgroundImage(){
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"加载图片失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
               final String responseString = response.body().string();
               SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
               editor.putString("imageStringFromServer",responseString);
               editor.apply();
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Glide.with(WeatherActivity.this).load(responseString).into(imageView_background);
                   }
               });
            }
        });
    }

}