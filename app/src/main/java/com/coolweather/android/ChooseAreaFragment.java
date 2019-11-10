package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.ParseUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    //省，市，县类列表
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County> countyList = new ArrayList<>();

    private List<String> dataList = new ArrayList<>();  //所需数据名字

    //布局变量
    private Button backButton;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    //当前级别
    private int current_level;

    //被选的省市县
    private Province selected_province;
    private City selected_city;
    private County selected_county;

    //进度条
    private ProgressDialog progressDialog;

    /**
     * 活动创建后就执行碎片的onCreateView进行布局初始化
     * 初始化选择地区界面并为列表子项设置适配器
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        backButton = view.findViewById(R.id.back_button);
        titleText = view.findViewById(R.id.title_text);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }


    /**活动创建后回调
     * 为按钮以及子项绑定监听
     * 初始化省数据界面
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //按钮点击事件
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(current_level == LEVEL_CITY){
                    //返回省界面
                    queryProvinces();
                }else if(current_level == LEVEL_COUNTY){
                    //返回市界面
                    queryCities();
                }
            }
        });

        //子项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(current_level == LEVEL_PROVINCE){
                    selected_province = provinceList.get(position);
                    queryCities();
                }else if(current_level == LEVEL_CITY){
                    selected_city = cityList.get(position);
                    queryCounties();
                }else if(current_level == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        queryProvinces();
    }

    /**为活动初始化省数据界面
     * 先从数据库查询，没有则去服务器读到数据库[该方法会回调本方法：总是在数据库查到数据]
     **/
    private void queryProvinces(){
        backButton.setVisibility(View.GONE);
        titleText.setText("中国");
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size() > 0){
            //数据库有数据，重设dataList中的数据,让适配器更新,listView指向0，当前水平设为省
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);       //▲
            current_level = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";  //***
            queryFromServer(address,"province");
        }
    }

    /**查询市*/
    private void queryCities(){
        //使返回按钮可见，标题为省名
        backButton.setVisibility(View.VISIBLE);
        titleText.setText(selected_province.getProvinceName());
        //先从数据库查询：有重设dataList中的数据,让适配器更新,listView指向0，当前水平设为市
        cityList = LitePal.where("provinceid = ?",String.valueOf(selected_province.getProvinceCode()))
                .find(City.class);      //▲
        if (cityList.size() > 0) {
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            current_level = LEVEL_CITY;
        }else {
            // 没有则去服务器读到数据库[该方法会回调本方法：总是在数据库查到数据]
            int provinceCode = selected_province.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }
    /**查询县*/
    private void queryCounties(){
        backButton.setVisibility(View.VISIBLE);
        titleText.setText(selected_city.getCityName());
        countyList = LitePal.where("cityid = ?",String.valueOf(selected_city.getCityCode()))
                .find(County.class);
        if (countyList.size() > 0 ){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            current_level = LEVEL_COUNTY;
        }else{
            int provinceCode = selected_province.getProvinceCode();
            int cityCode = selected_city.getCityCode();
            String address = "http://guolin.tech/api/china/" +provinceCode
                    + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**从服务器获取数据到数据库，再回调方法去数据库查询*/
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        //子线程：根据类型去获取数据到数据库的不同的表
        //主线程：回调方法
        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                closeProgressDialog();
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = ParseUtil.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = ParseUtil.handleCityResponse(responseText, selected_province.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = ParseUtil.handleCountyResponse(responseText, selected_city.getCityCode());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //回到主线程处理
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    /**展示进度框*/
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中……");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**隐藏进度条*/
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
