package top.liziyang.cuteheroweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import top.liziyang.cuteheroweather.util.HttpCallbackListener;
import top.liziyang.cuteheroweather.util.HttpUtil;
import top.liziyang.cuteheroweather.util.Utility;

/**
 * Created by stefan on 15/5/31.
 */
public class WeatherActivity extends Activity {

    private TextView textViewCity;
    private TextView textViewUpdateTime;
    private TextView textViewCurrentDate;
    private TextView textViewWeather;
    private TextView textViewTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        textViewCity = (TextView) findViewById(R.id.textview_weather_city);
        textViewUpdateTime = (TextView) findViewById(R.id.textview_update_time);
        textViewCurrentDate = (TextView) findViewById(R.id.textview_date);
        textViewWeather = (TextView) findViewById(R.id.textview_weather);
        textViewTemperature = (TextView) findViewById(R.id.textview_temperature);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // 有county_code，需要查询新的天气
            textViewUpdateTime.setText("正在更新......");
            updateWeatherByCountyCode(countyCode);
        } else {
            // 没有county_code，显示本地天气
            showLocalWeather();

        }
    }

    private void updateWeatherByCountyCode(String countyCode) {
        Log.d("Stefan", "countyCode : " + countyCode);
        String address = "http://www.weather.com.cn/data/cityinfo/101" + countyCode + ".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (!TextUtils.isEmpty(response)) {
                    /*String[] array = response.split("\\|");
                    if (array != null && array.length == 2) {
                        String weatherCode = array[1];
                        //
                    }*/
                    Log.d("Stefan", "response : " + response);
                    Utility.handleWeatherJsonResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLocalWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewUpdateTime.setText("同步失败！");
                    }
                });
            }
        });
    }

    private void showLocalWeather() {
        // 从 SharedPreference 里获取本地天气信息
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        textViewCity.setText(sharedPreferences.getString("city_name", ""));
        textViewUpdateTime.setText("今天" + sharedPreferences.getString("update_time", "更新时间") + "发布");
        textViewCurrentDate.setText(sharedPreferences.getString("current_date", "时间"));
        textViewWeather.setText(sharedPreferences.getString("weather_description", "天气情况"));
        textViewTemperature.setText(sharedPreferences.getString("low_temperature", "最低温度") + " ~ " + sharedPreferences.getString("high_temperature", "最高温度"));
    }
}
