package top.liziyang.cuteheroweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
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
    private TextView textViewCurrentTemperature;
    private TextView textViewShidu;
    private TextView textViewFeng;
    private TextView textViewSunrise;
    private TextView textViewCurrentDate;
    private TextView textViewSunset;

    private ImageView imageViewUpdate;
    private ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        textViewCity = (TextView) findViewById(R.id.textview_weather_city);
        textViewUpdateTime = (TextView) findViewById(R.id.textview_update_time);
        textViewCurrentTemperature = (TextView) findViewById(R.id.textview_current_temperature);
        textViewFeng = (TextView) findViewById(R.id.textview_feng);
        textViewShidu = (TextView) findViewById(R.id.textview_shidu);
        textViewSunrise = (TextView) findViewById(R.id.textview_sunrise);
        textViewCurrentDate = (TextView) findViewById(R.id.textview_date);
        textViewSunset = (TextView) findViewById(R.id.textview_sunset);

        imageViewUpdate = (ImageView) findViewById(R.id.imageview_update_weather);
        imageViewUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewUpdateTime.setText("同步中......");
                // Rotation
                RotateAnimation rotateAnimation = new RotateAnimation(0, 360 * 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(2000);
                imageViewUpdate.startAnimation(rotateAnimation);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String cityId = sharedPreferences.getString("city_id", "");
                if (!TextUtils.isEmpty(cityId)) {
                    updateWeatherByCountyCode(cityId);
                }

            }
        });
        imageViewBack = (ImageView) findViewById(R.id.imageview_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, AreaActivity.class);
                intent.putExtra("is_from_weather_activity", true);
                startActivity(intent);
                finish();
            }
        });

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // 有county_code，需要查询新的天气
            textViewUpdateTime.setText("正在更新......");
            updateWeatherByCountyCode("101" + countyCode);
        } else {
            // 没有county_code，显示本地天气
            showLocalWeather();

        }
    }

    private void updateWeatherByCountyCode(final String countyCode) {
        Log.d("Stefan", "countyCode : " + countyCode);
        //String address = "http://www.weather.com.cn/data/cityinfo/" + countyCode + ".html";
        String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + countyCode;
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (!TextUtils.isEmpty(response)) {
                    Log.d("Stefan", "response : " + response);
                    //Utility.handleWeatherJsonResponse(WeatherActivity.this, response);
                    Utility.handleWeatherXmlResponse(WeatherActivity.this, response, countyCode);
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
        textViewUpdateTime.setText("今天" + sharedPreferences.getString("update_time", "--:--") + "发布");
        textViewCurrentTemperature.setText(sharedPreferences.getString("current_temperature", "--℃") + "℃");
        textViewShidu.setText("湿度 " + sharedPreferences.getString("shi_du", "--%"));
        textViewFeng.setText(sharedPreferences.getString("feng_xiang", "-风") + " " + sharedPreferences.getString("feng_li", "-级"));

        textViewSunrise.setText("日出 " + sharedPreferences.getString("sun_rise", "--:--"));
        textViewCurrentDate.setText(sharedPreferences.getString("current_date", "----年--月--日"));
        textViewSunset.setText("日落 " + sharedPreferences.getString("sun_set", "--:--"));
    }
}
